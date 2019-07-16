package com.tindercatapp.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class arrayAdapter extends android.widget.ArrayAdapter<cards> {
    Context context;

    public arrayAdapter(Context context, int resourcesId, List<cards> items){
        super(context, resourcesId, items);
    }
    public View getView(int position, View convertsView, ViewGroup parent) {
        cards card_item = getItem(position);

        if (convertsView == null) {
            convertsView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }
        TextView name = (TextView) convertsView.findViewById(R.id.name);
        ImageView image = (ImageView) convertsView.findViewById(R.id.image);

        name.setText(card_item.getName());
        image.setImageResource(R.mipmap.ic_launcher);

        return convertsView;

    }
}
