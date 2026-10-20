package com.bluestone.attendance.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bluestone.attendance.MainActivity;
import com.bluestone.attendance.R;
import com.bluestone.attendance.databinding.FragmentDashboardBinding;
import com.bluestone.attendance.domain.model.WindowSizeClass;
import com.bluestone.attendance.utils.AttendanceCalculator;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DashboardViewModel viewModel;
    private TodayClassesAdapter todayAdapter;
    private DashboardSubjectsAdapter subjectsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        setupRecyclerViews();
        setupClickListeners(view);
        setupWeeklyPills();
        observeData();
    }

    private void setupRecyclerViews() {
        todayAdapter = new TodayClassesAdapter();
        binding.rvTodayClasses.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTodayClasses.setHasFixedSize(true);
        binding.rvTodayClasses.setItemViewCacheSize(8);
        binding.rvTodayClasses.setNestedScrollingEnabled(false);
        binding.rvTodayClasses.setAdapter(todayAdapter);

        subjectsAdapter = new DashboardSubjectsAdapter(item -> {
            Bundle bundle = new Bundle();
            bundle.putLong("subjectId", item.getSubject().getId());
            Navigation.findNavController(requireView()).navigate(R.id.action_subjects_to_detail, bundle);
        });
        binding.rvDashboardSubjects.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDashboardSubjects.setHasFixedSize(true);
        binding.rvDashboardSubjects.setItemViewCacheSize(8);
        binding.rvDashboardSubjects.setNestedScrollingEnabled(false);
        binding.rvDashboardSubjects.setAdapter(subjectsAdapter);
    }

    private void setupWeeklyPills() {
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

        LinearLayout[] pillLayouts = {
                binding.llDayPill1, binding.llDayPill2, binding.llDayPill3, binding.llDayPill4, binding.llDayPill5
        };
        android.widget.TextView[] numViews = {
                binding.tvDayPill1Num, binding.tvDayPill2Num, binding.tvDayPill3Num, binding.tvDayPill4Num, binding.tvDayPill5Num
        };
        android.widget.TextView[] nameViews = {
                binding.tvDayPill1Name, binding.tvDayPill2Name, binding.tvDayPill3Name, binding.tvDayPill4Name, binding.tvDayPill5Name
        };
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri"};

        for (int i = 0; i < 5; i++) {
            java.time.LocalDate date = monday.plusDays(i);
            numViews[i].setText(String.valueOf(date.getDayOfMonth()));
            nameViews[i].setText(dayNames[i]);

            boolean isToday = date.equals(today);
            if (isToday) {
                pillLayouts[i].setBackgroundResource(R.drawable.bg_date_pill_active);
                numViews[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                nameViews[i].setTextColor(0xFFD8CCFF);
            } else {
                pillLayouts[i].setBackgroundResource(R.drawable.bg_date_pill_inactive);
                numViews[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface));
                nameViews[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface_variant));
            }
        }
    }

    private void setupClickListeners(View view) {
        binding.btnQuickMark.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_dashboard_to_attendance);
        });

        binding.tvQuickMarkHeader.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_dashboard_to_attendance);
        });

        binding.tvViewAllSubjects.setOnClickListener(v -> {
            Navigation.findNavController(view).navigate(R.id.action_dashboard_to_subjects);
        });

        binding.ivProfileHeader.setOnClickListener(v -> showProfileDialog());
    }

    private void showProfileDialog() {
        if (!isAdded()) return;
        com.bluestone.attendance.data.entities.Student student = viewModel.getStudent().getValue();
        if (student == null) return;

        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_student_profile, null);
        TextView tvName = dialogView.findViewById(R.id.tv_prof_name);
        TextView tvId = dialogView.findViewById(R.id.tv_prof_id);
        TextView tvCourse = dialogView.findViewById(R.id.tv_prof_course);
        TextView tvEmail = dialogView.findViewById(R.id.tv_prof_email);
        TextView tvCollege = dialogView.findViewById(R.id.tv_prof_college);
        TextView tvSem = dialogView.findViewById(R.id.tv_prof_sem);

        tvName.setText(student.getFullName());
        tvId.setText("ID: " + student.getStudentId());
        tvCourse.setText(student.getCourse());
        tvEmail.setText(student.getEmail());
        tvCollege.setText(student.getCollege() != null ? student.getCollege() : "Computer Science Engineering");
        tvSem.setText("Semester " + student.getSemester() + " (" + (student.getAcademicYear() != null ? student.getAcademicYear() : "2025–2026") + ")");

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void observeData() {
        viewModel.getStudent().observe(getViewLifecycleOwner(), student -> {
            if (student != null) {
                String fullName = student.getFullName();
                String firstName = (fullName != null && fullName.contains(" "))
                        ? fullName.split("\\s+")[0] : (fullName != null ? fullName : "Student");
                binding.tvGreeting.setText("Hi, " + firstName + " 👋");
                binding.tvAcademicSemesterBadge.setText("Sem " + student.getSemester());
                binding.tvAcademicInfo.setText(student.getCourse());
            }
        });

        viewModel.getOverallSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                double pct = summary.getOverallPercentage();
                binding.tvOverallPercentage.setText(AttendanceCalculator.formatPercentage(pct));
                binding.tvOverallFraction.setText(
                        summary.getTotalPresent() + " / " + summary.getTotalEligible() + " classes attended"
                );
                binding.tvStatAttended.setText(String.valueOf(summary.getTotalPresent()));
                binding.tvStatMissed.setText(String.valueOf(summary.getTotalAbsent()));
                binding.tvStatTotal.setText(String.valueOf(summary.getTotalEligible()));

                int progressInt = (pct < 0) ? 0 : (int) Math.min(100, Math.round(pct));
                binding.progressOverallBar.setProgress(progressInt);

                double threshold = 75.0;
                if (viewModel.getSettings().getValue() != null) {
                    threshold = viewModel.getSettings().getValue().getAttendanceThreshold();
                }

                if (pct >= threshold) {
                    setOverallStatus(true);
                    int canMiss = AttendanceCalculator.calculateClassesCanMiss(
                            summary.getTotalPresent(), summary.getTotalAbsent(), threshold
                    );
                    if (canMiss > 0) {
                        binding.tvOverallAdviceBadge.setText("✓ " + canMiss + " upcoming class" + (canMiss > 1 ? "es" : "") + " can be safely missed");
                    } else {
                        binding.tvOverallAdviceBadge.setText("✓ Right on target for 75% requirement");
                    }
                } else if (pct >= 0) {
                    setOverallStatus(false);
                    int needed = AttendanceCalculator.calculateSessionsNeeded(
                            summary.getTotalPresent(), summary.getTotalAbsent(), threshold
                    );
                    binding.tvOverallAdviceBadge.setText("⚡ Attend " + needed + " more class" + (needed > 1 ? "es" : "") + " to reach " + Math.round(threshold) + "% target");
                } else {
                    setOverallStatus(true);
                    binding.tvOverallAdviceBadge.setText("No classes marked yet");
                }
            }
        });

        viewModel.getTodaySessions().observe(getViewLifecycleOwner(), list -> {
            if (list == null || list.isEmpty()) {
                binding.rvTodayClasses.setVisibility(View.GONE);
                binding.tvEmptyToday.setVisibility(View.VISIBLE);
            } else {
                binding.rvTodayClasses.setVisibility(View.VISIBLE);
                binding.tvEmptyToday.setVisibility(View.GONE);
                if (list.size() > 5) {
                    todayAdapter.submitList(list.subList(0, 5));
                } else {
                    todayAdapter.submitList(list);
                }
            }
        });

        viewModel.getSubjectSummaries().observe(getViewLifecycleOwner(), summaries -> {
            if (summaries != null) {
                subjectsAdapter.submitList(summaries);
            }
        });
    }

    private void setOverallStatus(boolean isGood) {
        if (isGood) {
            binding.tvOverallStatusBadge.setText("Good Standing");
            binding.tvOverallStatusBadge.setBackgroundResource(R.drawable.bg_badge_glass);
            binding.tvOverallStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else {
            binding.tvOverallStatusBadge.setText("Attention Needed");
            binding.tvOverallStatusBadge.setBackgroundResource(R.drawable.bg_badge_glass);
            binding.tvOverallStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        }
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
