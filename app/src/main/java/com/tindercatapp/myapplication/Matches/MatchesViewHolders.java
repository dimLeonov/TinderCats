package com.tindercatapp.myapplication.Matches;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.tindercatapp.myapplication.R;

public class MatchesViewHolders extends RecyclerView.ViewHolder implements View.OnClickListener {
public TextView mMatchId,mMatchName;
public ImageView mMatchImage;

public MatchesViewHolders(View itemView){
    super(itemView);
    itemView.setOnClickListener(this);
    mMatchId = (TextView)itemView.findViewById(R.id.Matchid);
    mMatchName=(TextView)itemView.findViewById(R.id.MatchName);
    mMatchImage=(ImageView) itemView.findViewById(R.id.MatchImage);

}

    @Override
    public void onClick(View view) {

    }
}
