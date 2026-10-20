package com.bluestone.attendance;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bluestone.attendance.data.repository.AuthRepository;
import com.bluestone.attendance.data.repository.LocalAuthRepository;
import com.bluestone.attendance.databinding.ActivityMainBinding;
import com.bluestone.attendance.domain.model.WindowSizeClass;
import com.bluestone.attendance.utils.WindowSizeHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AuthRepository authRepository;
    private WindowSizeClass currentSizeClass = WindowSizeClass.COMPACT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle Android 12+ SplashScreen
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = new LocalAuthRepository(this);

        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHost != null) {
            navController = navHost.getNavController();
        }

        setupWindowInsets();
        setupNavigation();
        updateNavigationForWindowSize();

        // Route guard
        if (navController != null) {
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                boolean isLogin = destination.getId() == R.id.loginFragment;
                if (!isLogin && !authRepository.isLoggedIn()) {
                    controller.navigate(R.id.loginFragment);
                    return;
                }

                // Update toolbar title
                if (destination.getLabel() != null) {
                    binding.topAppBar.setTitle(destination.getLabel());
                }

                // Show/hide navigation controls based on auth state
                if (isLogin) {
                    binding.topAppBar.setVisibility(View.GONE);
                    binding.bottomNavigation.setVisibility(View.GONE);
                    binding.navigationRail.setVisibility(View.GONE);
                    binding.sideNavigation.setVisibility(View.GONE);
                } else {
                    boolean isDashboard = destination.getId() == R.id.dashboardFragment;
                    binding.topAppBar.setVisibility(isDashboard ? View.GONE : View.VISIBLE);
                    updateNavigationForWindowSize();
                }
            });

            // If already logged in, navigate straight to dashboard
            if (authRepository.isLoggedIn()) {
                navController.navigate(R.id.action_login_to_dashboard);
            }
        }
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, windowInsets) -> {
            Insets insets = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
            );

            // Apply top inset to app bar
            binding.topAppBar.setPadding(0, insets.top, 0, 0);

            // Apply bottom inset to bottom nav if visible
            if (binding.bottomNavigation.getVisibility() == View.VISIBLE) {
                binding.bottomNavigation.setPadding(0, 0, 0, insets.bottom);
            }

            return WindowInsetsCompat.CONSUMED;
        });
    }

    private void setupNavigation() {
        if (navController == null) return;

        // Bottom Nav
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

        // Navigation Rail
        binding.navigationRail.setOnItemSelectedListener(item -> {
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        // Side Navigation (NavigationView)
        binding.sideNavigation.setNavigationItemSelectedListener(item -> {
            return NavigationUI.onNavDestinationSelected(item, navController);
        });

        // Top App Bar menu / profile shortcut
        binding.topAppBar.setOnMenuItemClickListener(this::onOptionsItemSelected);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateNavigationForWindowSize();
    }

    private void updateNavigationForWindowSize() {
        if (navController == null) return;
        NavDestination currentDest = navController.getCurrentDestination();
        if (currentDest != null && currentDest.getId() == R.id.loginFragment) {
            binding.bottomNavigation.setVisibility(View.GONE);
            binding.navigationRail.setVisibility(View.GONE);
            binding.sideNavigation.setVisibility(View.GONE);
            return;
        }

        currentSizeClass = WindowSizeHelper.computeWindowSizeClass(this);

        switch (currentSizeClass) {
            case EXPANDED:
                binding.sideNavigation.setVisibility(View.VISIBLE);
                binding.navigationRail.setVisibility(View.GONE);
                binding.bottomNavigation.setVisibility(View.GONE);
                break;
            case MEDIUM:
                binding.sideNavigation.setVisibility(View.GONE);
                binding.navigationRail.setVisibility(View.VISIBLE);
                binding.bottomNavigation.setVisibility(View.GONE);
                break;
            case COMPACT:
            default:
                binding.sideNavigation.setVisibility(View.GONE);
                binding.navigationRail.setVisibility(View.GONE);
                binding.bottomNavigation.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Desktop / Keyboard shortcuts: Esc, Ctrl+N, Ctrl+F
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ESCAPE) {
                if (navController != null && navController.popBackStack()) {
                    return true;
                }
            } else if (event.isCtrlPressed()) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_N) {
                    // Navigate to quick add attendance
                    if (navController != null) {
                        navController.navigate(R.id.attendanceFragment);
                        return true;
                    }
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_F) {
                    // Quick search in subjects
                    if (navController != null) {
                        navController.navigate(R.id.subjectsFragment);
                        return true;
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public WindowSizeClass getCurrentSizeClass() {
        return currentSizeClass;
    }
}
