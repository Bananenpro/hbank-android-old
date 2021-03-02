package de.julianhofmann.h_bank.ui.main.log;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.SizeModel;
import de.julianhofmann.h_bank.ui.main.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LogFragment extends Fragment {

    private final Handler refreshLogHandler = new Handler();
    private Runnable refreshLogRunnable;
    private boolean paused = false;
    private int logSize = -1;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_log, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity activity = (MainActivity) requireActivity();
        activity.resetLogPages();
        activity.loadLog();

        ScrollView scrollView = requireView().findViewById(R.id.log_scroll);


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

        refreshLogRunnable = () -> {
            if (!activity.isOffline()) {
                Call<SizeModel> call = RetrofitService.getHbankApi().getLogSize(RetrofitService.getAuthorization());
                call.enqueue(new Callback<SizeModel>() {
                    @Override
                    public void onResponse(@NotNull Call<SizeModel> call, @NotNull Response<SizeModel> response) {
                        activity.online();
                        if (response.isSuccessful()) {
                            if (response.body() != null && response.body().getSize() != logSize) {
                                logSize = response.body().getSize();
                                MainActivity activity = (MainActivity) requireActivity();
                                activity.resetLogPages();
                                activity.loadLog();
                            }
                        }
                        refreshLogHandler.postDelayed(refreshLogRunnable, 2000);
                    }

                    @Override
                    public void onFailure(@NotNull Call<SizeModel> call, @NotNull Throwable t) {
                        activity.offline();
                        refreshLogHandler.postDelayed(refreshLogRunnable, 2000);
                    }
                });
            } else {
                refreshLogHandler.postDelayed(refreshLogRunnable, 2000);
            }
        };
        refreshLogHandler.postDelayed(refreshLogRunnable, 2000);
    }

    @Override
    public void onPause() {
        super.onPause();
        refreshLogHandler.removeCallbacks(refreshLogRunnable);
        paused = true;
    }

    @Override
    public void onResume() {
        super.onResume();

        MainActivity activity = (MainActivity) requireActivity();

        Call<SizeModel> call = RetrofitService.getHbankApi().getLogSize(RetrofitService.getAuthorization());
        call.enqueue(new Callback<SizeModel>() {
            @Override
            public void onResponse(@NotNull Call<SizeModel> call, @NotNull Response<SizeModel> response) {
                activity.online();
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        logSize = response.body().getSize();
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<SizeModel> call, @NotNull Throwable t) {
                activity.offline();
            }
        });

        if (paused) {
            activity.resetLogPages();
            activity.loadLog();

            refreshLogHandler.postDelayed(refreshLogRunnable, 2000);
            paused = false;
        }
    }
}