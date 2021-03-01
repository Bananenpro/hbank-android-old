package de.julianhofmann.h_bank.ui.tools;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Calendar;

import de.julianhofmann.h_bank.R;
import de.julianhofmann.h_bank.api.RetrofitService;
import de.julianhofmann.h_bank.api.models.CalculateModel;
import de.julianhofmann.h_bank.ui.BaseActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CalculatorActivity extends BaseActivity {

    private boolean dialog = false;
    private boolean afterCalculate = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init(R.layout.activity_calculator);

        Spinner dropdown = findViewById(R.id.calculate_time_unit);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.schedule_units, R.layout.support_simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);

        EditText date = findViewById(R.id.calculate_date);
        EditText time = findViewById(R.id.calculate_time);
        EditText money = findViewById(R.id.calculate_money);

        date.setOnTouchListener((v, event) -> {
            if (date.isEnabled()) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!dialog) {
                        if (date.getText().length() > 0 && event.getX() >= date.getWidth() - date.getTotalPaddingRight()) {
                            date.setText("");
                            date.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                            time.setEnabled(true);
                            money.setEnabled(true);
                            if (afterCalculate) {
                                afterCalculate = false;
                                time.setText("");
                                money.setText("");
                            }
                        } else {
                            pickDate();
                        }
                    }
                }
            }
            return true;
        });

        time.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (time.isEnabled()) {
                    if (s.length() > 0) {

                        String unit = dropdown.getSelectedItem().toString();

                        int t = Integer.parseInt(s.toString());

                        if (unit.equals(getString(R.string.days)) && t > 365 * 5 + 1) {
                            time.setText(Integer.toString(365 * 5 + 1));
                        } else if (unit.equals(getString(R.string.weeks)) && t > 52 * 5) {
                            time.setText(Integer.toString(52 * 5));
                        } else if (unit.equals(getString(R.string.months)) && t > 12 * 5) {
                            time.setText(Integer.toString(12 * 5));
                        } else if (unit.equals(getString(R.string.years)) && t > 5) {
                            time.setText(Integer.toString(5));
                        }

                        date.setEnabled(false);
                        date.setText("");
                        date.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                        money.setEnabled(false);
                        money.setText("");
                    } else {
                        date.setEnabled(true);
                        money.setEnabled(true);
                    }

                    if (afterCalculate) {
                        afterCalculate = false;
                        date.setText("");
                        date.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                        money.setText("");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        money.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (money.isEnabled()) {
                    if (s.length() > 0) {
                        date.setEnabled(false);
                        date.setText("");
                        date.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                        time.setEnabled(false);
                        time.setText("");
                    } else {
                        date.setEnabled(true);
                        time.setEnabled(true);
                    }
                    if (afterCalculate) {
                        afterCalculate = false;
                        date.setText("");
                        date.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                        time.setText("");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String unit = dropdown.getSelectedItem().toString();

                if (time.getText().length() > 0) {
                    int t = Integer.parseInt(time.getText().toString());

                    if (unit.equals(getString(R.string.days)) && t > 365 * 5 + 1) {
                        time.setText(Integer.toString(365 * 5 + 1));
                    } else if (unit.equals(getString(R.string.weeks)) && t > 52 * 5) {
                        time.setText(Integer.toString(52 * 5));
                    } else if (unit.equals(getString(R.string.months)) && t > 12 * 5) {
                        time.setText(Integer.toString(12 * 5));
                    } else if (unit.equals(getString(R.string.years)) && t > 5) {
                        time.setText(Integer.toString(5));
                    }

                    date.setEnabled(false);
                    date.setText("");
                    date.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                    money.setEnabled(false);
                    money.setText("");
                }

                if (afterCalculate) {
                    afterCalculate = false;
                    date.setText("");
                    date.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
                    time.setText("");
                    money.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        date.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
    }

    public void calculate(View v) {
        EditText date = findViewById(R.id.calculate_date);
        EditText time = findViewById(R.id.calculate_time);
        Spinner dropdown = findViewById(R.id.calculate_time_unit);
        EditText money = findViewById(R.id.calculate_money);
        Button button = findViewById(R.id.calculate_btn);

        String unit = "";
        if (dropdown.getSelectedItem().toString().equals(getString(R.string.days))) unit = "days";
        else if (dropdown.getSelectedItem().toString().equals(getString(R.string.weeks))) unit = "weeks";
        else if (dropdown.getSelectedItem().toString().equals(getString(R.string.months))) unit = "months";
        else if (dropdown.getSelectedItem().toString().equals(getString(R.string.years))) unit = "years";

        Call<CalculateModel> call = RetrofitService.getHbankApi().calculate(date.getText().toString(), time.getText().toString(), unit, money.getText().toString(), RetrofitService.getAuthorization());
        button.setText(R.string.loading);
        button.setEnabled(false);
        call.enqueue(new Callback<CalculateModel>() {
            @Override
            public void onResponse(@NotNull Call<CalculateModel> call, @NotNull Response<CalculateModel> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        date.setEnabled(false);
                        time.setEnabled(false);
                        money.setEnabled(false);

                        date.setText(response.body().getDate());
                        date.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_cancel_24, 0);
                        date.setEnabled(true);
                        time.setText(response.body().getDeltatime());
                        time.setEnabled(true);
                        dropdown.setSelection(Arrays.asList(getResources().getStringArray(R.array.schedule_units)).indexOf(response.body().getDeltaunit()));
                        money.setText(response.body().getBalance());
                        money.setEnabled(true);
                        afterCalculate = true;
                    }
                } else if (response.code() == 403) {
                    String name = RetrofitService.getName();
                    RetrofitService.logout();
                    switchToLoginActivity(name);
                }
                button.setText(R.string.calculate);
                button.setEnabled(true);
            }

            @Override
            public void onFailure(@NotNull Call<CalculateModel> call, @NotNull Throwable t) {
                Toast.makeText(CalculatorActivity.this, getString(R.string.cannot_reach_server), Toast.LENGTH_SHORT).show();
                button.setText(R.string.calculate);
                button.setEnabled(true);
            }
        });
    }

    public void pickDate() {
        dialog = true;

        EditText date = findViewById(R.id.calculate_date);
        EditText time = findViewById(R.id.calculate_time);
        EditText money = findViewById(R.id.calculate_money);

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog picker = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            afterCalculate = false;
            month1++;

            String dayOfMonthStr = dayOfMonth < 10 ? "0" + dayOfMonth : Integer.toString(dayOfMonth);
            String monthStr = month1 < 10 ? "0" + month1 : Integer.toString(month1);

            String dateString = dayOfMonthStr + "." + (monthStr) + "." + year1;
            date.setText(dateString);
            time.setEnabled(false);
            time.setText("");
            money.setEnabled(false);
            money.setText("");
            dialog = false;
            date.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_baseline_cancel_24, 0);
        }, year, month, day);
        picker.setOnCancelListener(d -> dialog = false);
        picker.setOnDismissListener(d -> dialog = false);

        Calendar min = Calendar.getInstance();
        min.add(Calendar.DAY_OF_MONTH, 1);
        picker.getDatePicker().setMinDate(min.getTimeInMillis());

        Calendar max = Calendar.getInstance();
        max.add(Calendar.YEAR, 5);
        picker.getDatePicker().setMaxDate(max.getTimeInMillis());

        picker.show();
    }
}