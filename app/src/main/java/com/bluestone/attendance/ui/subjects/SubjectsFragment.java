package com.bluestone.attendance.ui.subjects;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.databinding.DialogEditSubjectBinding;
import com.bluestone.attendance.databinding.FragmentSubjectsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SubjectsFragment extends Fragment {

    private FragmentSubjectsBinding binding;
    private SubjectsViewModel viewModel;
    private SubjectAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSubjectsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SubjectsViewModel.class);

        setupRecyclerView();
        setupFiltersAndSearch();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new SubjectAdapter(item -> {
            Bundle bundle = new Bundle();
            bundle.putLong("subjectId", item.getSubject().getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_subjects_to_detail, bundle);
        });

        binding.rvSubjectsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvSubjectsList.setAdapter(adapter);
    }

    private void setupFiltersAndSearch() {
        // Search text watcher
        binding.etSearchSubjects.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Semester filter chips
        binding.chipGroupSemester.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                viewModel.setSemesterFilter(0);
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chip_sem_1) viewModel.setSemesterFilter(1);
            else if (id == R.id.chip_sem_2) viewModel.setSemesterFilter(2);
            else if (id == R.id.chip_sem_3) viewModel.setSemesterFilter(3);
            else if (id == R.id.chip_sem_4) viewModel.setSemesterFilter(4);
            else viewModel.setSemesterFilter(0);
        });

        // Sort chips
        binding.chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_sort_percentage) viewModel.setSortMode(1);
            else if (id == R.id.chip_sort_code) viewModel.setSortMode(2);
            else viewModel.setSortMode(0);
        });
    }

    private void showEditSubjectDialog(@Nullable Subject existing) {
        DialogEditSubjectBinding dialogBinding = DialogEditSubjectBinding.inflate(getLayoutInflater());

        if (existing != null) {
            dialogBinding.etSubName.setText(existing.getName());
            dialogBinding.etSubCode.setText(existing.getCode());
            dialogBinding.etSubSemester.setText(String.valueOf(existing.getSemester()));
            dialogBinding.etSubCredits.setText(String.valueOf(existing.getCredits()));
            dialogBinding.etSubFaculty.setText(existing.getFaculty() != null ? existing.getFaculty() : "");
            dialogBinding.etSubTarget.setText(String.valueOf((int) existing.getTargetPercentage()));
            dialogBinding.etSubNotes.setText(existing.getNotes() != null ? existing.getNotes() : "");
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? R.string.add_subject : R.string.edit_subject)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = dialogBinding.etSubName.getText() != null
                            ? dialogBinding.etSubName.getText().toString().trim() : "";
                    String code = dialogBinding.etSubCode.getText() != null
                            ? dialogBinding.etSubCode.getText().toString().trim() : "";

                    if (name.isEmpty() || code.isEmpty()) {
                        Toast.makeText(requireContext(), "Name and Code are required", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int semester = 1;
                    int credits = 3;
                    double target = 75.0;

                    try {
                        semester = Integer.parseInt(dialogBinding.etSubSemester.getText().toString());
                    } catch (Exception ignored) {}

                    try {
                        credits = Integer.parseInt(dialogBinding.etSubCredits.getText().toString());
                    } catch (Exception ignored) {}

                    try {
                        target = Double.parseDouble(dialogBinding.etSubTarget.getText().toString());
                    } catch (Exception ignored) {}

                    String faculty = dialogBinding.etSubFaculty.getText() != null
                            ? dialogBinding.etSubFaculty.getText().toString().trim() : null;
                    String notes = dialogBinding.etSubNotes.getText() != null
                            ? dialogBinding.etSubNotes.getText().toString().trim() : null;

                    Subject subject = (existing != null) ? existing : new Subject("", "", 1, 3, "", 75.0, "", false);
                    subject.setName(name);
                    subject.setCode(code);
                    subject.setSemester(semester);
                    subject.setCredits(credits);
                    subject.setFaculty(faculty);
                    subject.setTargetPercentage(target);
                    subject.setNotes(notes);

                    viewModel.saveSubject(subject, () -> {
                        Toast.makeText(requireContext(), "Subject saved", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getFilteredSummaries().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                binding.rvSubjectsList.setVisibility(View.GONE);
                binding.tvEmptySubjects.setVisibility(View.VISIBLE);
            } else {
                binding.rvSubjectsList.setVisibility(View.VISIBLE);
                binding.tvEmptySubjects.setVisibility(View.GONE);
                adapter.submitList(list);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
