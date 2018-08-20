package com.example.sanch.downloadservice;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.example.sanch.downloadservice.Model.FileItem;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DownloadService extends IntentService {

    private int result = Activity.RESULT_CANCELED;
    public static final String URL = "urlpath";
    public static final String DOWNLOAD = "install";
    public static final String DELETE = "delete";
    public static final String FILEPATH = "filepath";
    public static final String RESULT = "result";
    public static final String NOTIFICATION = "com.example.sanch.downloadservice.receiver";
    String TAG = "DownloadService";
    boolean stopped;

    private FileItem[] mFileInfo;

    public DownloadService() {
        super("DownloadService");
        stopped = false;
    }

    // will be called asynchronously by Android
    @Override
    protected void onHandleIntent(Intent intent) {

        String urlPath = intent.getStringExtra(URL);
        String inputStream = sendGET(urlPath);
        //parse the Json string
        mFileInfo = parseJsonStr(inputStream);

        for (int i = 0 ; i < mFileInfo.length ; i++) {

            String filePath =  mFileInfo[i].getUrl();
            String fileName =   mFileInfo[i].getFileName();
            String fileAddress =  mFileInfo[i].getFileDest();
            String fileAction = mFileInfo[i].getFileAction();
            File fileDir  =  new File(fileAddress);

            if ( mFileInfo[i].getFileAction().contains(DOWNLOAD)) {
              // Download files
                Log.v(TAG, "ACTION : " +  fileAction);
                File output = new File(fileAddress + fileName);
                Log.v(TAG , "Download Destination : " + output.getPath());

                InputStream input = null;
                OutputStream fos = null;

                if (output.exists()) {

                     Log.i(TAG, output + "exists . Deleting it");
                     output.delete();

                }

                try {

                    URL url = new URL(filePath);
                    URLConnection conn = url.openConnection();
                    conn.connect();

                    // input stream to read file
                    input = new BufferedInputStream(url.openStream());

                    //output stream to write file
                    if(!fileDir.exists()) {
                        fileDir.mkdir();
                    }
                    output.createNewFile();

                    fos = new FileOutputStream(output.getPath());

                    byte data[] = new byte[1024];
                    int count;
                    long  total = 0;
                    long lengthOfFile = conn.getContentLength();
                    Log.i(TAG, "Length of file in Bytes " + lengthOfFile);

                    while ((count = input.read(data, 0, 1024)) != -1  ) {

                            if(stopped){ Log.v(TAG, "Download paused"); result = Activity.RESULT_FIRST_USER;  break; }

                          fos.write(data, 0, count);
                          total += count;
                            // In Log file
                            Log.i(TAG, "Downloading File " + fileName);
                            Log.i(TAG,"Download Completed" + (int)((total*100)/lengthOfFile) + "%");
                        }
                        // successfully finished
                        if (lengthOfFile == total) {

                            result = Activity.RESULT_OK;
                        }
                } catch (Exception e) {
                   Log.e(TAG,  e.getMessage());
                }
                finally {
                    if (input != null) {
                        try {
                            input.close();
                        } catch (IOException e) {
                            Log.e(TAG,e.getMessage());
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            Log.e(TAG,e.getMessage());
                        }
                    }
                }
                Log.v(TAG,"File Downloaded :" + output.getAbsolutePath());
                publishResults(output.getAbsolutePath(), result);
            }

            if ( mFileInfo[i].getFileAction().contains(DELETE)){
                Log.v(TAG, "ACTION : " +  fileAction);
                // delete the files
                File output = new File (fileAddress + fileName);
                if (output.exists()) {
                    Log.i(TAG, " Deleting " + output.getAbsolutePath());
                    output.delete();
                    result = Activity.RESULT_OK;
                    Log.i(TAG, " Deleted " + output.getAbsolutePath());
                }
                else
                {
                    Log.i(TAG,  output.getAbsolutePath() + " not found");
                }

            }
        }
    }

    private void publishResults(String outputPath, int result) {
        Intent intent = new Intent(NOTIFICATION);
        intent.putExtra(FILEPATH, outputPath);
        intent.putExtra(RESULT, result);
        sendBroadcast(intent);
    }

    private String sendGET(String urlStr) {
        try {
            URL obj = new URL(urlStr);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            Log.v(TAG,"GET Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                // print result
                return response.toString();
            }
        } catch (IOException e)
        {
            Log.e(TAG,e.getMessage());
            return null;
        }
        return null;
    }


    private FileItem[] parseJsonStr(String JsonStr) {
        Log.v(TAG, JsonStr);
        try {
            //Attributes of Json String
            final String F_NAME = "filename";
            final String F_PATH = "url";
            final String F_ACTION = "action";
            final String F_DEST = "destination_path";

            //Convert Json String to Json Object
            JSONObject JsonObj = new JSONObject(JsonStr);
            //Get the Json Array
            JSONArray fileJsonArray = JsonObj.optJSONArray("media_items");
            FileItem[] fileInfo = new FileItem[fileJsonArray.length()];

            for (int i = 0; i < fileJsonArray.length(); i++) {
                JSONObject post = fileJsonArray.optJSONObject(i);

                fileInfo[i] = new FileItem( post.getString(F_NAME),
                        post.optString(F_PATH),
                        post.getString(F_ACTION),
                        post.getString(F_DEST)

                );
              Log.v(TAG, "File Name : " + fileInfo[i].getFileName());
              Log.v(TAG, "File Path : " + fileInfo[i].getUrl());
              Log.v(TAG, "File Action : " + fileInfo[i].getFileAction());
              Log.v(TAG, "File Destionation : " + fileInfo[i].getFileDest() );

            }
            return fileInfo;
        }
        catch (JSONException e) {

            Log.e(TAG,"JSON parsing Error");
        }
        return null;

    }

    @Override
   public void onDestroy()
    {
        stopped = true;
        super.onDestroy();

    }
}















































