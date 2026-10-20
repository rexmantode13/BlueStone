package com.bluestone.attendance.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bluestone.attendance.R;
import com.bluestone.attendance.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private LoginViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        binding.btnLogin.setOnClickListener(v -> {
            String studentId = binding.etStudentId.getText() != null ? binding.etStudentId.getText().toString() : "";
            String password = binding.etPassword.getText() != null ? binding.etPassword.getText().toString() : "";
            viewModel.login(studentId, password);
        });

        binding.cardDemoBoy1.setOnClickListener(v -> {
            binding.etStudentId.setText("CS2026001");
            binding.etPassword.setText("Blue@123");
            viewModel.login("CS2026001", "Blue@123");
        });

        binding.cardDemoBoy2.setOnClickListener(v -> {
            binding.etStudentId.setText("CS2026002");
            binding.etPassword.setText("Blue@123");
            viewModel.login("CS2026002", "Blue@123");
        });

        binding.cardDemoGirl1.setOnClickListener(v -> {
            binding.etStudentId.setText("CS2026003");
            binding.etPassword.setText("Blue@123");
            viewModel.login("CS2026003", "Blue@123");
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            binding.pbLogin.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnLogin.setEnabled(!loading);
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                binding.tvLoginError.setText(error);
                binding.tvLoginError.setVisibility(View.VISIBLE);
            } else {
                binding.tvLoginError.setVisibility(View.GONE);
            }
        });

        viewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Navigation.findNavController(view).navigate(R.id.action_login_to_dashboard);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
