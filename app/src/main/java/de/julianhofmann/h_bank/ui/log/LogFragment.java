package de.julianhofmann.h_bank.ui.log;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import de.julianhofmann.h_bank.MainActivity;
import de.julianhofmann.h_bank.R;

public class LogFragment extends Fragment {

    private boolean paused = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        activity.resetLogPages();
        activity.loadLog();

        ScrollView scrollView = getView().findViewById(R.id.log_scroll);


        if (scrollView.getChildAt(0).getBottom() <= (scrollView.getHeight() + scrollView.getScrollY() + 50)) {
            activity.loadLog();
        }

        scrollView.getViewTreeObserver()
                .addOnScrollChangedListener(() -> {
                    if (scrollView.getChildAt(0).getBottom()
                            <= (scrollView.getHeight() + scrollView.getScrollY() + 50)) {
                        activity.loadLog();
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(paused) {
            MainActivity activity = (MainActivity) getActivity();
            activity.resetLogPages();
            activity.loadLog();
            paused = false;
        }
    }
}