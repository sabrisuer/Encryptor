package com.example.mss.encryptor.encryption_activities;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by mss on 16/01/16.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHodler;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera){
        super(context);
        mCamera = camera;

        mHodler = getHolder();
        mHodler.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder){
        try{
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }catch (IOException e){
            Log.d("Sorun", "Camerareview SurfaceCreated");
            e.printStackTrace();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder){

    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHodler.getSurface() == null) {
            return;
        }

        try{
            mCamera.stopPreview();
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        }catch (Exception e){
            Log.d("Sorun:","SurfaceChanged");
            e.printStackTrace();
        }
    }
}
