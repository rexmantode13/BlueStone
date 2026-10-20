package com.bluestone.attendance.ui.timetable;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bluestone.attendance.R;
import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.data.entities.TimetableSession;
import com.bluestone.attendance.databinding.DialogEditTimetableBinding;
import com.bluestone.attendance.databinding.FragmentTimetableBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimetableFragment extends Fragment {

    private FragmentTimetableBinding binding;
    private TimetableViewModel viewModel;
    private TimetableAdapter adapter;

    private final List<Subject> subjectList = new ArrayList<>();
    private final Map<Long, Subject> subjectMap = new HashMap<>();

    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTimetableBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(TimetableViewModel.class);

        setupRecyclerView();
        setupDayChips();
        observeViewModel();
    }

    private void setupRecyclerView() {
        adapter = new TimetableAdapter(new TimetableAdapter.OnSessionActionListener() {
            @Override
            public void onEdit(TimetableSession session) {
                showEditSessionDialog(session);
            }

            @Override
            public void onDelete(TimetableSession session) {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.confirm_delete)
                        .setMessage(R.string.delete_session_confirm)
                        .setPositiveButton(R.string.delete, (d, w) -> {
                            viewModel.deleteSession(session.getId(), () -> {
                                Toast.makeText(requireContext(), "Session deleted", Toast.LENGTH_SHORT).show();
                            });
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });

        binding.rvTimetableSessions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTimetableSessions.setAdapter(adapter);
    }

    private void setupDayChips() {
        // Set initial chip
        int currentDay = viewModel.getSelectedDay().getValue() != null ? viewModel.getSelectedDay().getValue() : 1;
        checkChipForDay(currentDay);

        binding.chipGroupDays.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chip_day_mon) viewModel.setSelectedDay(1);
            else if (id == R.id.chip_day_tue) viewModel.setSelectedDay(2);
            else if (id == R.id.chip_day_wed) viewModel.setSelectedDay(3);
            else if (id == R.id.chip_day_thu) viewModel.setSelectedDay(4);
            else if (id == R.id.chip_day_fri) viewModel.setSelectedDay(5);
            else if (id == R.id.chip_day_all) viewModel.setSelectedDay(0);
        });
    }

    private void checkChipForDay(int day) {
        if (day == 1) binding.chipDayMon.setChecked(true);
        else if (day == 2) binding.chipDayTue.setChecked(true);
        else if (day == 3) binding.chipDayWed.setChecked(true);
        else if (day == 4) binding.chipDayThu.setChecked(true);
        else if (day == 5) binding.chipDayFri.setChecked(true);
        else binding.chipDayAll.setChecked(true);
    }

    private void showEditSessionDialog(@Nullable TimetableSession existing) {
        DialogEditTimetableBinding dBinding = DialogEditTimetableBinding.inflate(getLayoutInflater());

        // Setup subject dropdown
        List<String> subNames = new ArrayList<>();
        int selectedSubIndex = 0;
        for (int i = 0; i < subjectList.size(); i++) {
            Subject s = subjectList.get(i);
            subNames.add(s.getName() + " (" + s.getCode() + ")");
            if (existing != null && existing.getSubjectId() == s.getId()) {
                selectedSubIndex = i;
            }
        }
        ArrayAdapter<String> subAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, subNames);
        dBinding.actvTtSubject.setAdapter(subAdapter);
        if (!subNames.isEmpty()) {
            dBinding.actvTtSubject.setText(subNames.get(selectedSubIndex), false);
        }

        // Setup day dropdown
        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, DAYS);
        dBinding.actvTtDay.setAdapter(dayAdapter);
        int initialDayIndex = (existing != null) ? existing.getDayOfWeek() - 1 :
                (viewModel.getSelectedDay().getValue() != null && viewModel.getSelectedDay().getValue() > 0 ? viewModel.getSelectedDay().getValue() - 1 : 0);
        dBinding.actvTtDay.setText(DAYS[initialDayIndex], false);

        // Time pickers
        final LocalTime[] startTime = {existing != null ? existing.getStartTime() : LocalTime.of(9, 0)};
        final LocalTime[] endTime = {existing != null ? existing.getEndTime() : LocalTime.of(10, 0)};
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        dBinding.btnTtStartTime.setText("Start: " + startTime[0].format(timeFmt));
        dBinding.btnTtEndTime.setText("End: " + endTime[0].format(timeFmt));

        dBinding.btnTtStartTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(startTime[0].getHour())
                    .setMinute(startTime[0].getMinute())
                    .setTitleText("Select Start Time")
                    .build();
            picker.addOnPositiveButtonClickListener(pv -> {
                startTime[0] = LocalTime.of(picker.getHour(), picker.getMinute());
                dBinding.btnTtStartTime.setText("Start: " + startTime[0].format(timeFmt));
                checkOverlap(dBinding, initialDayIndex + 1, startTime[0], endTime[0], existing != null ? existing.getId() : 0);
            });
            picker.show(getParentFragmentManager(), "START_TIME_PICKER");
        });

        dBinding.btnTtEndTime.setOnClickListener(v -> {
            MaterialTimePicker picker = new MaterialTimePicker.Builder()
                    .setTimeFormat(TimeFormat.CLOCK_24H)
                    .setHour(endTime[0].getHour())
                    .setMinute(endTime[0].getMinute())
                    .setTitleText("Select End Time")
                    .build();
            picker.addOnPositiveButtonClickListener(pv -> {
                endTime[0] = LocalTime.of(picker.getHour(), picker.getMinute());
                dBinding.btnTtEndTime.setText("End: " + endTime[0].format(timeFmt));
                checkOverlap(dBinding, initialDayIndex + 1, startTime[0], endTime[0], existing != null ? existing.getId() : 0);
            });
            picker.show(getParentFragmentManager(), "END_TIME_PICKER");
        });

        if (existing != null) {
            dBinding.etTtRoom.setText(existing.getRoom() != null ? existing.getRoom() : "");
            dBinding.etTtFaculty.setText(existing.getFaculty() != null ? existing.getFaculty() : "");
            dBinding.cbTtCancelled.setChecked(existing.isCancelled());
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? R.string.add_session : R.string.edit_session)
                .setView(dBinding.getRoot())
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    int chosenSubPos = subNames.indexOf(dBinding.actvTtSubject.getText().toString());
                    if (chosenSubPos < 0 || chosenSubPos >= subjectList.size()) {
                        Toast.makeText(requireContext(), "Please select a valid subject", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long subId = subjectList.get(chosenSubPos).getId();

                    int dayOfWeek = 1;
                    String selectedDayText = dBinding.actvTtDay.getText().toString();
                    for (int i = 0; i < DAYS.length; i++) {
                        if (DAYS[i].equals(selectedDayText)) {
                            dayOfWeek = i + 1;
                            break;
                        }
                    }

                    String room = dBinding.etTtRoom.getText() != null ? dBinding.etTtRoom.getText().toString().trim() : "";
                    String faculty = dBinding.etTtFaculty.getText() != null ? dBinding.etTtFaculty.getText().toString().trim() : "";
                    boolean cancelled = dBinding.cbTtCancelled.isChecked();

                    TimetableSession session = (existing != null) ? existing : new TimetableSession(subId, dayOfWeek, startTime[0], endTime[0], room, faculty, cancelled);
                    session.setSubjectId(subId);
                    session.setDayOfWeek(dayOfWeek);
                    session.setStartTime(startTime[0]);
                    session.setEndTime(endTime[0]);
                    session.setRoom(room);
                    session.setFaculty(faculty);
                    session.setCancelled(cancelled);

                    viewModel.saveSession(session, () -> {
                        Toast.makeText(requireContext(), "Session saved", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void checkOverlap(DialogEditTimetableBinding dBinding, int dayOfWeek, LocalTime start, LocalTime end, long excludeId) {
        viewModel.checkOverlap(dayOfWeek, start, end, excludeId, hasOverlap -> {
            dBinding.tvTtOverlapWarning.setVisibility(hasOverlap ? View.VISIBLE : View.GONE);
        });
    }

    private void observeViewModel() {
        viewModel.getSubjects().observe(getViewLifecycleOwner(), subjects -> {
            if (subjects == null) return;
            subjectList.clear();
            subjectList.addAll(subjects);

            subjectMap.clear();
            for (Subject s : subjects) {
                subjectMap.put(s.getId(), s);
            }
            adapter.setSubjects(subjectMap);
        });

        viewModel.getFilteredSessions().observe(getViewLifecycleOwner(), sessions -> {
            if (sessions == null || sessions.isEmpty()) {
                binding.rvTimetableSessions.setVisibility(View.GONE);
                binding.tvEmptyTimetable.setVisibility(View.VISIBLE);
            } else {
                binding.rvTimetableSessions.setVisibility(View.VISIBLE);
                binding.tvEmptyTimetable.setVisibility(View.GONE);
                adapter.submitList(sessions);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
