package com.example.liang;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //Variable
    private static final int TOTAL_PAGE = 2;
    private int currentPage = 1;
    private String mName = "" , mId = "";


    DatabaseHelper mDatabaseHelper;


    public RecordListAdapter mRecordListAdapter;
    Record record;

    private ViewFlipper viewFlipper;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    //Interface
    private TextView tvUserName, tvUserMatric;
    private ListView lvRecordList;

    private EditText etLocation, etTemperature;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mEditor = mPreferences.edit();
        mDatabaseHelper = new DatabaseHelper(this);

        viewFlipper = findViewById(R.id.ViewFlipper);
        View settingPage = findViewById(R.id.setting_page);
        View mainPage = findViewById(R.id.main_page);
        View recordPage = findViewById(R.id.record_page);

        tvUserName = settingPage.findViewById(R.id.tvUserName);
        tvUserMatric = settingPage.findViewById(R.id.tvUserMatric);
        lvRecordList = recordPage.findViewById(R.id.recordList);

        etLocation = mainPage.findViewById(R.id.etLocation);
        etTemperature = mainPage.findViewById(R.id.etTemperature);

        viewFlipper.showNext();

        mName = mPreferences.getString("Name","");
        mId = mPreferences.getString("Matric","");

        tvUserName.setText(mName);
        tvUserMatric.setText(mId);

        runTimePermission();
        checkUserDetails();
        getQRData();

        lvRecordList.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                Log.d(TAG, "onSwipeLeft: Swipped Left");
                swapNext();
                // your swipe right here.
            }
        });
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: Resumed");

        super.onResume();
    }

    private void runTimePermission(){
        Dexter.withContext(this).withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE).
                withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void checkUserDetails() {
        if (mName.equals("")|| mId.equals("")){
            updateName();
        }
    }

    private void updateName(){
        DialogFragment dialog = new DialogFragment();
        dialog.show(getSupportFragmentManager(), "DialogFragment");
    }

    float x1,x2;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(event.getAction()==MotionEvent.ACTION_DOWN){
            x1=event.getX();
        }
        else if(event.getAction()==MotionEvent.ACTION_UP){
            x2=event.getX();
            float deltaX = x2-x1;
            if(Math.abs(deltaX)>150){
                if(x2>x1){
                    Log.d(TAG, "onTouchEvent: Swipe Left");
                    swapNext();
                }
                else{
                    Log.d(TAG, "onTouchEvent: Swipe Right");
                    swapPrevious();
                }
            }
        }

        return super.onTouchEvent(event);
    }

    private void volleyData(String url){

        Log.d(TAG, "getData: Web pressed");
        
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest jsonObjRequest = new StringRequest(

                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "onResponse:" + response);
                        String temperature = response.split(":")[1];
                        volleyAddData(temperature);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("volley", "Error: " + error.getMessage());
                        error.printStackTrace();
                        Log.e(TAG, "onErrorResponse: " + error );
                    }
                }) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", mName);
                params.put("id", mId);
                return params;
            }

        };

        queue.add(jsonObjRequest);

    }

    private void volleyAddData(String temperature){


        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String strDate = dateFormat.format(date);
        String strTime = timeFormat.format(date);

        String location = "Testing";
        Record record = new Record();
        record.setDate(strDate);
        record.setTime(strTime);
        record.setLocation(location);
        record.setTemperature(temperature);

        boolean insertData = mDatabaseHelper.addData(record);

        if(insertData){
            Log.d(TAG, "addingData:" + record.getDate() + "; " + record.getTime() + "; " +
                    record.getLocation() +"; " + record.getTemperature() + "successfully added");
        }
        else{
            Log.d(TAG, "addingData: Fail adding data");
        }
        Log.d(TAG, "addData: "+ strDate + strTime + location + temperature);
    }

    public void qRScan(View view){
        Log.d(TAG, "QRScan: Starting");
        startActivity(new Intent(getApplicationContext(),QrScan.class));
    }

    private void getQRData(){
        Intent i = getIntent();
        String url = i.getStringExtra("QRString");
        volleyData(url);
    }

    private void swapPrevious(){
        if(currentPage > 0){
            viewFlipper.setInAnimation(this, R.anim.slide_in_right);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
            viewFlipper.showNext();
            currentPage -= 1;
        }
        Log.d(TAG, "swapPrevious: " + currentPage);
        if(currentPage==0){
            displayData();
        }
    }

    private void swapNext(){
        if(currentPage < TOTAL_PAGE){
            viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
            viewFlipper.showPrevious();
            currentPage += 1;
        }

        Log.d(TAG, "swapNext: " + currentPage);
    }

    public void preferenceEdit(String Name, String Matric){
        mEditor.putString("Name",Name);
        mName = Name;
        mEditor.putString("Matric",Matric);
        mId = Matric;
        mEditor.apply();
        Log.d(TAG, "PreferenceEdit: Name Updated");
        tvUserName.setText(mName);
        tvUserMatric.setText(mId);
    }


    public void editOnClick(View view){
        updateName();
    }

    private void displayData(){

        ArrayList<Record> mRecord = new ArrayList<>();

        Log.d(TAG, "displayData: Processing");
        Cursor data = mDatabaseHelper.getListContents();
        int numRows = data.getCount();
        if (numRows!=0){
            int i=0;
            while(data.moveToNext()){
                record = new Record();
                record.setDate(data.getString(1));
                record.setTime(data.getString(2));
                record.setLocation(data.getString(3));
                record.setTemperature(data.getString(4));

                mRecord.add(i,record);
                Log.d(TAG, "displayData: " +data.getString(1) + data.getString(2)
                        + data.getString(3)+ data.getString(4) );
                Log.d(TAG, "displayData: Data get" + mRecord.get(i));
                i++;
            }
        }
        mRecordListAdapter = new RecordListAdapter(this,R.layout.record_list_adapter_view,mRecord);
        lvRecordList.setAdapter(mRecordListAdapter);
    }



}