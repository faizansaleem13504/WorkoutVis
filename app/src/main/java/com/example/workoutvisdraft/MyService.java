package com.example.workoutvisdraft;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Binder;
import android.os.IBinder;
import android.view.SurfaceView;

import androidx.annotation.Nullable;
import androidx.camera.core.ImageProxy;

import static com.example.workoutvisdraft.ConstantsKt.MODEL_HEIGHT;
import static com.example.workoutvisdraft.ConstantsKt.MODEL_WIDTH;

public class MyService extends Service {
    private final IBinder binder = new LocalBinder();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
    public class LocalBinder extends Binder {
        MyService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MyService.this;
        }
    }
    public void processFrame(Context context, SurfaceView surfaceView, ImageProxy image) {
       /* System.out.println("We are recording");
        Log.i("recording on","its one");
        ModelClass modelClass=new ModelClass();
        @SuppressLint("UnsafeExperimentalUsageError") Image im=image.getImage();
        Image.Plane pl[]=im.getPlanes();

        int format=im.getFormat();
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
        //modelExecution.init(CameraX.this);
        modelExecution.init(context);
        Person p=modelExecution.call(scaledBitmap);


        // val canvas: Canvas = surfaceHolder!!.lockCanvas()
        Canvas canvas=surfaceView.getHolder().lockCanvas();
        modelExecution.draw(canvas, p, scaledBitmap,surfaceView.getHolder());*/
        RealtimeFrameThread rft=new RealtimeFrameThread(context,surfaceView,image);
        rft.start();

    }
    class RealtimeFrameThread extends Thread {
        long minPrime;
        Context c;
        SurfaceView surfaceView;
        ImageProxy image;
        RealtimeFrameThread(Context context, SurfaceView surfaceView, ImageProxy image) {
            c=context;this.surfaceView=surfaceView;this.image=image;

        }

        public void run() {
            ModelClass modelClass=new ModelClass();
            @SuppressLint("UnsafeExperimentalUsageError") Image im=image.getImage();
            Image.Plane pl[]=im.getPlanes();
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
            //modelExecution.init(CameraX.this);
            modelExecution.init(c);
            Person p=modelExecution.call(scaledBitmap);


            // val canvas: Canvas = surfaceHolder!!.lockCanvas()
            Canvas canvas=surfaceView.getHolder().lockCanvas();
            modelExecution.draw(canvas, p, scaledBitmap,surfaceView.getHolder());
        }
    }

    class PrimeThread extends Thread {
        long minPrime;
        Context c;
        SurfaceView surfaceView;
        Bitmap bitmap;
        PrimeThread(Context context, SurfaceView surfaceView, Bitmap bitmap) {
            c=context;this.surfaceView=surfaceView;this.bitmap=bitmap;

        }

        public void run() {
            ModelClass modelClass=new ModelClass();
            Bitmap croppedBitmap=modelClass.cropBitmap(bitmap);
            // Bitmap croppedBitmap=modelClass.cropBitmap(modelClass.toBitmap(im));
            Bitmap scaledBitmap=Bitmap.createScaledBitmap(croppedBitmap,MODEL_WIDTH,MODEL_HEIGHT,true);

            ModelExecution modelExecution=new ModelExecution();
            //modelExecution.init(CameraX.this);
            modelExecution.init(c);
            Person p=modelExecution.call(scaledBitmap);


            // val canvas: Canvas = surfaceHolder!!.lockCanvas()

            Canvas canvas = surfaceView.getHolder().lockCanvas();
            if(canvas!=null) {
                modelExecution.draw(canvas, p, scaledBitmap, surfaceView.getHolder());
            }
            // compute primes larger than minPrime
              //. . .
        }
    }
    public void processVideoFrame(Context context, SurfaceView surfaceView, Bitmap bitmap) {
       // System.out.println("We are recording");
       // Log.i("recording on","its one");
        PrimeThread p=new PrimeThread(context,surfaceView,bitmap);
        p.start();

       // @SuppressLint("UnsafeExperimentalUsageError") Image im=image.getImage();
       // Image.Plane pl[]=im.getPlanes();

       // int format=im.getFormat();
       // byte[][] yuvBytes=modelClass.getYuvBytes();
       // modelClass.fillBytes(im.getPlanes(),yuvBytes);
       // int PREVIEW_WIDTH = 640;
       // int PREVIEW_HEIGHT = 480;
       // int []rgbBytes=new int[PREVIEW_WIDTH*PREVIEW_HEIGHT];
       // modelClass.imageUtilWrapper(yuvBytes,PREVIEW_WIDTH,PREVIEW_HEIGHT,im,rgbBytes);
        //Bitmap imageBitmap = Bitmap.createBitmap(
        //        rgbBytes, PREVIEW_WIDTH, PREVIEW_HEIGHT,
        //        Bitmap.Config.ARGB_8888
        //);
        //Matrix rotateMatrix=new Matrix();
        //rotateMatrix.postRotate(90.0f);
        //Bitmap rotatedBitmap=Bitmap.createBitmap(imageBitmap, 0, 0, PREVIEW_WIDTH, PREVIEW_HEIGHT,
        //        rotateMatrix, true);
        ////////////////////////////////////////////////////

    }
}
