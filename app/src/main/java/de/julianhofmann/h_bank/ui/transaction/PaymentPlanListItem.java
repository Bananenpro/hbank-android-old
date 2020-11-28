package de.julianhofmann.h_bank.ui.transaction;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import de.julianhofmann.h_bank.R;

public class PaymentPlanListItem extends ConstraintLayout {

    public PaymentPlanListItem(@NonNull Context context) {
        super(context);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.payment_plan_list_item, this, true);
    }

    public PaymentPlanListItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.payment_plan_list_item, this, true);
    }

    public PaymentPlanListItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.payment_plan_list_item, this, true);
    }

    public TextView getDescription() {
        return (TextView) getChildAt(4);
    }

    public TextView getSchedule() {
        return (TextView) getChildAt(3);
    }

    public TextView getAmount() {
        return (TextView) getChildAt(2);
    }

    public Button getButton() {
        return (Button) getChildAt(1);
    }
}
