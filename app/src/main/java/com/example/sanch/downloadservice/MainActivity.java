package com.example.sanch.downloadservice;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
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
    Button pauseButton;
    Button resumeButton;

    Context mContext;
    String TAG = "MainActivity";

    //Permision code that will be checked in the method onRequestPermissionsResult
    private int STORAGE_PERMISSION_CODE = 21;
    Intent intent = null;

    static final String DOWNLOAD_KEY = "download key";
    Boolean download_flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        downloadButton = (Button) findViewById(R.id.button_download);
        updateButton = (Button) findViewById(R.id.button_update);
        pauseButton = (Button) findViewById(R.id.button_pause);
        resumeButton = (Button) findViewById(R.id.button_resume);

        mContext = getApplicationContext();

        downloadButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (checkPermission()) {

                    startDownload();
                    enableButtons();

                } else {

                    Log.v(TAG, "Permission to Write onto External Storage denied");
                    //If the app has not the permission then asking for the permission
                    requestStoragePermission();

                }
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (checkPermission()) {

                    startUpdate();
                    enableButtons();

                } else {

                    Log.v(TAG, "Permission to Write onto External Storage denied");
                    //If the app has not the permission then asking for the permission
                    requestStoragePermission();

                }
            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                    if (intent != null) {

                        stopService(intent);
                        intent = null;

                    }
                    else
                    {

                        Log.v(TAG, " Oops,No file to download");
                        Toast.makeText(mContext, "Oops , No file to download", Toast.LENGTH_LONG );

                    }

            }
        });

        resumeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                pauseButton.setVisibility(View.VISIBLE);
                if (download_flag) {

                    startDownload();

                } else {

                    startUpdate();

                }

            }
        });

        if(savedInstanceState != null) {
            if (savedInstanceState.containsKey(DOWNLOAD_KEY)) {

                download_flag = savedInstanceState.getBoolean(DOWNLOAD_KEY);

            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

            savedInstanceState.putBoolean(DOWNLOAD_KEY, download_flag);

    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //  super.onRestoreInstanceState(savedInstanceState);
        download_flag = savedInstanceState.getBoolean(DOWNLOAD_KEY);

    }

    private  void  startDownload()
    {
        intent = new Intent(mContext, DownloadService.class);
        // add infos for the service which file to download and where to store
        intent.putExtra(DownloadService.URL, getResources().getString(R.string.download_url));
        Log.i(TAG, "Starting the Download Service : downloading ");
        download_flag = true;
        startService(intent);

    }

    private  void startUpdate()
    {
        intent = new Intent(mContext, DownloadService.class);
        // add infos for the service which file to download and where to store
        intent.putExtra(DownloadService.URL, getResources().getString(R.string.update_url));
        Log.i(TAG,"Starting the Download Service :updating ");
        download_flag = false;
        startService(intent);

    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            if (bundle != null) {

                String string = bundle.getString(DownloadService.FILEPATH);
                int resultCode = bundle.getInt(DownloadService.RESULT);

                switch (resultCode) {
                    case (RESULT_OK): {
                        Toast.makeText(MainActivity.this,
                                "Download complete. Download URI: " + string,
                                Toast.LENGTH_LONG).show();
                        Log.v(TAG, "Download complete. Download URI: " + string);
                            disableButtons();

                        break;
                    }
                    case (RESULT_CANCELED): {
                        Toast.makeText(MainActivity.this, "Download failed",
                                Toast.LENGTH_LONG).show();
                        Log.v(TAG, "Download failed");
                        disableButtons();
                        break;
                    }

                    case (RESULT_FIRST_USER): {
                        Toast.makeText(MainActivity.this, "Download paused",
                                Toast.LENGTH_LONG).show();
                        Log.v(TAG, "Download paused");
                        pauseButton.setVisibility(View.GONE);
                        resumeButton.setVisibility(View.VISIBLE);

                        break;
                    }

                }
            }
        }
    };

    private void  disableButtons()
    {
        pauseButton.setVisibility(View.GONE);
        resumeButton.setVisibility(View.GONE);

    }

    private void  enableButtons()
    {
        pauseButton.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.VISIBLE);

    }


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

        int result = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED){

                // Request user to grant write external storage permission.
               // ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                return true;

        }
        else

            return false;

    }


    //Requesting permission
    private void requestStoragePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){

            Toast.makeText(this,"Permission required to write into the storage",Toast.LENGTH_LONG).show();

        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions, int[] grantResults) {

        //Checking the request code of our request
        if(requestCode == STORAGE_PERMISSION_CODE){

            //If permission is granted
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                //Displaying a toast
                Toast.makeText(this,"Permission granted now you can write into the storage",Toast.LENGTH_LONG).show();

            }else{
                //Displaying another toast if permission is not granted
                Toast.makeText(this,"Oops you just denied the permission",Toast.LENGTH_LONG).show();
            }
        }
    }
}
