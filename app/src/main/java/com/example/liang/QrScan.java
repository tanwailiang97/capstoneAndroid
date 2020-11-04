package com.example.liang;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QrScan extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private static final String TAG = "ScanCode";
    ZXingScannerView ScannerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScannerView = new ZXingScannerView(this);
        setContentView(ScannerView);
    }

    @Override
    public void handleResult(Result rawResult) {

        Log.d(TAG, "handleResult: " + rawResult.getText());
        //result = rawResult.getText();
        onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        ScannerView.stopCamera();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        ScannerView.setResultHandler(this);
        ScannerView.startCamera();
    }
}
