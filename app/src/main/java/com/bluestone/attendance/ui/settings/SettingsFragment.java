package com.bluestone.attendance.ui.settings;

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

import com.bluestone.attendance.R;
import com.bluestone.attendance.databinding.FragmentSettingsBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        setupThresholdSlider();
        setupThemeToggle();
        setupDataActions(view);
        observeSettings();
    }

    private void setupThresholdSlider() {
        binding.sliderThreshold.addOnChangeListener((slider, value, fromUser) -> {
            binding.tvThresholdValue.setText((int) value + "%");
            if (fromUser) {
                viewModel.updateThreshold(value);
            }
        });
    }

    private void setupThemeToggle() {
        binding.toggleTheme.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            if (checkedId == R.id.btn_theme_light) {
                viewModel.setDarkMode(1);
            } else if (checkedId == R.id.btn_theme_dark) {
                viewModel.setDarkMode(2);
            } else {
                viewModel.setDarkMode(0);
            }
        });
    }

    private void setupDataActions(View view) {
        binding.btnResetDemoData.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.reset_demo_data)
                    .setMessage(R.string.reset_confirm)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        viewModel.resetDemoData(() -> {
                            Toast.makeText(requireContext(), R.string.data_reset, Toast.LENGTH_SHORT).show();
                        });
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        binding.btnLogout.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.logout)
                    .setMessage(R.string.logout_confirm)
                    .setPositiveButton(R.string.logout, (dialog, which) -> {
                        viewModel.logout();
                        Navigation.findNavController(view).navigate(R.id.action_settings_to_login);
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
    }

    private void observeSettings() {
        viewModel.getSettings().observe(getViewLifecycleOwner(), settings -> {
            if (settings == null) return;

            binding.sliderThreshold.setValue((float) settings.getAttendanceThreshold());
            binding.tvThresholdValue.setText((int) settings.getAttendanceThreshold() + "%");

            if (settings.getDarkMode() == 1) {
                binding.toggleTheme.check(R.id.btn_theme_light);
            } else if (settings.getDarkMode() == 2) {
                binding.toggleTheme.check(R.id.btn_theme_dark);
            } else {
                binding.toggleTheme.check(R.id.btn_theme_system);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
