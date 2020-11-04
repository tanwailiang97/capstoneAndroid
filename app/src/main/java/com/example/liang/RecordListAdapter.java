package com.example.liang;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;



import java.util.ArrayList;

public class RecordListAdapter extends ArrayAdapter<Record> {

    private LayoutInflater mLayoutInflater;

    private ArrayList<Record> mRecord;
    private int  mResourceId;

    public RecordListAdapter(Context context, int resource, ArrayList<Record> record) {
        super(context, resource, record);
        this.mRecord = record;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResourceId = resource;
    }

    @Override
    public View getView(int position, View convertView,  ViewGroup parent) {

        convertView = mLayoutInflater.inflate(mResourceId,null);

        Record record = mRecord.get(position);

        if(record!=null){
            TextView location = convertView.findViewById(R.id.tvLocation);
            TextView date = convertView.findViewById(R.id.tvDate);
            TextView time = convertView.findViewById(R.id.tvTime);
            TextView temperature = convertView.findViewById(R.id.tvTemperature);

            if(location != null){
                location.setText(record.getLocation());
            }
            if(date != null){
                date.setText(record.getDate());
            }
            if(time != null){
                time.setText(record.getTime());
            }
            if(temperature != null){
                String text = record.getTemperature() + "°C";
                temperature.setText(text);
            }
        }

        return convertView;
    }
}
