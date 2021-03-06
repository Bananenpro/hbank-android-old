package de.julianhofmann.h_bank.ui.main.home;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.ui.main.MainActivity;
import de.julianhofmann.h_bank.util.SettingsService;

public class HomeFragment extends Fragment {

    private final Handler refreshBalanceHandler = new Handler();
    private boolean paused = false;
    private Runnable refreshBalanceRunnable;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        refreshBalanceRunnable = new Runnable() {
            @Override
            public void run() {
                ((MainActivity) requireActivity()).loadUserInfo(null);
                refreshBalanceHandler.postDelayed(this, SettingsService.getAutoRefreshInterval());
            }
        };

        ((MainActivity) requireActivity()).loadUserInfo(null);
        if (SettingsService.getAutoRefresh()) {
            refreshBalanceHandler.postDelayed(refreshBalanceRunnable, SettingsService.getAutoRefreshInterval());
        }

        FloatingActionButton refresh = requireView().findViewById(R.id.user_refresh_button);
        refresh.setVisibility(SettingsService.getAutoRefresh() ? View.GONE : View.VISIBLE);

        new Handler().postDelayed(() -> {
            if (((TextView) getView().findViewById(R.id.user_name_lbl)).getText().length() == 0) {
                ((MainActivity) requireActivity()).loadUserInfo(null);
            }
        }, 200);
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
        refreshBalanceHandler.removeCallbacks(refreshBalanceRunnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (paused) {
            ((MainActivity) requireActivity()).loadUserInfo(null);
            if (SettingsService.getAutoRefresh()) {
                refreshBalanceHandler.postDelayed(refreshBalanceRunnable, SettingsService.getAutoRefreshInterval());
            }
            FloatingActionButton refresh = requireView().findViewById(R.id.user_refresh_button);
            refresh.setVisibility(SettingsService.getAutoRefresh() ? View.GONE : View.VISIBLE);
            paused = false;
        }
    }
}