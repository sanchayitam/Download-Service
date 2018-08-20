package com.example.sanch.downloadservice;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button downloadButton;
    Button updateButton;
    Context mContext;
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadButton = (Button) findViewById(R.id.button_download);
        updateButton = (Button) findViewById(R.id.button_update);
        mContext = getApplicationContext();

        downloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (checkPermission()) {

                    Intent intent = new Intent(mContext, DownloadService.class);
                    // add infos for the service which file to download and where to store
                    intent.putExtra(DownloadService.URL, getResources().getString(R.string.download_url));
                    Log.i(TAG, "Starting the Download Service : downloading ");
                    startService(intent);
                } else {
                    Log.v(TAG, "Permission to Write to External Storage denied");
                }
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (checkPermission()) {
                Intent intent = new Intent(mContext, DownloadService.class);
                // add infos for the service which file to download and where to store
                intent.putExtra(DownloadService.URL, getResources().getString(R.string.update_url));
                Log.i(TAG,"Starting the Download Service :updating ");
                startService(intent);
                } else {
                    Log.v(TAG, "Permission to Write to External Storage denied");
                }
            }
        });
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String string = bundle.getString(DownloadService.FILEPATH);
                int resultCode = bundle.getInt(DownloadService.RESULT);
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainActivity.this,
                            "Download complete. Download URI: " + string,
                            Toast.LENGTH_LONG).show();
                    Log.v(TAG,"Download complete. Download URI: " + string );

                } else {
                    Toast.makeText(MainActivity.this, "Download failed",
                            Toast.LENGTH_LONG).show();
                    Log.v(TAG,"Download failed");

                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "Registering the Download Service");
        registerReceiver(receiver, new IntentFilter(DownloadService.NOTIFICATION));
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "Un-Registering the Download Service");
        unregisterReceiver(receiver);
    }

    private boolean checkPermission(){
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){
            return true;
        } else {
            return false;
        }
    }
}
