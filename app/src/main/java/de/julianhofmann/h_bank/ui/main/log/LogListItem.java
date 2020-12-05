package de.julianhofmann.h_bank.ui.main.log;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import de.julianhofmann.h_bank.R;

public class LogListItem extends ConstraintLayout {

    public LogListItem(@NonNull Context context) {
        super(context);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.log_list_item, this, true);
    }

    public LogListItem(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.log_list_item, this, true);
    }

    public LogListItem(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.log_list_item, this, true);
    }

    public Button getButton() {
        return (Button) getChildAt(0);
    }

    public TextView getDate() {
        return (TextView) getChildAt(1);
    }

    public TextView getDescription() {
        return (TextView) getChildAt(2);
    }

    public TextView getAmount() {
        return (TextView) getChildAt(3);
    }
}
