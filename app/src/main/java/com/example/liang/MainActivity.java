package com.example.liang;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
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
    private String mName = "", mPassword = "", mInOut ="";
    private static boolean qRFlag = true;
    private String androidKey;
    private float x1, x2;

    DatabaseHelper mDatabaseHelper;

    public RecordListAdapter mRecordListAdapter;
    private Record record;

    private ViewFlipper viewFlipper;
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    //Interface
    private TextView tvUserName, tvUserMatric, tvState;
    private ListView lvRecordList;

    //Location
    private Criteria criteria;
    private LocationListener locationListener;
    private LocationManager locationManager;
    private final Looper looper = null;
    private String mLocation = " ";


    @SuppressLint("ClickableViewAccessibility")
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

        tvState = mainPage.findViewById(R.id.tvState);
        tvUserName = settingPage.findViewById(R.id.tvUserName);
        tvUserMatric = settingPage.findViewById(R.id.tvUserPassword);
        lvRecordList = recordPage.findViewById(R.id.recordList);

        viewFlipper.showNext();

        mName = mPreferences.getString("Name", "");
        mPassword = mPreferences.getString("Password", "");
        mInOut = mPreferences.getString("InOut","Check In");

        tvState.setText(mInOut);
        tvUserName.setText(mName);
        tvUserMatric.setText(mPassword);

        runTimePermission();
        checkUserDetails();
        locationSetup();
        androidKey = getString(R.string.ANDROID_KEY);

        if (qRFlag) {
            qRFlag = false;
            startActivity(new Intent(getApplicationContext(), QrScan.class));
        }

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

    private void runTimePermission() {
        Dexter.withContext(this).withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION).
                withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {

                    }
                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        qRFlag = false;
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void checkUserDetails() {
        if (mName.equals("") || mPassword.equals("")) {
            qRFlag = false;
            updateName();
        }
    }

    private void updateName() {
        DialogFragment dialog = new DialogFragment();
        dialog.show(getSupportFragmentManager(), "DialogFragment");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            x1 = event.getX();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            x2 = event.getX();
            float deltaX = x2 - x1;
            if (Math.abs(deltaX) > 150) {
                if (x2 > x1) {
                    Log.d(TAG, "onTouchEvent: Swipe Left");
                    swapNext();
                } else {
                    Log.d(TAG, "onTouchEvent: Swipe Right");
                    swapPrevious();
                }
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: Pressed Back Button");
        if (currentPage == 2) {
            swapPrevious();
        } else if (currentPage == 0) {
            swapNext();
        } else {
            this.finish();
        }
    }

    private void volleyData(String url) {
        if (url == null) return;
        Log.d(TAG, "volleyData: Connecting to Web");

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest jsonObjRequest = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    Log.d(TAG, "onResponse:" + response);
                    if(response.contains("Attendance Added")){
                        Toast.makeText(MainActivity.this,"Attendance Added",Toast.LENGTH_LONG).show();
                    }
                    volleyResponseAddData(1,url);
                },
                error -> {
                    VolleyLog.d("volley", "Error: " + error.getMessage());
                    error.printStackTrace();
                    Log.e(TAG, "onErrorResponse: " + error);
                    volleyResponseAddData(0,url);
                }) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", mName);
                params.put("token", androidKey);
                params.put("coordinate",mLocation);
                params.put("state", mInOut);
                changeInOut();
                return params;
            }
        };
        queue.add(jsonObjRequest);
    }

    private void volleyResponseAddData(int state, String url) {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String strDate = dateFormat.format(date);
        String strTime = timeFormat.format(date);
        String[] splitString = url.split("/");
        String location = " ";
        if(splitString.length>1){
            location = splitString[splitString.length -1];
        }
        Record record = new Record();
        record.setDate(strDate);
        record.setTime(strTime);
        record.setLocation(location);
        record.setState(state);
        if(mInOut.equals("Check In")){
            record.setInOut("Check Out");
        }
        else{
            record.setInOut("Check In");
        }
        boolean insertData = mDatabaseHelper.addData(record);

        if (insertData) {
            Log.d(TAG, "addingData:" + record.getDate() + "; " + record.getTime() + "; " +
                    record.getLocation() + "; " + record.getState() + "successfully added");
        } else {
            Log.d(TAG, "addingData: Fail adding data");
        }
        Log.d(TAG, "addData: " + strDate + strTime + location);
    }

    public void qRScan(View view) {
        Log.d(TAG, "QRScan: Starting");
        startActivity(new Intent(getApplicationContext(), QrScan.class));
    }

    private void getQRData() {
        Intent i = getIntent();
        String url = i.getStringExtra("QRString");
        fetchLocation();
        volleyData(url);
    }

    private void swapPrevious() {
        if (currentPage > 0) {
            viewFlipper.setInAnimation(this, R.anim.slide_in_right);
            viewFlipper.setOutAnimation(this, R.anim.slide_out_left);
            viewFlipper.showNext();
            currentPage -= 1;
        }
        Log.d(TAG, "swapPrevious: " + currentPage);
        if (currentPage == 0) {
            displayData();
        }
    }

    private void swapNext() {
        if (currentPage < TOTAL_PAGE) {
            viewFlipper.setInAnimation(this, android.R.anim.slide_in_left);
            viewFlipper.setOutAnimation(this, android.R.anim.slide_out_right);
            viewFlipper.showPrevious();
            currentPage += 1;
        }
        Log.d(TAG, "swapNext: " + currentPage);
    }

    public void preferenceEdit(String Name, String Matric) {
        mEditor.putString("Name", Name);
        mName = Name;
        mEditor.putString("Password", Matric);
        mPassword = Matric;
        mEditor.apply();
        Log.d(TAG, "PreferenceEdit: Preference Updated");
        tvUserName.setText(mName);
        tvUserMatric.setText(mPassword);
    }

    public void editOnClick(View view) {
        updateName();
    }

    private void displayData() {

        ArrayList<Record> mRecord = new ArrayList<>();
        Log.d(TAG, "displayData: Processing");
        Cursor data = mDatabaseHelper.getListContents();
        int numRows = data.getCount();
        if (numRows != 0) {
            int i = 0;
            while (data.moveToNext()) {
                record = new Record();
                record.setDate(data.getString(1));
                record.setTime(data.getString(2));
                record.setLocation(data.getString(3));
                record.setState(data.getInt(4));
                record.setInOut(data.getString(5));
                mRecord.add(i, record);
                Log.d(TAG, "displayData: " + data.getString(1) + data.getString(2)
                        + data.getString(3) + data.getInt(4));
                Log.d(TAG, "displayData: Data get" + mRecord.get(i));
                i++;
            }
        }
        mRecordListAdapter = new RecordListAdapter(this, R.layout.record_list_adapter_view, mRecord);
        lvRecordList.setAdapter(mRecordListAdapter);
    }

    private void locationSetup() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("Location Changes", location.toString());
                mLocation = location.getLatitude() + " , ";
                mLocation += location.getLongitude() + " , ";
                mLocation += String.valueOf(location.getAccuracy());
                Log.d(TAG, "onLocationChanged: " + mLocation);
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Status Changed", String.valueOf(status));
            }
            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Provider Enabled", provider);
            }
            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Provider Disabled", provider);
            }
        };
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestSingleUpdate(criteria, locationListener, looper);
    }

    public void changeState(View view) {
        changeInOut();
    }
    private void changeInOut(){
        if(mInOut.equals("Check In")){
            mInOut = "Check Out";
        }
        else{
            mInOut = "Check In";
        }
        mEditor.putString("InOut", mInOut);
        mEditor.apply();
        tvState.setText(mInOut);
    }
}