package com.bluestone.attendance.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bluestone.attendance.R;
import com.bluestone.attendance.data.entities.Student;
import com.bluestone.attendance.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;
    private Student currentStudent;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        observeStudent();
        setupSaveButton();
    }

    private void observeStudent() {
        viewModel.getStudent().observe(getViewLifecycleOwner(), student -> {
            if (student == null) return;
            currentStudent = student;

            binding.etProfId.setText(student.getStudentId());
            binding.etProfName.setText(student.getFullName());
            binding.etProfEmail.setText(student.getEmail() != null ? student.getEmail() : "");
            binding.etProfCourse.setText(student.getCourse() != null ? student.getCourse() : "");
            binding.etProfSemester.setText(String.valueOf(student.getSemester()));
            binding.etProfAcademicYear.setText(student.getAcademicYear() != null ? student.getAcademicYear() : "");
            binding.etProfCollege.setText(student.getCollege() != null ? student.getCollege() : "");
        });
    }

    private void setupSaveButton() {
        binding.btnSaveProfile.setOnClickListener(v -> {
            if (currentStudent == null) return;

            String name = binding.etProfName.getText() != null
                    ? binding.etProfName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                binding.tilProfName.setError("Name cannot be empty");
                return;
            }
            binding.tilProfName.setError(null);

            String email = binding.etProfEmail.getText() != null
                    ? binding.etProfEmail.getText().toString().trim() : "";
            String course = binding.etProfCourse.getText() != null
                    ? binding.etProfCourse.getText().toString().trim() : "";

            int sem = currentStudent.getSemester();
            try {
                sem = Integer.parseInt(binding.etProfSemester.getText().toString());
            } catch (Exception ignored) {}

            String year = binding.etProfAcademicYear.getText() != null
                    ? binding.etProfAcademicYear.getText().toString().trim() : "";
            String college = binding.etProfCollege.getText() != null
                    ? binding.etProfCollege.getText().toString().trim() : "";

            currentStudent.setFullName(name);
            currentStudent.setEmail(email);
            currentStudent.setCourse(course);
            currentStudent.setSemester(sem);
            currentStudent.setAcademicYear(year);
            currentStudent.setCollege(college);

            viewModel.updateProfile(currentStudent, () -> {
                Toast.makeText(requireContext(), R.string.profile_saved, Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
