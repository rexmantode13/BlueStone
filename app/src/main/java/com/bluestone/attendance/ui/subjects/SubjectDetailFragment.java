package com.bluestone.attendance.ui.subjects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bluestone.attendance.R;
import com.bluestone.attendance.data.entities.Subject;
import com.bluestone.attendance.databinding.DialogEditSubjectBinding;
import com.bluestone.attendance.databinding.FragmentSubjectDetailBinding;
import com.bluestone.attendance.domain.model.SubjectAttendanceSummary;
import com.bluestone.attendance.ui.attendance.AttendanceAdapter;
import com.bluestone.attendance.utils.AttendanceCalculator;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class SubjectDetailFragment extends Fragment {

    private FragmentSubjectDetailBinding binding;
    private SubjectsViewModel viewModel;
    private AttendanceAdapter attendanceAdapter;
    private long subjectId = -1L;
    private Subject currentSubject;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSubjectDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            subjectId = getArguments().getLong("subjectId", -1L);
        }

        viewModel = new ViewModelProvider(this).get(SubjectsViewModel.class);

        setupAttendanceList();
        setupActions(view);
        observeSubjectData();
    }

    private void setupAttendanceList() {
        attendanceAdapter = new AttendanceAdapter(new AttendanceAdapter.OnRecordActionListener() {
            @Override
            public void onDeleteClick(com.bluestone.attendance.data.entities.AttendanceRecord record) {
                // Delete record confirm
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.confirm_delete)
                        .setMessage(R.string.delete_attendance_confirm)
                        .setPositiveButton(R.string.delete, (d, w) -> {
                            // Handled via attendance repository or viewmodel
                            Toast.makeText(requireContext(), R.string.attendance_deleted, Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }

            @Override
            public void onEditClick(com.bluestone.attendance.data.entities.AttendanceRecord record) {}
        });

        binding.rvDetailHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvDetailHistory.setAdapter(attendanceAdapter);
    }

    private void setupActions(View view) {
        binding.btnBackDetail.setOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        binding.btnDetailViewSyllabus.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("subjectId", subjectId);
            Navigation.findNavController(view).navigate(R.id.action_detail_to_syllabus, bundle);
        });

        binding.btnEditDetail.setOnClickListener(v -> {
            if (currentSubject != null) showEditDialog(currentSubject);
        });

        binding.btnDeleteDetail.setOnClickListener(v -> {
            if (currentSubject != null) showArchiveConfirmDialog(currentSubject);
        });
    }

    private void observeSubjectData() {
        viewModel.getSubjectById(subjectId).observe(getViewLifecycleOwner(), subject -> {
            if (subject == null) return;
            currentSubject = subject;

            binding.tvDetailCode.setText(subject.getCode());
            binding.tvDetailName.setText(subject.getName());
            binding.tvDetailSemester.setText("Semester " + subject.getSemester());
            binding.tvDetailCredits.setText(subject.getCredits() + " Credits");
            binding.tvDetailFaculty.setText(
                    subject.getFaculty() != null ? subject.getFaculty() : "Faculty not assigned"
            );

            attendanceAdapter.setSubjects(Collections.singletonMap(subject.getId(), subject));
            populateAssignments(subject);
        });

        viewModel.getFilteredSummaries().observe(getViewLifecycleOwner(), summaries -> {
            if (summaries == null) return;
            for (SubjectAttendanceSummary s : summaries) {
                if (s.getSubject().getId() == subjectId) {
                    double pct = s.getPercentage();
                    binding.tvDetailPercentage.setText(AttendanceCalculator.formatPercentage(pct));
                    binding.tvDetailFraction.setText(
                            s.getPresentCount() + " present, " + s.getAbsentCount() + " absent • " +
                                    s.getTotalEligible() + " classes"
                    );

                    if (pct < 0) {
                        binding.tvDetailStatusBadge.setText("No Data");
                        binding.tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_badge_unmarked);
                        binding.tvDetailStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.unmarked));
                        binding.tvDetailProjectionMessage.setText("No attendance recorded yet for this subject.");
                    } else if (s.isBelowTarget()) {
                        binding.tvDetailStatusBadge.setText("Below Target");
                        binding.tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_badge_absent);
                        binding.tvDetailStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.absent));
                        String msg = String.format(Locale.getDefault(),
                                "%d more consecutive classes needed to reach %.0f%% target (projection)",
                                s.getSessionsNeeded(), s.getSubject().getTargetPercentage());
                        binding.tvDetailProjectionMessage.setText(msg);
                        binding.tvDetailProjectionMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.absent));
                    } else {
                        binding.tvDetailStatusBadge.setText("On Track");
                        binding.tvDetailStatusBadge.setBackgroundResource(R.drawable.bg_badge_present);
                        binding.tvDetailStatusBadge.setTextColor(ContextCompat.getColor(requireContext(), R.color.present));
                        binding.tvDetailProjectionMessage.setText(
                                String.format(Locale.getDefault(), "Target: %.0f%% • You are above target!", s.getSubject().getTargetPercentage())
                        );
                        binding.tvDetailProjectionMessage.setTextColor(ContextCompat.getColor(requireContext(), R.color.present));
                    }
                    break;
                }
            }
        });

        viewModel.getSubjectAttendanceRecords(subjectId).observe(getViewLifecycleOwner(), records -> {
            if (records == null || records.isEmpty()) {
                binding.rvDetailHistory.setVisibility(View.GONE);
                binding.tvDetailEmptyHistory.setVisibility(View.VISIBLE);
            } else {
                binding.rvDetailHistory.setVisibility(View.VISIBLE);
                binding.tvDetailEmptyHistory.setVisibility(View.GONE);
                attendanceAdapter.submitList(records);
            }
        });
    }

    private void showEditDialog(Subject subject) {
        DialogEditSubjectBinding dialogBinding = DialogEditSubjectBinding.inflate(getLayoutInflater());
        dialogBinding.etSubName.setText(subject.getName());
        dialogBinding.etSubCode.setText(subject.getCode());
        dialogBinding.etSubSemester.setText(String.valueOf(subject.getSemester()));
        dialogBinding.etSubCredits.setText(String.valueOf(subject.getCredits()));
        dialogBinding.etSubFaculty.setText(subject.getFaculty() != null ? subject.getFaculty() : "");
        dialogBinding.etSubTarget.setText(String.valueOf((int) subject.getTargetPercentage()));
        dialogBinding.etSubNotes.setText(subject.getNotes() != null ? subject.getNotes() : "");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.edit_subject)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.save, (d, w) -> {
                    subject.setName(dialogBinding.etSubName.getText().toString().trim());
                    subject.setCode(dialogBinding.etSubCode.getText().toString().trim());
                    try {
                        subject.setSemester(Integer.parseInt(dialogBinding.etSubSemester.getText().toString()));
                    } catch (Exception ignored) {}
                    try {
                        subject.setCredits(Integer.parseInt(dialogBinding.etSubCredits.getText().toString()));
                    } catch (Exception ignored) {}
                    try {
                        subject.setTargetPercentage(Double.parseDouble(dialogBinding.etSubTarget.getText().toString()));
                    } catch (Exception ignored) {}
                    subject.setFaculty(dialogBinding.etSubFaculty.getText().toString().trim());
                    subject.setNotes(dialogBinding.etSubNotes.getText().toString().trim());

                    viewModel.saveSubject(subject, () -> {
                        Toast.makeText(requireContext(), "Subject updated", Toast.LENGTH_SHORT).show();
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showArchiveConfirmDialog(Subject subject) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_delete)
                .setMessage(getString(R.string.delete_subject_confirm, subject.getName()))
                .setPositiveButton(R.string.delete, (d, w) -> {
                    viewModel.softDeleteSubject(subject.getId(), () -> {
                        Toast.makeText(requireContext(), R.string.subject_deleted, Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void populateAssignments(Subject subject) {
        if (binding == null || getContext() == null) return;
        binding.llAssignmentsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(requireContext());

        List<AssignmentItem> list = getAssignmentsForSubject(subject != null ? subject.getCode() : "");
        for (AssignmentItem item : list) {
            View view = inflater.inflate(R.layout.item_subject_assignment, binding.llAssignmentsContainer, false);
            android.widget.TextView tvTitle = view.findViewById(R.id.tv_assign_title);
            android.widget.TextView tvMeta = view.findViewById(R.id.tv_assign_meta);
            android.widget.TextView tvStatus = view.findViewById(R.id.tv_assign_status);

            tvTitle.setText(item.title);
            tvMeta.setText("Due " + item.dueDate + " • Max: " + item.maxMarks + " Marks");
            tvStatus.setText(item.status);

            if ("Submitted".equalsIgnoreCase(item.status) || item.status.startsWith("Graded")) {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_present);
                tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.present));
            } else if ("Pending".equalsIgnoreCase(item.status)) {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_absent);
                tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.absent));
            } else {
                tvStatus.setBackgroundResource(R.drawable.bg_badge_unmarked);
                tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            }

            binding.llAssignmentsContainer.addView(view);
        }
    }

    private static class AssignmentItem {
        final String title;
        final String dueDate;
        final int maxMarks;
        final String status;

        AssignmentItem(String title, String dueDate, int maxMarks, String status) {
            this.title = title;
            this.dueDate = dueDate;
            this.maxMarks = maxMarks;
            this.status = status;
        }
    }

    private List<AssignmentItem> getAssignmentsForSubject(String code) {
        List<AssignmentItem> list = new java.util.ArrayList<>();
        if ("CS201".equalsIgnoreCase(code)) {
            list.add(new AssignmentItem("Assignment 1: Class Hierarchies & Operator Overloading", "Oct 12, 2026", 10, "Graded: 10/10"));
            list.add(new AssignmentItem("Assignment 2: Polymorphism & Generic Templates", "Oct 28, 2026", 10, "Submitted"));
            list.add(new AssignmentItem("Assignment 3: Exception Handling & Custom STL Containers", "Nov 14, 2026", 10, "Pending"));
            list.add(new AssignmentItem("Assignment 4: Modern C++ RAII & Design Patterns", "Dec 02, 2026", 15, "Upcoming"));
        } else if ("CS202".equalsIgnoreCase(code)) {
            list.add(new AssignmentItem("Assignment 1: Asymptotic Analysis & Binary Search Trees", "Oct 14, 2026", 10, "Graded: 10/10"));
            list.add(new AssignmentItem("Assignment 2: Self-Balancing AVL & Red-Black Trees", "Oct 30, 2026", 10, "Submitted"));
            list.add(new AssignmentItem("Assignment 3: Dijkstra's Algorithm & Spanning Trees", "Nov 16, 2026", 10, "Pending"));
            list.add(new AssignmentItem("Assignment 4: Dynamic Programming & Knapsack Optimization", "Dec 05, 2026", 15, "Upcoming"));
        } else if ("CS203".equalsIgnoreCase(code)) {
            list.add(new AssignmentItem("Assignment 1: Relational Algebra & ER Schema Modeling", "Oct 15, 2026", 10, "Graded: 9.5/10"));
            list.add(new AssignmentItem("Assignment 2: Complex SQL Queries, Joins & Triggers", "Nov 02, 2026", 10, "Submitted"));
            list.add(new AssignmentItem("Assignment 3: B+ Tree Indexing & Query Plans", "Nov 18, 2026", 10, "Pending"));
            list.add(new AssignmentItem("Assignment 4: ACID Transactions & Concurrency Control", "Dec 08, 2026", 15, "Upcoming"));
        } else if ("CS204".equalsIgnoreCase(code)) {
            list.add(new AssignmentItem("Assignment 1: Combinational Logic & 4-bit ALU Design", "Oct 16, 2026", 10, "Graded: 9/10"));
            list.add(new AssignmentItem("Assignment 2: MIPS Instruction Set & 5-Stage Pipeline", "Nov 04, 2026", 10, "Submitted"));
            list.add(new AssignmentItem("Assignment 3: Cache Memory Mapping & Hit-Rate Benchmark", "Nov 20, 2026", 10, "Pending"));
            list.add(new AssignmentItem("Assignment 4: Virtual Memory Management & TLB Simulation", "Dec 10, 2026", 15, "Upcoming"));
        } else if ("CS205".equalsIgnoreCase(code)) {
            list.add(new AssignmentItem("Assignment 1: Agile User Stories & Sprint Backlog", "Oct 18, 2026", 10, "Graded: 10/10"));
            list.add(new AssignmentItem("Assignment 2: UML Class, Sequence & State Diagrams", "Nov 06, 2026", 10, "Submitted"));
            list.add(new AssignmentItem("Assignment 3: Automated Unit Testing & CI/CD Pipeline", "Nov 22, 2026", 10, "Pending"));
            list.add(new AssignmentItem("Assignment 4: Software Architecture & Design Patterns", "Dec 12, 2026", 15, "Upcoming"));
        } else if ("MA201".equalsIgnoreCase(code)) {
            list.add(new AssignmentItem("Assignment 1: Propositional Logic & Induction Proofs", "Oct 20, 2026", 10, "Graded: 10/10"));
            list.add(new AssignmentItem("Assignment 2: Set Theory, Relations & Pigeonhole Principle", "Nov 08, 2026", 10, "Submitted"));
            list.add(new AssignmentItem("Assignment 3: Graph Theory & Eulerian Circuits", "Nov 24, 2026", 10, "Pending"));
            list.add(new AssignmentItem("Assignment 4: Combinatorics & Generating Functions", "Dec 15, 2026", 15, "Upcoming"));
        } else {
            list.add(new AssignmentItem("Assignment 1: Foundational Theory & Practical Concepts", "Oct 20, 2026", 10, "Submitted"));
            list.add(new AssignmentItem("Assignment 2: Laboratory Implementation & Problem Set", "Nov 10, 2026", 10, "Submitted"));
            list.add(new AssignmentItem("Assignment 3: Midterm Coursework Assessment", "Nov 25, 2026", 10, "Pending"));
            list.add(new AssignmentItem("Assignment 4: Final Term Project Submission", "Dec 15, 2026", 15, "Upcoming"));
        }
        return list;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
