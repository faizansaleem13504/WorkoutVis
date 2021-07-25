package com.example.workoutvisdraft;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Range;
import android.view.View;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class Camera2Activity  extends AppCompatActivity {

    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE","android.permission.RECORD_AUDIO","android.permission.READ_EXTERNAL_STORAGE","android.permission.INTERNET"};
    private int REQUEST_CODE_PERMISSIONS = 1001;
    int FILE_SELECT_CODE=100;
    private Camera mCamera;
    private boolean isRecording = false;
    private CameraPreview mPreview;
    MediaRecorder mediaRecorder;
    ImageView capVideo;
    private static Range<Integer>[] fpsRanges;
    ImageView browse;
    ImageView im2;
    ProgressDialog p;
    Chronometer chronometer;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    String videoPath=null;

    /** Create a file Uri for saving an image or video */
    private Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }
    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
if(ContextCompat.checkSelfPermission(Camera2Activity.this, Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(Camera2Activity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED) {
    File mediaStorageDir;
    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "DCIM/WorkoutVis");
            if (!mediaStorageDir.exists()) {
            //  if (!mediaStorageDir.mkdirs()) {

                try {
                    //mediaStorageDir.mkdirs();
                   Path p= Paths.get(mediaStorageDir.getAbsolutePath());
                    Files.createDirectory(p);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "unable to create Directory", Toast.LENGTH_LONG).show();
                }
            }
            Log.d("MyCameraApp", "failed to create directory");
            //return null;
        //}
    }
    else {
        mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "WorkoutVis");
        if (!mediaStorageDir.exists()) {
            if(!mediaStorageDir.mkdirs()) {

                Toast.makeText(getBaseContext(), "unable to create Directory", Toast.LENGTH_LONG).show();
            }
        }
    }
    /*if (!mediaStorageDir.exists()) {
          if (!mediaStorageDir.mkdirs())
              Log.d("MyCameraApp", "failed to create directory");
        //return null;
        //}
    }*/
    //final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Chords/Processed Audio");
    //boolean flag=dir.mkdirs();

    // Create a media file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    File mediaFile;
    if (type == MEDIA_TYPE_IMAGE) {
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
    } else if (type == MEDIA_TYPE_VIDEO) {
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "VID_" + timeStamp + ".mp4");
    } else {
        return null;
    }
    videoPath=mediaFile.getPath();
    return mediaFile;
}
else{
    Toast.makeText(getBaseContext(), "Permission issues", Toast.LENGTH_LONG).show();
}
        return null;
    }
    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
           // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCamera();
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
    private Range<Integer> getRange() {
        CameraCharacteristics chars = null;
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            chars = manager.getCameraCharacteristics(manager.getCameraIdList()[0]);
            Range<Integer>[] ranges = chars.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            Range<Integer> result = null;
            for (Range<Integer> range : ranges) {
                int upper = range.getUpper();
                int lower = range.getLower();
                // 10 - min range upper for my needs
                Log.e("Upper fps :",""+upper);
                Log.e("range fps :",""+range);
                if (upper >= 10000) {
                    if (result == null || upper < result.getUpper().intValue()) {
                        Range<Integer> newsrange = new Range<>(range.getLower()/1000,range.getLower()/1000);
                        result = newsrange;
                        Log.e("result fps :",""+range);

                    }
                }
                if (upper >= 10) {
                    if (result == null || upper < result.getUpper().intValue()) {
                        result = range;
                        Log.e("result fps :",""+range);

                    }
                }
            }
            if (result == null) {
                result = ranges[0];
                Log.e("result in range[0] is :",""+result);
            }
            return result;
        } catch (CameraAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    private boolean prepareVideoRecorder(){

        mCamera = getCameraInstance();
        mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        //CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        mediaRecorder = new MediaRecorder();
        //mediaRecorder.setVideoEncoder();

        mediaRecorder.setOrientationHint(90);
        mediaRecorder.setMaxDuration(1000*120); //setting max duration to 2 minutes
        mediaRecorder.setCaptureRate(30);
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);

        //mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        // Step 2: Set sources
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
       // mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_480P));
        // Step 4: Set output file
        mediaRecorder.setOutputFile(getOutputMediaFile(MEDIA_TYPE_VIDEO).toString());

        // Step 5: Set the preview output
        mediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

        mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if( what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)
                {
                    chronometer.stop();
                    chronometer.setVisibility(View.INVISIBLE);
                    isRecording=false;
                    capVideo.setImageResource(R.mipmap.btn_video_online);
                    // stop recording and release camera
                    mediaRecorder.stop();  // stop the recording
                    releaseMediaRecorder(); // release the MediaRecorder object
                    mCamera.lock();         // take camera access back from MediaRecorder

                    // inform the user that recording has stopped

                    // setCaptureButtonText("Capture");
                    isRecording = false;
                }
            }
        });
        //mediaRecorder.setVideoEncodingBitRate(10000000);

       // mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        // Step 6: Prepare configured MediaRecorder
        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    private boolean allPermissionsGranted(){

        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_CODE_PERMISSIONS){
            if(allPermissionsGranted()){
                startCamera();
            } else{
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }
    private class AnalyzeTask extends AsyncTask<String, String, String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p.hide();
            p = new ProgressDialog(Camera2Activity.this);
            p.setMessage("Please wait...Video is being Analyzed");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }
        @Override
        protected String doInBackground(String... strings) {
            String bitmap=strings[0];
            analyzeTask(bitmap);
            return "";
        }
        @Override
        protected void onPostExecute(String bitmap) {
           // p.hide();

        }

    }
    private void analyzeTask(String bitmap){
        File root = new File(Environment.getExternalStorageDirectory(), "DCIM/Notes");
        File gpxfile;
        //   if (!root.exists()) {
        //      root.mkdirs();
        // }
        root = new File(getBaseContext().getExternalFilesDir(null), "WorkoutVis");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            if (!root.exists()) {
                //  if (!mediaStorageDir.mkdirs()) {

                try {
                    //mediaStorageDir.mkdirs();
                    Path p= Paths.get(root.getAbsolutePath());
                    Files.createDirectory(p);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), "unable to create Directory", Toast.LENGTH_LONG).show();
                }
            }
            Log.d("MyCameraApp", "failed to create directory");
            //return null;
            //}
        }
        else {

            if (!root.exists()) {
                if(!root.mkdirs()) {

                    Toast.makeText(getBaseContext(), "unable to create Directory", Toast.LENGTH_LONG).show();
                }
            }


        }
        gpxfile = new File(root, "singleRep.txt");
        boolean flag=false;
        try {
            flag=gpxfile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(gpxfile));
            System.out.print("byte array"+bitmap);
            writer.write(bitmap);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();
        APIData apiData=new APIData(bitmap);
        Retrofit retrofit=new Retrofit.Builder().baseUrl("https://workoutvis-320817.uc.r.appspot.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        OurRetrofit ourRetrofit=retrofit.create(OurRetrofit.class);
        Call<APIRes> call=ourRetrofit.PostData(apiData);
        call.enqueue(new Callback<APIRes>() {
            @Override
            public void onResponse(Call<APIRes> call, Response<APIRes> response) {
                //System.out.println(response.body().cycles);
                Intent i=new Intent(Camera2Activity.this, Feedback.class);
                //i.putExtra("predictions",response.body().predictions);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("predictions",response.body().predictions);
                i.putExtras(mBundle);
                Bundle b2=new Bundle();
                b2.putSerializable("cycles",response.body().cycles);
                i.putExtras(b2);

                //i.putExtra("cycle",response.body().cycles);
                i.putExtra("filePath",Uri.fromFile(new File(videoPath)));
                startActivity(i);
                finish();
            }
            @Override
            public void onFailure(Call<APIRes> call, Throwable t) {
                System.out.println("Failed");
                Toast.makeText(getBaseContext(),"Failed to analyze the video in fixed time", Toast.LENGTH_SHORT).show();
                Intent i=new Intent(Camera2Activity.this, MainActivity.class);
                startActivity(i);
            }
        });

    }
    public void upload(String encode){
        RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), encode);
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.MINUTES)
                .readTimeout(10, TimeUnit.MINUTES)
                .build();
        Retrofit retrofit=new Retrofit.Builder().baseUrl("https://workoutvis-320817.uc.r.appspot.com")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        RetrofitMultipart ourRetrofit=retrofit.create(RetrofitMultipart.class);
        Call<APIRes> call=ourRetrofit.uploadImage(descBody);
        call.enqueue(new Callback<APIRes>() {
            @Override
            public void onResponse(Call<APIRes> call, Response<APIRes> response) {
                System.out.println(response.body().cycles);
                Intent i=new Intent(Camera2Activity.this, Feedback.class);
                //i.putExtra("predictions",response.body().predictions);
                Bundle mBundle = new Bundle();
                mBundle.putSerializable("predictions",response.body().predictions);
                i.putExtras(mBundle);
                Bundle b2=new Bundle();
                b2.putSerializable("cycles",response.body().cycles);
                i.putExtras(b2);

                //i.putExtra("cycle",response.body().cycles);
                i.putExtra("filePath",Uri.fromFile(new File(videoPath)));
                startActivity(i);
                finish();
            }
            @Override
            public void onFailure(Call<APIRes> call, Throwable t) {
                System.out.println("Failed");
                Toast.makeText(getBaseContext(),"Failed to analyze the video in fixed time", Toast.LENGTH_SHORT).show();
                Intent i=new Intent(Camera2Activity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }
    private class AsyncTaskExample extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(Camera2Activity.this);
            p.setMessage("Please wait...Video is being uploaded");
            p.setIndeterminate(false);
            p.setCancelable(false);
            p.show();
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected String doInBackground(String... strings) {
            InputStream inputStream;

            try {
                inputStream = new FileInputStream(strings[0]);
                byte[] bytes;
                byte[] buffer = new byte[8192];
                int bytesRead;
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                bytes = output.toByteArray();
                String encodedString = java.util.Base64.getEncoder().encodeToString(bytes);

                //analyzeTask(encodedString);
               // upload(encodedString);
                return encodedString;
            } catch (Exception e) {
                e.printStackTrace();
                // TODO: handle exception
            }
            return "";
        }
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(String bitmap) {
            super.onPostExecute(bitmap);
            p.hide();
            AnalyzeTask at=new AnalyzeTask();
            at.execute(bitmap);


            /*File root = new File(Environment.getExternalStorageDirectory(), "DCIM/Notes");
            File gpxfile;
            //   if (!root.exists()) {
          //      root.mkdirs();
           // }
            root = new File(getBaseContext().getExternalFilesDir(null), "WorkoutVis");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                if (!root.exists()) {
                    //  if (!mediaStorageDir.mkdirs()) {

                    try {
                        //mediaStorageDir.mkdirs();
                        Path p= Paths.get(root.getAbsolutePath());
                        Files.createDirectory(p);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), "unable to create Directory", Toast.LENGTH_LONG).show();
                    }
                }
                Log.d("MyCameraApp", "failed to create directory");
                //return null;
                //}
            }
            else {

                if (!root.exists()) {
                    if(!root.mkdirs()) {

                        Toast.makeText(getBaseContext(), "unable to create Directory", Toast.LENGTH_LONG).show();
                    }
                }


            }
            gpxfile = new File(root, "singleRep.txt");
            boolean flag=false;
            try {
                flag=gpxfile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(gpxfile));
                System.out.print("byte array"+bitmap);
                writer.write(bitmap);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


            //
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.MINUTES)
                    .readTimeout(10, TimeUnit.MINUTES)
                    .build();
            APIData apiData=new APIData(bitmap);
            Retrofit retrofit=new Retrofit.Builder().baseUrl("https://workoutvis-320817.uc.r.appspot.com")
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            OurRetrofit ourRetrofit=retrofit.create(OurRetrofit.class);
            Call<APIRes> call=ourRetrofit.PostData(apiData);
            call.enqueue(new Callback<APIRes>() {
                @Override
                public void onResponse(Call<APIRes> call, Response<APIRes> response) {
                    System.out.println(response.body().getJson().cycles);
                }
                @Override
                public void onFailure(Call<APIRes> call, Throwable t) {
                    System.out.println("Failed");
                }
            });

            p.hide();*/

        }
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE) {
            Uri selectedVideoUri = data.getData();
            String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DURATION};
            Cursor cursor = managedQuery(selectedVideoUri, projection, null, null, null);

            cursor.moveToFirst();
            String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
            videoPath=filePath;
            Log.d("File Name:",filePath);
            Toast.makeText(getBaseContext(),"File Name:"+filePath,Toast.LENGTH_LONG);

            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
            // Setting the thumbnail of the video in to the image view

            //im2.setImageBitmap(thumb);
            InputStream inputStream = null;
            // Converting the video in to the bytes
            try
            {
                inputStream = getContentResolver().openInputStream(selectedVideoUri);
                // byte[] video = new byte[inputStream.available()];
                AsyncTaskExample asyncTask=new AsyncTaskExample();
                asyncTask.execute(filePath);
                //byte[] base64 = Base64.encode(video,Base64.DEFAULT);
                System.out.println("hello");
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void startCamera(){

        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);

        FrameLayout preview = (FrameLayout) findViewById(R.id.frameCam);
        preview.addView(mPreview);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerax);

        // Create an instance of Camera

        browse=findViewById(R.id.browse);

        capVideo=findViewById(R.id.capVideo);
        chronometer=findViewById(R.id.chronometer);
        capVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    chronometer.stop();
                    chronometer.setVisibility(View.INVISIBLE);
                    isRecording=false;
                    capVideo.setImageResource(R.mipmap.btn_video_online);
                    // stop recording and release camera
                    try {
                        mediaRecorder.stop();  // stop the recording
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                    releaseMediaRecorder(); // release the MediaRecorder object
                    mCamera.lock();         // take camera access back from MediaRecorder
                    AsyncTaskExample at=new AsyncTaskExample();
                    at.execute(videoPath);

                    // inform the user that recording has stopped

                    // setCaptureButtonText("Capture");
                    isRecording = false;
                   /* if(videoPath!=null){
                        Intent i = new Intent(Camera2Activity.this, Feedback.class);
                        i.putExtra("filePath",Uri.fromFile(new File(videoPath)));

                        startActivity(i);
                    }*/

                } else {
                    // initialize video camera
                    if (prepareVideoRecorder()) {
                        // Camera is available and unlocked, MediaRecorder is prepared,
                        // now you can start recording
                        isRecording=true;
                        capVideo.setImageResource(R.mipmap.btn_video_busy);
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.setVisibility(View.VISIBLE);
                        chronometer.start();
                        // recorder.start();
                        mediaRecorder.start();


                        // inform the user that recording has started
                        //   setCaptureButtonText("Stop");
                        isRecording = true;
                    } else {
                        // prepare didn't work, release the camera
                        releaseMediaRecorder();
                        // inform user
                    }
                }
            }
        });
        browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                // intent.setType("*/*");
                Intent videoPickIntent = new Intent(Intent.ACTION_PICK);
                videoPickIntent.setType("video/*");
                try {
                    //startActivityForResult(chooser, FILE_SELECT_CODE);
                    startActivityForResult(Intent.createChooser(videoPickIntent, "Select a Video"),FILE_SELECT_CODE);
                } catch (Exception ex) {
                    System.out.println("browseClick :"+ex);//android.content.ActivityNotFoundException ex
                }
            }
        });
        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

}