package com.tindercatapp.myapplication.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tindercatapp.myapplication.R;

public class ChatViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView mMessage;
    public LinearLayout mContainer;

    public ChatViewHolders(View itemView){
    super(itemView);
    itemView.setOnClickListener(this);

    mMessage = itemView.findViewById(R.id.message);
    mContainer = itemView.findViewById(R.id.container);


}

    @Override
    public void onClick(View view) {

    }
}
