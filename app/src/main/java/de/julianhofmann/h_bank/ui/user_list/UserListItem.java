package de.julianhofmann.h_bank.ui.user_list;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import de.julianhofmann.h_bank.R;

public class UserListItem extends ConstraintLayout {

    public UserListItem(Context context) {
        super(context);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.user_list_item, this, true);
    }

    public UserListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.user_list_item, this, true);
    }

    public UserListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = mInflater.inflate(R.layout.user_list_item, this, true);
    }

    public Button getNameButton() {
        return (Button) getChildAt(0);
    }

    public ImageView getProfilePictureImageView() {
        return (ImageView)((CardView) getChildAt(1)).getChildAt(0);
    }
}