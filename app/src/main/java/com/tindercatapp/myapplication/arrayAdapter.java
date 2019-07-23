package com.tindercatapp.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.res.Resources;


import com.bumptech.glide.Glide;

import java.util.List;

public class arrayAdapter extends ArrayAdapter<cards> {

    Context context;
    String CardUserID;
//int i=0;

    public arrayAdapter(Context context, int resourceId, List<cards> items) {
        super(context, resourceId, items);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        cards card_item = getItem(position);

        /*i++;
        if (i == 1) {
            setCardUserID(card_item.getUserId());
        }
        //if (i > 1){i=0;}*/

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }

        TextView name = (TextView) convertView.findViewById(R.id.name);
        ImageView image = (ImageView) convertView.findViewById(R.id.image);


        //name.setText(card_item.getName());
        name.setText(card_item.toString());


        switch (card_item.getProfileImageUrl()) {
            case "default":
                //image.setImageResource(R.drawable.ic_launcher_web);
                Glide.clear(image);
                Glide.with(convertView.getContext()).load(R.drawable.logo_green_400).into(image);
                break;
            default:
                Glide.clear(image);
                Glide.with(convertView.getContext()).load(card_item.getProfileImageUrl()).into(image);
                break;
        }


        return convertView;
    }

/*    public void setCardUserID(String CardUserID) {
            this.CardUserID = CardUserID;
    }

    public String getCardUserID() {
        return CardUserID;
    }*/

}
