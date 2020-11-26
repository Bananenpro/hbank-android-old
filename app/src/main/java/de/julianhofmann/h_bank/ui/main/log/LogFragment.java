package de.julianhofmann.h_bank.ui.main.log;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import de.julianhofmann.h_bank.ui.main.MainActivity;
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