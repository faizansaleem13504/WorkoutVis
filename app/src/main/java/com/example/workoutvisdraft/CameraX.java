package com.example.workoutvisdraft;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.loader.content.CursorLoader;

import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.media.MediaMetadataRetriever.OPTION_CLOSEST;

//import android.graphics.Bitmap;

public class CameraX extends AppCompatActivity implements LifecycleOwner {

    private Executor executor = Executors.newSingleThreadExecutor();
    private int REQUEST_CODE_PERMISSIONS = 1001;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE","android.permission.RECORD_AUDIO"};
    Interpreter tflite = null;
    VideoCapture videoCapture;

    ImageView im2;
    PreviewView mPreviewView;
    ImageView captureImage;
    ImageView capVideo;
    ImageView browse;
    Chronometer chronometer;
    Camera camera;
    boolean isRecording=false;
    SurfaceView surfaceView;
    MediaRecorder recorder;
    Canvas canvas;
    int count=0;
    MyService mService;
    ProgressDialog p;
    Bitmap imageBitmap2;
    boolean mBound = false;
    Map<Integer, Object> outputMap = new HashMap<>();
    float[][][][] out1 = new float[1][23][17][17];
    float[][][][] out2 = new float[1][23][17][34];
    float[][][][] out3 = new float[1][23][17][64];
    float[][][][] out4 = new float[1][23][17][1];
    int FILE_SELECT_CODE=100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerax);
        mPreviewView = findViewById(R.id.camera);

        captureImage = findViewById(R.id.captureImg);
        capVideo=findViewById(R.id.capVideo);
        browse=findViewById(R.id.browse);
        chronometer=findViewById(R.id.chronometer);
        recorder = new MediaRecorder();
       // surfaceView=findViewById(R.id.cameraView);

        Intent intent = new Intent(CameraX.this, MyService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
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
    /*    String modelFile="C:\\Users\\faiza\\AndroidStudioProjects\\WorkoutVisDraft\\app\\src\\main\\ml\\posenet_mv1_075_float_from_checkpoints.tflite";
        try {
            tflite=new Interpreter(loadModelFile(CameraX.this,modelFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Tensor no = tflite.getInputTensor(0);
        Log.d("TAG3", "onCreate: Input shape"+ Arrays.toString(no.shape()));

        int c = tflite.getOutputTensorCount();
        Log.d("TAG4", "onCreate: Output Count" +c );
        for (int i = 0; i <4 ; i++) {
            final Tensor output = tflite.getOutputTensor(i);
            Log.d("TAG5", "onCreate: Output shape" + Arrays.toString(output.shape()));
        }*/
        /** Defines callbacks for service binding, passed to bindService() */

        capVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isRecording){
                    chronometer.stop();
                    chronometer.setVisibility(View.INVISIBLE);
                    isRecording=false;
                    capVideo.setImageResource(R.mipmap.btn_video_online);
                    unbindService(connection);
                    mBound = false;
                    Intent i = new Intent(CameraX.this, Feedback.class);
                    startActivity(i);

                    finish();
                    //recorder.release();
                    //  initRecorder();
                }
                else{
                    isRecording=true;
                    capVideo.setImageResource(R.mipmap.btn_video_busy);
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.setVisibility(View.VISIBLE);
                    //Intent intent = new Intent(CameraX.this, MyService.class);
                    //bindService(intent, connection, Context.BIND_AUTO_CREATE);
                    chronometer.start();
                    // recorder.start();
                }
            }
        });
        if(allPermissionsGranted()){
            startCamera(); //start camera if permission has been granted by user
        } else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

    }
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            MyService.LocalBinder binder = (MyService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getApplicationContext(),
                contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    public static Bitmap retriveVideoFrameFromVideo(String p_videoPath, int i)

    {
        Bitmap m_bitmap = null;
        MediaMetadataRetriever m_mediaMetadataRetriever = null;
        try
        {
            m_mediaMetadataRetriever = new MediaMetadataRetriever();
            m_mediaMetadataRetriever.setDataSource(p_videoPath);
            m_bitmap = m_mediaMetadataRetriever.getFrameAtTime(i*100000,OPTION_CLOSEST);
        }
        catch (Exception m_e)
        {
            String s=m_e.getMessage();
            System.out.println(s);
        }
        finally
        {
            if (m_mediaMetadataRetriever != null)
            {
                m_mediaMetadataRetriever.release();
            }
        }
        return m_bitmap;
    }
    private class AsyncTaskExample extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            p = new ProgressDialog(CameraX.this);
            p.setMessage("Please wait...It is downloading");
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
                return encodedString;
            } catch (Exception e) {
                // TODO: handle exception
            }


            // Log.i("Strng", encodedString);
            /*byte[] base64 = Base64.encode(strings[0],Base64.DEFAULT);
            System.out.println(base64);
            try{


                OutputStream
                        os
                        = new FileOutputStream(gpxfile);
                os.write(base64);
                os.close();*/

            return "";
        }


        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(String bitmap) {
            super.onPostExecute(bitmap);
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "singleRep.txt");
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
            //ThreadClass tc=new ThreadClass(apiData);
            //tc.start();
           /*  try {
                 ThreadClass tc=new ThreadClass(apiData);
                 //tc.start();
                AsyncHttpClient client = new AsyncHttpClient();
                // Http Request Params Object
                //client.
                RequestParams params = new RequestParams();

                // String mob = "880xxxxxxxxxx";
                params.put("data", bitmap);

                //  params.put("uph", mob.toString());
                client.post("https://workoutvis.uc.r.appspot.com/run_model", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                        String s=new String(responseBody);
                        System.out.println("SuccessssssssssssssssssSSSSSSSSSSSSSSS"+responseBody);
                    }
                    @Override
                    public void onFailure(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody, Throwable error) {
                        System.out.println(statusCode);
                        System.out.println(error);
                        System.out.println(responseBody);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            byte[] decodedBytes = java.util.Base64.getDecoder().decode(bitmap.getBytes());

            try {
                FileOutputStream out = new FileOutputStream(
                        Environment.getExternalStorageDirectory()
                                + "/Notes/Convert.mp4");
                out.write(decodedBytes);
                out.close();
            } catch (Exception e) {
                // TODO: handle exception
                Log.e("Error", e.toString());

            }*/

            p.hide();

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
            Log.d("File Name:",filePath);

            Bitmap thumb = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND);
            // Setting the thumbnail of the video in to the image view

            im2.setImageBitmap(thumb);
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




//Decode String To Video With mig Base64.



            /*int bufferSize = 100000;
            byte[] buffer = new byte[bufferSize];
            ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
            Base64OutputStream output64 = new Base64OutputStream(byteBuffer, Base64.DEFAULT);
            int len = 0;
            try
            {
                while ((len = inputStream.read(buffer)) != -1)
                {
                    output64.write(buffer, 0, len  );
                    //byteBuffer.write(buffer, 0, len);
                }
                output64.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            String attachedFile = byteBuffer.toString();

            System.out.println("converted!"+attachedFile);*/
            //System.out.println("converted2!"+attachedFile);

            //String videoData="";
            //Converting bytes into base64
            //videoData = Base64.encodeToString(byteBuffer.toByteArray(), Base64.DEFAULT);
            //Log.d("VideoData**>  " , videoData);

            //String sinSaltoFinal2 = videoData.trim();
            //String sinsinSalto2 = sinSaltoFinal2.replaceAll("\n", "");
            //String sNew=videoData;
           // for(int i=0;i<400;i++)
            //    sNew+=sinsinSalto2.charAt(i);
            //String sinSaltoFinal2 = attachedFile.trim();
            /*String sinsinSalto2 = attachedFile.replaceAll("\n", "");
            

            Log.d("VideoData**>  " , sinsinSalto2);
            String str = "Hello";
            try{
                File root = new File(Environment.getExternalStorageDirectory(), "Notes");
                if (!root.exists()) {
                    root.mkdirs();
                }
                File gpxfile = new File(root, "output.txt");


            BufferedWriter writer = new BufferedWriter(new FileWriter(gpxfile));
            writer.write(sinsinSalto2);

            writer.close();
            }
            catch(Exception e){
                System.out.println(e);
            }*/
           // APIData apiData=new APIData(sNew);
            //APIData apiData=new APIData(sinsinSalto2);
            //ThreadClass tc=new ThreadClass(apiData);
            //tc.start();
            /* try {
                AsyncHttpClient client = new AsyncHttpClient();
                // Http Request Params Object
                client.
                RequestParams params = new RequestParams();

                // String mob = "880xxxxxxxxxx";
                params.put("data", sNew);

                //  params.put("uph", mob.toString());
                client.post("https://workoutvis.uc.r.appspot.com/run_model", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String s=new String(responseBody);
                        System.out.println("SuccessssssssssssssssssSSSSSSSSSSSSSSS");

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        System.out.println(statusCode);
                        System.out.println(error);
                        System.out.println(responseBody);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }*/
           // baseVideo = sinsinSalto2;

            /*Uri uri = data.getData();
            ////Intent intent = new Intent(CameraX.this, MyService.class);
            //bindService(intent, connection, Context.BIND_AUTO_CREATE);
            String path = getRealPathFromURI(uri);
            Toast.makeText(this, path, Toast.LENGTH_LONG).show();
            int i = 0;
            imageBitmap2 = retriveVideoFrameFromVideo(path, i);
            while (imageBitmap != null) {
                for (int j = 0; j < 20; j++) {
                    if (imageBitmap2 != null) {
                        Runnable someRunnable = new Runnable() {
                            @Override
                            public void run() {
                                // todo: background tasks
                                // runOnUiThread(new Runnable() {
                                //   @Override
                                // public void run() {
                                // mService.processVideoFrame(CameraX.this,surfaceView,imageBitmap2);
                                ModelClass modelClass = new ModelClass();
                                Bitmap croppedBitmap = modelClass.cropBitmap(imageBitmap2);
                                // Bitmap croppedBitmap=modelClass.cropBitmap(modelClass.toBitmap(im));
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, MODEL_WIDTH, MODEL_HEIGHT, true);

                                ModelExecution modelExecution = new ModelExecution();
                                //modelExecution.init(CameraX.this);
                                modelExecution.init(CameraX.this);
                                Person p = modelExecution.call(scaledBitmap);


                                // val canvas: Canvas = surfaceHolder!!.lockCanvas()

                                Canvas canvas = surfaceView.getHolder().lockCanvas();
                                if (canvas != null) {
                                    modelExecution.draw(canvas, p, scaledBitmap, surfaceView.getHolder());
                                }
                                // todo: update your ui / view in activity
                                //   }
                                // });
                            }
                        };
                        Executors.newSingleThreadExecutor().execute(someRunnable);
                    }
                }
                imageBitmap2 = retriveVideoFrameFromVideo(path,i++);
                /*ModelClass modelClass = new ModelClass();
                int PREVIEW_WIDTH = 640;
                int PREVIEW_HEIGHT = 480;
                Matrix rotateMatrix = new Matrix();
                rotateMatrix.postRotate(90.0f);
                //   Bitmap rotatedBitmap=Bitmap.createBitmap(imageBitmap, 0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                //           rotateMatrix, true);
                Bitmap croppedBitmap = modelClass.cropBitmap(imageBitmap);
                // Bitmap croppedBitmap=modelClass.cropBitmap(modelClass.toBitmap(im));
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, MODEL_WIDTH, MODEL_HEIGHT, true);

                ModelExecution modelExecution = new ModelExecution();
                modelExecution.init(CameraX.this);
                Person p = modelExecution.call(scaledBitmap);
                SurfaceHolder sh = surfaceView.getHolder();
                if (canvas != null) {
                    sh.unlockCanvasAndPost(canvas);
                }
                canvas = surfaceView.getHolder().lockCanvas();
                if(canvas!=null)
                    modelExecution.draw(canvas, p, scaledBitmap, surfaceView.getHolder());
                i++;
                imageBitmap = retriveVideoFrameFromVideo(path,i);

            }
        }


            /*Handler h=new Handler();
            h.post(new Runnable() {
                @Override
                public void run() {

                    Bitmap imageBitmap = retriveVideoFrameFromVideo(path,i);
                    while (imageBitmap != null) {
                        ModelClass modelClass = new ModelClass();
                        int PREVIEW_WIDTH = 640;
                        int PREVIEW_HEIGHT = 480;
                        Matrix rotateMatrix = new Matrix();
                        rotateMatrix.postRotate(90.0f);
                        //   Bitmap rotatedBitmap=Bitmap.createBitmap(imageBitmap, 0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                        //           rotateMatrix, true);
                        Bitmap croppedBitmap = modelClass.cropBitmap(imageBitmap);
                        // Bitmap croppedBitmap=modelClass.cropBitmap(modelClass.toBitmap(im));
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, MODEL_WIDTH, MODEL_HEIGHT, true);

                        ModelExecution modelExecution = new ModelExecution();
                        modelExecution.init(CameraX.this);
                        Person p = modelExecution.call(scaledBitmap);
             (ImageFormat)map;
            ModelClass modelClass=new ModelClass();

            byte[][] yuvBytes=modelClass.getYuvBytes();

            modelClass.fillBytes(im.getPlanes(),yuvBytes);
            int PREVIEW_WIDTH = 640;
            int PREVIEW_HEIGHT = 480;
            int []rgbBytes=new int[PREVIEW_WIDTH*PREVIEW_HEIGHT];
            modelClass.imageUtilWrapper(yuvBytes,PREVIEW_WIDTH,PREVIEW_HEIGHT,im,rgbBytes);
            Bitmap imageBitmap = Bitmap.createBitmap(
                    rgbBytes, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                    Bitmap.Config.ARGB_8888
            );
            Matrix rotateMatrix=new Matrix();
            rotateMatrix.postRotate(90.0f);
            Bitmap rotatedBitmap=Bitmap.createBitmap(imageBitmap, 0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                    rotateMatrix, true);
            ////////////////////////////////////////////////////
            Bitmap croppedBitmap=modelClass.cropBitmap(rotatedBitmap);
            // Bitmap croppedBitmap=modelClass.cropBitmap(modelClass.toBitmap(im));
            Bitmap scaledBitmap=Bitmap.createScaledBitmap(croppedBitmap,MODEL_WIDTH,MODEL_HEIGHT,true);

            ModelExecution modelExecution=new ModelExecution();
            modelExecution.init(CameraX.this);
            Person p=modelExecution.call(scaledBitmap);


            // val canvas: Canvas = surfaceHolder!!.lockCanvas()
                        SurfaceHolder sh = surfaceView.getHolder();
                        if (canvas != null) {
                            sh.unlockCanvasAndPost(canvas);
                        }
                            canvas = surfaceView.getHolder().lockCanvas();
                        if(canvas!=null)
                            modelExecution.draw(canvas, p, scaledBitmap, surfaceView.getHolder());
                        i++;
                        imageBitmap = retriveVideoFrameFromVideo(path,i);
                    }
                }
            });*/

            // }
        }
    }
    private MappedByteBuffer loadModelFile(Activity activity, String MODEL_FILE) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_FILE);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }
    public float sigmoid(float value) {
        float p =  (float)(1.0 / (1 + Math.exp(-value)));
        return p;
    }
    private Bitmap imageProxyToBitmap(ImageProxy image)
    {
        ImageProxy.PlaneProxy planeProxy = image.getPlanes()[0];
        ByteBuffer buffer = planeProxy.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
    private Bitmap toBitmap(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];
        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, image.getWidth(), image.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 75, out);

        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }
    private String encodeImage(Bitmap bm)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);

        return encImage;
    }
    @SuppressLint("RestrictedApi")
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                // .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        HandlerThread analyzerThread = new HandlerThread("OpenCVAnalysis");
        analyzerThread.start();
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder().setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        /*ImageAnalysisConfig imageAnalysisConfig = new ImageAnalysisConfig.Builder()
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setCallbackHandler(new Handler(analyzerThread.getLooper()))
                .setImageQueueDepth(1).build();*/


        /*ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
                .setImageQueueDepth(50)
                .build();*/
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            File f=new File("myVideo");
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void analyze(@NonNull ImageProxy image) {
                int rotationDegrees = image.getImageInfo().getRotationDegrees();
                if(isRecording) {
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    Bitmap myBitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,null);
                    String str=encodeImage(myBitmap);
                    System.out.println(str);


                   /* if (image != null) {
                        //mService.processFrame(CameraX.this, surfaceView, image);
                        ModelClass modelClass = new ModelClass();
                        @SuppressLint("UnsafeExperimentalUsageError") Image im = image.getImage();
                        Image.Plane pl[] = im.getPlanes();

                        int format = im.getFormat();
                        byte[][] yuvBytes = modelClass.getYuvBytes();
                        modelClass.fillBytes(im.getPlanes(), yuvBytes);
                        int PREVIEW_WIDTH = 640;
                        int PREVIEW_HEIGHT = 480;
                        int[] rgbBytes = new int[PREVIEW_WIDTH * PREVIEW_HEIGHT];
                        modelClass.imageUtilWrapper(yuvBytes, PREVIEW_WIDTH, PREVIEW_HEIGHT, im, rgbBytes);
                        Bitmap imageBitmap = Bitmap.createBitmap(
                                rgbBytes, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                                Bitmap.Config.ARGB_8888
                        );
                        Matrix rotateMatrix = new Matrix();
                        rotateMatrix.postRotate(90.0f);
                        Bitmap rotatedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT,
                                rotateMatrix, true);
                        Bitmap croppedBitmap = modelClass.cropBitmap(rotatedBitmap);
                        // Bitmap croppedBitmap=modelClass.cropBitmap(modelClass.toBitmap(im));
                        Bitmap scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, MODEL_WIDTH, MODEL_HEIGHT, true);

                        ModelExecution modelExecution = new ModelExecution();
                        //modelExecution.init(CameraX.this);
                        modelExecution.init(CameraX.this);
                        Person p = modelExecution.call(scaledBitmap);


                        // val canvas: Canvas = surfaceHolder!!.lockCanvas()
                        Canvas canvas = surfaceView.getHolder().lockCanvas();
                        modelExecution.draw(canvas, p, scaledBitmap, surfaceView.getHolder());
                        count = 0;

                    }*/



                   /* videoCapture.startRecording(f,getBaseContext().getMainExecutor(), new VideoCapture.OnVideoSavedCallback() {
                        @Override
                        public void onVideoSaved(@NonNull File file) {
                            Log.i("video saved", "Video File : $file");
                        }

                        @Override
                        public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                            System.out.println("video not saved");
                        }
                    });*/
                    //here we will call posenet estimator
                    //08

                }
                else{
                    image.close();
                   // videoCapture.stopRecording();
                }
                System.out.println(isRecording);
                image.close();
                // insert your code here.
            }
        });
        ImageCapture.Builder builder = new ImageCapture.Builder();

        //Vendor-Extensions (The CameraX extensions dependency in build.gradle)
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);

        // Query if extension is available (optional).
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            // Enable the extension if available.
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        final ImageCapture imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();

        //mPreviewView.

        //preview.setSurfaceProvider(surfaceView.;
        preview.setSurfaceProvider(mPreviewView.createSurfaceProvider());

        camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, imageAnalysis, imageCapture);
        //camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview, videoCapture);
           // camera=cameraProvider.bindToLifecycle()


        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date())+ ".jpg");

                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback () {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Handler h=new Handler(Looper.getMainLooper());

                        h.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(CameraX.this, "Image Saved successfully", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onError(@NonNull ImageCaptureException error) {
                        error.printStackTrace();
                    }
                });
            }
        });
    }
    public String getBatchDirectoryName() {

        String app_folder_path = "";
        app_folder_path = Environment.getExternalStorageDirectory().toString() + "/images";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {

        }

        return app_folder_path;
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
    private String getOutputMediaFile(){
        String state= Environment.getExternalStorageState();
        if(!state.equals(Environment.MEDIA_MOUNTED)){
            return null;
        }
        else{
            File folder=new File(Environment.getExternalStorageDirectory()+File.separator+"GUI");
            if(!folder.exists()){
                folder.mkdir();

            }
            // File outputFile=new File(folder,"temp.3gp");
            return folder.getAbsolutePath()+"/temp.mp4";
            //return outputFile;
        }
    }
    private void initRecorder() {

        //recorder.setCamera((android.hardware.Camera) camera);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        CamcorderProfile cpHigh = CamcorderProfile
                .get(CamcorderProfile.QUALITY_HIGH);
        recorder.setProfile(cpHigh);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(getOutputMediaFile());
        recorder.setMaxDuration(50000); // 50 seconds
        recorder.setMaxFileSize(500000000); // Approximately 500 megabytes

    }
}
