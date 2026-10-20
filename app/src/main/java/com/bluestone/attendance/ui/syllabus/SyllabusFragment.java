package com.bluestone.attendance.ui.syllabus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bluestone.attendance.R;
import com.bluestone.attendance.data.entities.SyllabusUnit;
import com.bluestone.attendance.databinding.DialogEditUnitBinding;
import com.bluestone.attendance.databinding.FragmentSyllabusBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;

public class SyllabusFragment extends Fragment {

    private FragmentSyllabusBinding binding;
    private SyllabusViewModel viewModel;
    private SyllabusAdapter adapter;
    private long subjectId = -1L;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSyllabusBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            subjectId = getArguments().getLong("subjectId", -1L);
        }

        viewModel = new ViewModelProvider(this).get(SyllabusViewModel.class);

        setupRecyclerView();
        setupActions(view);
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new SyllabusAdapter(new SyllabusAdapter.OnUnitActionListener() {
            @Override
            public void onToggleCompleted(SyllabusUnit unit, boolean isCompleted) {
                viewModel.setUnitCompleted(unit.getId(), isCompleted, null);
            }

            @Override
            public void onEditUnit(SyllabusUnit unit) {
                showEditUnitDialog(unit);
            }

            @Override
            public void onDeleteUnit(SyllabusUnit unit) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.confirm_delete)
                        .setMessage(R.string.delete_unit_confirm)
                        .setPositiveButton(R.string.delete, (d, w) -> {
                            viewModel.deleteUnit(unit.getId(), () -> {
                                Toast.makeText(requireContext(), "Unit deleted", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        binding.rvSyllabusUnits.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSyllabusUnits.setAdapter(adapter);
    }

    private void setupActions(View view) {
        binding.btnBackSyllabus.setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        binding.btnAddUnit.setOnClickListener(v -> {
            showEditUnitDialog(null);
        });
    }

    private void observeData() {
        viewModel.getSubject(subjectId).observe(getViewLifecycleOwner(), subject -> {
            if (subject != null) {
                binding.tvSyllabusCode.setText(subject.getCode());
                binding.tvSyllabusTitle.setText(subject.getName() + " Syllabus");
            }
        });

        viewModel.getUnits(subjectId).observe(getViewLifecycleOwner(), units -> {
            if (units == null || units.isEmpty()) {
                binding.rvSyllabusUnits.setVisibility(View.GONE);
                binding.tvEmptySyllabus.setVisibility(View.VISIBLE);
                binding.tvSyllabusFraction.setText("0 / 0 units completed");
                binding.pbSyllabusProgress.setProgress(0);
                binding.tvSyllabusPercentage.setText("No units added yet");
            } else {
                binding.rvSyllabusUnits.setVisibility(View.VISIBLE);
                binding.tvEmptySyllabus.setVisibility(View.GONE);
                adapter.submitList(units);

                int total = units.size();
                int completed = 0;
                for (SyllabusUnit u : units) {
                    if (u.isCompleted()) completed++;
                }

                double pct = ((double) completed / total) * 100.0;
                binding.tvSyllabusFraction.setText(
                        String.format(Locale.getDefault(), "%d / %d completed (%d remaining)", completed, total, (total - completed))
                );
                binding.pbSyllabusProgress.setProgress((int) pct);
                binding.tvSyllabusPercentage.setText(
                        String.format(Locale.getDefault(), "%.1f%% of syllabus completed", pct)
                );
            }
        });
    }

    private void showEditUnitDialog(@Nullable SyllabusUnit existing) {
        DialogEditUnitBinding dialogBinding = DialogEditUnitBinding.inflate(getLayoutInflater());

        if (existing != null) {
            dialogBinding.etUnitNum.setText(String.valueOf(existing.getUnitNumber()));
            dialogBinding.etUnitTitle.setText(existing.getTitle());
            dialogBinding.etUnitTopics.setText(existing.getTopics() != null ? existing.getTopics() : "");
            dialogBinding.etUnitObjectives.setText(existing.getObjectives() != null ? existing.getObjectives() : "");
            dialogBinding.etUnitNotes.setText(existing.getNotes() != null ? existing.getNotes() : "");
            dialogBinding.cbDialogCompleted.setChecked(existing.isCompleted());
        } else {
            int nextUnitNum = (adapter.getItemCount() + 1);
            dialogBinding.etUnitNum.setText(String.valueOf(nextUnitNum));
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? R.string.add_unit : R.string.edit_unit)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String title = dialogBinding.etUnitTitle.getText() != null
                            ? dialogBinding.etUnitTitle.getText().toString().trim() : "";
                    if (title.isEmpty()) {
                        Toast.makeText(requireContext(), "Unit title is required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int num = 1;
                    try {
                        num = Integer.parseInt(dialogBinding.etUnitNum.getText().toString().trim());
                    } catch (Exception ignored) {}

                    String topics = dialogBinding.etUnitTopics.getText() != null
                            ? dialogBinding.etUnitTopics.getText().toString().trim() : "";
                    String objectives = dialogBinding.etUnitObjectives.getText() != null
                            ? dialogBinding.etUnitObjectives.getText().toString().trim() : "";
                    String notes = dialogBinding.etUnitNotes.getText() != null
                            ? dialogBinding.etUnitNotes.getText().toString().trim() : "";
                    boolean completed = dialogBinding.cbDialogCompleted.isChecked();

                    SyllabusUnit unit = (existing != null) ? existing : new SyllabusUnit(subjectId, num, title, topics, objectives, notes, completed);
                    unit.setUnitNumber(num);
                    unit.setTitle(title);
                    unit.setTopics(topics);
                    unit.setObjectives(objectives);
                    unit.setNotes(notes);
                    unit.setCompleted(completed);

                    viewModel.saveUnit(unit, () -> {
                        Toast.makeText(requireContext(), "Unit saved", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
