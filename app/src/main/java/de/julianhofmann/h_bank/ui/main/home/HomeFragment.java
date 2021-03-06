package de.julianhofmann.h_bank.ui.main.home;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;

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

        EditText cash = requireView().findViewById(R.id.cash_input);
        cash.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                ((MainActivity) requireActivity()).updateCash();
            }

            return false;
        });

        new Handler().postDelayed(() -> {
            if (((TextView) getView().findViewById(R.id.user_name_lbl)).getText().length() == 0) {
                ((MainActivity) requireActivity()).loadUserInfo(null);
            }
        }, 200);

        FloatingActionButton paymentPlanBtn = getView().findViewById(R.id.home_payment_plan_btn);

        DisplayMetrics metrics = getResources().getDisplayMetrics();


        if (metrics.heightPixels / metrics.density < 700) {
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.WRAP_CONTENT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(toDp(24), toDp(0), toDp(0), toDp(80));
            paymentPlanBtn.setLayoutParams(params);

            ConstraintLayout layout = getView().findViewById(R.id.home_constraint_layout);
            ConstraintSet set = new ConstraintSet();
            set.clone(layout);
            set.connect(R.id.home_payment_plan_btn, ConstraintSet.START, R.id.home_constraint_layout, ConstraintSet.START);
            set.connect(R.id.home_payment_plan_btn, ConstraintSet.BOTTOM, R.id.home_constraint_layout, ConstraintSet.BOTTOM);
            set.clear(R.id.home_payment_plan_btn, ConstraintSet.END);
            set.applyTo(layout);
        }
    }

    private int toDp(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
        refreshBalanceHandler.removeCallbacks(refreshBalanceRunnable);
        ((MainActivity) requireActivity()).updateCash();
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