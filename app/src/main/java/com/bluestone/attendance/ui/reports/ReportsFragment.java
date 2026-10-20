package com.bluestone.attendance.ui.reports;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bluestone.attendance.R;
import com.bluestone.attendance.databinding.FragmentReportsBinding;
import com.bluestone.attendance.domain.model.SubjectAttendanceSummary;
import com.bluestone.attendance.ui.subjects.SubjectAdapter;
import com.bluestone.attendance.utils.AttendanceCalculator;

import java.util.ArrayList;
import java.util.List;

public class ReportsFragment extends Fragment {

    private FragmentReportsBinding binding;
    private ReportsViewModel viewModel;
    private SubjectAdapter atRiskAdapter;
    private SubjectAdapter allSubjectsAdapter;

    private ActivityResultLauncher<String> createDocumentLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Storage Access Framework (SAF) document creator launcher
        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.CreateDocument("text/csv"),
                uri -> {
                    if (uri != null) {
                        exportCsv(uri);
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReportsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        setupRecyclerViews();
        setupExportActions();
        observeViewModel();
    }

    private void setupRecyclerViews() {
        atRiskAdapter = new SubjectAdapter(item -> {
            Bundle bundle = new Bundle();
            bundle.putLong("subjectId", item.getSubject().getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_subjects_to_detail, bundle);
        });
        binding.rvAtRiskSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAtRiskSubjects.setHasFixedSize(true);
        binding.rvAtRiskSubjects.setAdapter(atRiskAdapter);

        allSubjectsAdapter = new SubjectAdapter(item -> {
            Bundle bundle = new Bundle();
            bundle.putLong("subjectId", item.getSubject().getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_subjects_to_detail, bundle);
        });
        binding.rvAllSubjectBreakdown.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAllSubjectBreakdown.setHasFixedSize(true);
        binding.rvAllSubjectBreakdown.setAdapter(allSubjectsAdapter);
    }

    private void setupExportActions() {
        binding.btnExportCsv.setOnClickListener(v -> {
            // Trigger SAF document creation prompt
            createDocumentLauncher.launch("bluestone_attendance_report.csv");
        });

        binding.btnShareReport.setOnClickListener(v -> {
            viewModel.createShareIntent(requireContext(), new ReportsViewModel.ShareCallback() {
                @Override
                public void onReady(Intent intent) {
                    startActivity(intent);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), "Could not share report: " + message, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void exportCsv(Uri uri) {
        viewModel.exportCsvToUri(requireContext(), uri, () -> {
            Toast.makeText(requireContext(), R.string.csv_exported, Toast.LENGTH_SHORT).show();
        }, () -> {
            Toast.makeText(requireContext(), "Failed to export CSV", Toast.LENGTH_SHORT).show();
        });
    }

    private void observeViewModel() {
        viewModel.getOverallSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                double pct = summary.getOverallPercentage();
                binding.tvRepTotalAttended.setText(String.valueOf(summary.getTotalPresent()));
                binding.tvRepTotalMissed.setText(String.valueOf(summary.getTotalAbsent()));
                binding.tvRepOverallPct.setText(AttendanceCalculator.formatPercentage(pct));
                binding.tvRepFractionText.setText(
                        summary.getTotalPresent() + " of " + summary.getTotalEligible() + " classes attended"
                );

                double threshold = 75.0;
                if (viewModel.getSettings().getValue() != null) {
                    threshold = viewModel.getSettings().getValue().getAttendanceThreshold();
                }
                binding.tvRepTargetGoal.setText(Math.round(threshold) + "%");

                if (pct >= threshold) {
                    binding.tvRepStatusBadge.setText("Good Standing");
                    binding.tvRepStatusBadge.setBackgroundResource(R.drawable.bg_badge_present);
                    int canMiss = AttendanceCalculator.calculateClassesCanMiss(
                            summary.getTotalPresent(), summary.getTotalAbsent(), threshold
                    );
                    if (canMiss > 0) {
                        binding.tvRepAdviceText.setText("✓ Absence Allowance: You can safely miss " + canMiss + " upcoming lecture" + (canMiss > 1 ? "s" : ""));
                    } else {
                        binding.tvRepAdviceText.setText("✓ You are currently right on target (75% min)");
                    }
                } else if (pct >= 0) {
                    binding.tvRepStatusBadge.setText("Attention Needed");
                    binding.tvRepStatusBadge.setBackgroundResource(R.drawable.bg_badge_absent);
                    int needed = AttendanceCalculator.calculateSessionsNeeded(
                            summary.getTotalPresent(), summary.getTotalAbsent(), threshold
                    );
                    binding.tvRepAdviceText.setText("⚡ Must attend " + needed + " consecutive lecture" + (needed > 1 ? "s" : "") + " to reach " + Math.round(threshold) + "%");
                } else {
                    binding.tvRepStatusBadge.setText("No Records");
                    binding.tvRepStatusBadge.setBackgroundResource(R.drawable.bg_badge_unmarked);
                    binding.tvRepAdviceText.setText("No recorded classes yet");
                }
            }
        });

        viewModel.getSubjectSummaries().observe(getViewLifecycleOwner(), summaries -> {
            if (summaries != null) {
                double threshold = 75.0;
                if (viewModel.getSettings().getValue() != null) {
                    threshold = viewModel.getSettings().getValue().getAttendanceThreshold();
                }
                binding.chartReports.setData(summaries, threshold);

                allSubjectsAdapter.submitList(summaries);

                List<SubjectAttendanceSummary> atRisk = new ArrayList<>();
                for (SubjectAttendanceSummary s : summaries) {
                    if (s.isBelowTarget()) {
                        atRisk.add(s);
                    }
                }

                if (atRisk.isEmpty()) {
                    binding.rvAtRiskSubjects.setVisibility(View.GONE);
                    binding.tvNoAtRisk.setVisibility(View.VISIBLE);
                } else {
                    binding.rvAtRiskSubjects.setVisibility(View.VISIBLE);
                    binding.tvNoAtRisk.setVisibility(View.GONE);
                    atRiskAdapter.submitList(atRisk);
                }
            }
        });

        viewModel.getSettings().observe(getViewLifecycleOwner(), settings -> {
            if (settings != null) {
                binding.tvRepTargetGoal.setText(Math.round(settings.getAttendanceThreshold()) + "%");
                List<SubjectAttendanceSummary> summaries = viewModel.getSubjectSummaries().getValue();
                if (summaries != null) {
                    binding.chartReports.setData(summaries, settings.getAttendanceThreshold());
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
