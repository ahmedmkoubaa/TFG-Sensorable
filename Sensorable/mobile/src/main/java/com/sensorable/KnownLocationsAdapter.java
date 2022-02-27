package com.sensorable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class KnownLocationsAdapter extends ArrayAdapter<KnownLocation> {
    private final int resource;
    private Context context;

    public KnownLocationsAdapter(@NonNull Context context, int resource, @NonNull ArrayList<KnownLocation> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String title = this.getItem(position).getTitle();
        String address = this.getItem(position).getAddress();
        String tag = this.getItem(position).getTag();



        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(this.resource, parent, false);

        TextView titleText, addressText, tagText;

        titleText = (TextView) convertView.findViewById(R.id.locationTitle);
        addressText = (TextView) convertView.findViewById(R.id.locationAddress);
        tagText = (TextView) convertView.findViewById(R.id.locationTag);

        titleText.setText(title);
        addressText.setText(address);
        tagText.setText(tag);

        return convertView;
    }
}
