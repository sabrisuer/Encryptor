package com.example.mss.encryptor.encryption_activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.mss.encryptor.R;
import com.example.mss.encryptor.encryption_utilities.EncryptionProgressionActivity;
import com.example.mss.encryptor.logic.CustomProperties;
import com.example.mss.encryptor.logic.algorithms.gnu.crypto.cipher.BaseCipher;
import com.example.mss.encryptor.logic.algorithms.gnu.crypto.cipher.Rijndael;
import com.example.mss.encryptor.logic.algorithms.gnu.crypto.cipher.Serpent;
import com.example.mss.encryptor.logic.algorithms.gnu.crypto.cipher.Twofish;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by mss on 16/01/16.
 */
public class CameraActivity extends Activity {
    private Camera mCamera;
    private CameraPreview mPreview;

    private Button captureButton;

    private CustomProperties customProperties;

    private boolean isRecording = false;

    private String mode = null;
    private int photoCount = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);

        customProperties = getIntent().getExtras().getParcelable("properties");
        mode = getIntent().getExtras().getString("mode");


        final Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.setStreamVolume(AudioManager.STREAM_RING, audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
                audioManager.playSoundEffect(AudioManager.FLAG_PLAY_SOUND);
            }
        };

        captureButton = (Button) findViewById(R.id.button_capture);

        mCamera = getCameraInstance();

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mode.equals("picture") || mode.equals("photo")) {
                    mCamera.takePicture(shutterCallback, null, mPicture);
                }

            }
        });

        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e) {
            Log.d("KAMERAAÇMA", "Sorun oluştu...");
            e.printStackTrace();
        }
        return c;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            final String alg = customProperties.sessionAlgorithm;
            final int keyLength = customProperties.sessionKeyLength;
            final byte[] key = customProperties.sessionKey;
            String hint = customProperties.sessionHint;
            File saveDirectory = customProperties.sessionSaveDirectory;
            String filename = customProperties.sessionFilename;

            final byte[] tmpdata = Arrays.copyOf(data, data.length);

            if (filename == null) {
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy-HH-mm-ss");
                filename = sdf.format(date);
            }
            if (hint == null) {
                hint = "yok";
            }

            final EncryptionProgressionActivity encryptionProgressionActivity = new EncryptionProgressionActivity();
            final File outFile = new File(saveDirectory, filename + photoCount + ".enc");

            final ProgressDialog progress = ProgressDialog.show(CameraActivity.this, "Şifreleniyor.", "Dosya boyutu:" + data.length, true, false);
            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(tmpdata);
                        FileOutputStream outputStream = new FileOutputStream(outFile);

                        switch (alg) {
                            case "AES":
                                encryptionProgressionActivity.aesEncryptionDecryption(keyLength, key, byteArrayInputStream, outputStream, true);
                                break;
                            case "Twofish":
                                encryptionProgressionActivity.twofishEncryptionDecryption(keyLength, key, byteArrayInputStream, outputStream, true);
                                break;
                            case "Serpent":
                                encryptionProgressionActivity.serpentEncryptionDecryption(keyLength, key, byteArrayInputStream, outputStream, true);
                                break;
                        }

                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.setMessage("Şifreleme tamamlandı.");
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            progress.dismiss();
                        }
                    });
                }
            }).start();

            saveFileName(saveDirectory, filename, hint);

            camera.stopPreview();
            camera.startPreview();
        }
    };


    public void saveFileName(File saveDirectory, String filename, String hint) {
        try {
            FileOutputStream fos = openFileOutput("filenames" + customProperties.username + ".txt", MODE_APPEND);
            String name = saveDirectory.getPath() + "/" + filename + photoCount + ".enc " + hint + " ";
            photoCount++;
            fos.write(name.getBytes());
            fos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
