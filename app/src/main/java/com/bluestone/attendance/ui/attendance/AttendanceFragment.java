package com.bluestone.attendance.ui.attendance;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bluestone.attendance.R;
import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.databinding.FragmentAttendanceBinding;
import com.bluestone.attendance.domain.model.AttendanceStatus;
import com.bluestone.attendance.domain.model.SubjectAttendanceSummary;
import com.bluestone.attendance.ui.dashboard.DashboardViewModel.TimetableSessionWithSubject;
import com.bluestone.attendance.utils.AttendanceCalculator;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttendanceFragment extends Fragment {

    private FragmentAttendanceBinding binding;
    private AttendanceViewModel viewModel;
    private CalendarAdapter calendarAdapter;
    private TodayAttendanceScheduleAdapter scheduleAdapter;

    private final List<Subject> subjectList = new ArrayList<>();
    private final Map<Long, Subject> subjectMap = new HashMap<>();
    private Subject currentSelectedSubject;
    private TimetableSessionWithSubject currentSelectedSession;
    private LocalDate selectedDate = LocalDate.now();
    private AttendanceStatus selectedStatus = AttendanceStatus.PRESENT;

    private static final DateTimeFormatter DATE_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy");
    private static final DateTimeFormatter MONTH_HEADER_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAttendanceBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(AttendanceViewModel.class);

        setupAdapters();
        setupFormListeners();
        observeViewModel();
    }

    private void setupAdapters() {
        // Calendar Grid
        calendarAdapter = new CalendarAdapter(date -> {
            selectedDate = date;
            binding.btnPickDate.setText(selectedDate.format(DATE_DISPLAY_FORMAT));
            Toast.makeText(requireContext(), "Selected " + selectedDate.format(DATE_DISPLAY_FORMAT), Toast.LENGTH_SHORT).show();
        });
        binding.rvCalendarGrid.setLayoutManager(new GridLayoutManager(requireContext(), 7));
        binding.rvCalendarGrid.setHasFixedSize(true);
        binding.rvCalendarGrid.setAdapter(calendarAdapter);

        // Today's Schedule Adapter
        scheduleAdapter = new TodayAttendanceScheduleAdapter(item -> {
            activateSession(item);
            Toast.makeText(requireContext(), "Activated " + (item.subject != null ? item.subject.getCode() : "Class"), Toast.LENGTH_SHORT).show();
        });
        binding.rvTodayAttendanceSchedule.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTodayAttendanceSchedule.setHasFixedSize(true);
        binding.rvTodayAttendanceSchedule.setAdapter(scheduleAdapter);
    }

    private void setupFormListeners() {
        // Date Picker Button
        binding.btnPickDate.setText("Today (" + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd")) + ")");
        binding.btnPickDate.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Attendance Date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build();

            picker.addOnPositiveButtonClickListener(selection -> {
                selectedDate = Instant.ofEpochMilli(selection)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                binding.btnPickDate.setText(selectedDate.format(DATE_DISPLAY_FORMAT));
            });

            picker.show(getParentFragmentManager(), "DATE_PICKER");
        });

        // Status Toggle Group
        binding.toggleAttendanceStatus.check(R.id.btn_status_present);
        binding.toggleAttendanceStatus.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_status_present) {
                selectedStatus = AttendanceStatus.PRESENT;
            } else if (checkedId == R.id.btn_status_absent) {
                selectedStatus = AttendanceStatus.ABSENT;
            } else if (checkedId == R.id.btn_status_unmarked) {
                selectedStatus = AttendanceStatus.NOT_MARKED;
            }
        });

        // Month pagination
        binding.btnPrevMonth.setOnClickListener(v -> viewModel.prevMonth());
        binding.btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());

        // Save / Mark Attendance Button with Location & Bluetooth Permission Check
        binding.btnSaveAttendance.setOnClickListener(v -> {
            if (currentSelectedSubject == null) {
                Toast.makeText(requireContext(), "No class scheduled or selected to mark", Toast.LENGTH_SHORT).show();
                return;
            }

            String sessionStr = binding.etSessionNumber.getText() != null
                    ? binding.etSessionNumber.getText().toString().trim() : "1";
            int session = 1;
            try {
                session = Integer.parseInt(sessionStr);
            } catch (NumberFormatException ignored) {}

            String notes = binding.etNotes.getText() != null ? binding.etNotes.getText().toString().trim() : "";

            showLocationBluetoothPermissionDialog(session, notes);
        });
    }

    private void showLocationBluetoothPermissionDialog(int session, String notes) {
        if (!isAdded()) return;
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_attendance_verification, null);
        android.widget.TextView tvLectureInfo = dialogView.findViewById(R.id.tv_verify_lecture_info);

        String info = currentSelectedSubject.getName() + " (" + currentSelectedSubject.getCode() + ")";
        if (currentSelectedSession != null && currentSelectedSession.session != null && currentSelectedSession.session.getRoom() != null) {
            info += " • " + currentSelectedSession.session.getRoom();
        }
        tvLectureInfo.setText(info);

        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Grant & Mark Present", (dialog, which) -> {
                    binding.toggleAttendanceStatus.check(R.id.btn_status_present);
                    selectedStatus = AttendanceStatus.PRESENT;
                    viewModel.saveAttendance(currentSelectedSubject.getId(), selectedDate, session, AttendanceStatus.PRESENT, notes, () -> {
                        Toast.makeText(requireContext(),
                                "✓ Bluetooth & Location verified: Attendance marked PRESENT for " + currentSelectedSubject.getCode(),
                                Toast.LENGTH_LONG).show();
                        binding.etNotes.setText("");
                    });
                })
                .setNegativeButton("Deny (Mark Absent)", (dialog, which) -> {
                    binding.toggleAttendanceStatus.check(R.id.btn_status_absent);
                    selectedStatus = AttendanceStatus.ABSENT;
                    viewModel.saveAttendance(currentSelectedSubject.getId(), selectedDate, session, AttendanceStatus.ABSENT, notes, () -> {
                        Toast.makeText(requireContext(),
                                "⚠️ Permissions denied: Attendance marked ABSENT for " + currentSelectedSubject.getCode(),
                                Toast.LENGTH_LONG).show();
                        binding.etNotes.setText("");
                    });
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void observeViewModel() {
        // Subjects
        viewModel.getSubjects().observe(getViewLifecycleOwner(), subjects -> {
            if (subjects == null) return;
            subjectList.clear();
            subjectList.addAll(subjects);
            subjectMap.clear();
            for (Subject s : subjects) {
                subjectMap.put(s.getId(), s);
            }
        });

        // Today's Scheduled Classes - Auto activation!
        viewModel.getTodaySessions().observe(getViewLifecycleOwner(), sessions -> {
            if (sessions == null || sessions.isEmpty()) {
                binding.rvTodayAttendanceSchedule.setVisibility(View.GONE);
                binding.tvEmptyAttendanceSchedule.setVisibility(View.VISIBLE);
                // Fallback to first available subject if no timetable today
                if (currentSelectedSubject == null && !subjectList.isEmpty()) {
                    currentSelectedSubject = subjectList.get(0);
                    updateActiveSubjectUI(currentSelectedSubject, null);
                }
            } else {
                binding.rvTodayAttendanceSchedule.setVisibility(View.VISIBLE);
                binding.tvEmptyAttendanceSchedule.setVisibility(View.GONE);
                scheduleAdapter.submitList(sessions);

                // Auto-activate class if not chosen yet
                if (currentSelectedSession == null) {
                    LocalTime now = LocalTime.now();
                    TimetableSessionWithSubject bestMatch = sessions.get(0);
                    for (TimetableSessionWithSubject s : sessions) {
                        if (!now.isBefore(s.session.getStartTime()) && !now.isAfter(s.session.getEndTime())) {
                            bestMatch = s;
                            break;
                        }
                    }
                    activateSession(bestMatch);
                }
            }
        });

        // Attendance records & Calendar
        viewModel.getAllRecords().observe(getViewLifecycleOwner(), records -> {
            if (viewModel.getCurrentMonth().getValue() != null) {
                calendarAdapter.setMonth(viewModel.getCurrentMonth().getValue(), records);
            }
        });

        // Summaries
        viewModel.getSummaries().observe(getViewLifecycleOwner(), summaries -> {
            updateStandingForActiveSubject();
        });

        // Current Month
        viewModel.getCurrentMonth().observe(getViewLifecycleOwner(), ym -> {
            if (ym != null) {
                binding.tvCalendarMonthYear.setText(ym.format(MONTH_HEADER_FORMAT));
                calendarAdapter.setMonth(ym, viewModel.getAllRecords().getValue());
            }
        });
    }

    private void activateSession(TimetableSessionWithSubject item) {
        if (item == null) return;
        currentSelectedSession = item;
        currentSelectedSubject = item.subject;
        scheduleAdapter.setActiveSessionId(item.session.getId());

        LocalTime now = LocalTime.now();
        boolean isLive = !now.isBefore(item.session.getStartTime()) && !now.isAfter(item.session.getEndTime());

        if (isLive) {
            binding.tvActiveClassBadge.setText("● LIVE CLASS NOW");
            binding.tvActiveClassBadge.setBackgroundResource(R.drawable.bg_badge_present);
            binding.tvActiveClassBadge.setTextColor(requireContext().getColor(R.color.present));
        } else {
            binding.tvActiveClassBadge.setText("● SCHEDULED TODAY");
            binding.tvActiveClassBadge.setBackgroundResource(R.drawable.bg_badge_glass);
            binding.tvActiveClassBadge.setTextColor(requireContext().getColor(R.color.primary));
        }

        String timeStr = item.session.getStartTime().format(TIME_FORMATTER) + " - " + item.session.getEndTime().format(TIME_FORMATTER);
        binding.tvActiveClassTime.setText(timeStr);

        if (item.subject != null) {
            binding.tvActiveSubjectName.setText(item.subject.getName() + " (" + item.subject.getCode() + ")");
            String details = (item.session.getRoom() != null ? item.session.getRoom() : "Main Lecture Hall") +
                    (item.session.getFaculty() != null ? " • " + item.session.getFaculty() : "");
            binding.tvActiveClassDetails.setText(details);
        } else {
            binding.tvActiveSubjectName.setText("Scheduled Lecture");
            binding.tvActiveClassDetails.setText(item.session.getRoom() != null ? item.session.getRoom() : "");
        }

        updateStandingForActiveSubject();
    }

    private void updateActiveSubjectUI(Subject subject, TimetableSessionWithSubject session) {
        if (subject == null) return;
        binding.tvActiveClassBadge.setText("● ENROLLED SUBJECT");
        binding.tvActiveClassBadge.setBackgroundResource(R.drawable.bg_badge_glass);
        binding.tvActiveClassBadge.setTextColor(requireContext().getColor(R.color.primary));
        binding.tvActiveClassTime.setText("Semester " + subject.getSemester());
        binding.tvActiveSubjectName.setText(subject.getName() + " (" + subject.getCode() + ")");
        binding.tvActiveClassDetails.setText(subject.getFaculty() != null ? subject.getFaculty() : "");
        updateStandingForActiveSubject();
    }

    private void updateStandingForActiveSubject() {
        if (currentSelectedSubject == null || binding == null) return;
        List<SubjectAttendanceSummary> summaries = viewModel.getSummaries().getValue();
        if (summaries != null) {
            for (SubjectAttendanceSummary s : summaries) {
                if (s.getSubject().getId() == currentSelectedSubject.getId()) {
                    if (s.getPercentage() >= 0) {
                        String standing = s.isBelowTarget() ? "Attention Needed" : "On Track";
                        binding.tvActiveSubjectStanding.setText(
                                String.format("Current Attendance: %s (%d/%d classes) • %s",
                                        AttendanceCalculator.formatPercentage(s.getPercentage()),
                                        s.getPresentCount(),
                                        s.getTotalEligible(),
                                        standing)
                        );
                        binding.tvActiveSubjectStanding.setTextColor(
                                requireContext().getColor(s.isBelowTarget() ? R.color.absent : R.color.primary)
                        );
                    } else {
                        binding.tvActiveSubjectStanding.setText("No attendance records logged yet");
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
