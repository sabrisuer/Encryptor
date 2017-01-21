package com.example.mss.encryptor.encryption_utilities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mss.encryptor.R;
import com.example.mss.encryptor.logic.CustomProperties;
import com.example.mss.encryptor.logic.algorithms.gnu.crypto.cipher.Rijndael;
import com.example.mss.encryptor.logic.algorithms.gnu.crypto.cipher.Serpent;
import com.example.mss.encryptor.logic.algorithms.gnu.crypto.cipher.Twofish;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.util.ArrayList;

public class EncryptionProgressionActivity extends AppCompatActivity {

    private long totalSize = 0;
    private long progressSize = 0;

    private ProgressBar mProgress;
    private int mProgressStatus = 0;
    private Handler mHandler = new Handler();

    private CustomProperties customProperties;

    private final ArrayList<File> files = new ArrayList<>();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encryption_progression);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);


        final Bundle data = getIntent().getExtras();
        customProperties = data.getParcelable("properties");

        final boolean isencryption = data.getBoolean("isencryption");
        final String mode = data.getString("mode");
        final int fileCount = data.getInt("fileCount");

        final String textfilename;
        if(isencryption)
            textfilename = customProperties.sessionFilename;
        else
            textfilename = data.getString("textfilename");

        if(mode == null){
            finish();
            return;
        }
        if(mode.equals("text") && textfilename == null){
            Log.d("SORUN:","EncryptionProgression - textfilename sorunu");
            this.finish();
            return;
        }


        final ArrayList<File> files = new ArrayList<>();
        switch (mode) {
            case "file":
                for (int i = 0; i < fileCount; i++) {
                    File f = (File) data.getSerializable("fileobject" + i);
                    files.add(f);
                    totalSize += f != null ? f.length() : 0;
                }
                break;
            case "text":
                Log.d("TEXTFILENAME:",textfilename);
                File f = new File(getFilesDir(),textfilename);
                files.add(f);
                totalSize = f.length();
                break;
            default:
                finish();
                break;
        }


        mProgress = (ProgressBar) findViewById(R.id.progressBar);
        Drawable drawable = getResources().getDrawable(R.drawable.custom_progress_bar);
        mProgress.setProgressDrawable(drawable);


        new Thread(new Runnable() {

            String algorithm = customProperties.sessionAlgorithm;
            int keylength = customProperties.sessionKeyLength;
            byte[] key = customProperties.sessionKey;
            String hint = customProperties.sessionHint;

            TextView view = (TextView) findViewById(R.id.files_in_progress_text_view);
            Button backToMain = (Button) findViewById(R.id.back_to_main);
            String filename;
            File saveDirectory;

            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(progressSize<totalSize){
                            runOnUiThread(new Runnable() {
                                final TextView textViewPercent = (TextView) findViewById(R.id.edit_text_progression_percentage);
                                final TextView textViewSize = (TextView) findViewById(R.id.edit_text_progression_size);

                                long percentage = 0;

                                @Override
                                public void run() {
                                    percentage = progressSize*100/totalSize;
                                    textViewSize.setText("" + progressSize + "B/" + totalSize + "B");
                                    textViewPercent.setText("" + percentage + "%");
                                    //mProgressStatus = (int)progressSize*100/(int)totalSize;
                                    mProgress.setProgress((int) percentage);
                                }
                            });
                        }
                    }
                }).start();

                showLog("THREADALGORİTHM:", algorithm);
                showLog("THREADKEYLENGTH:",""+keylength);
                if(key != null)
                    showLog("THREADKEY:",new String(key));
                else
                    showLog("THREADKEY:","null");
                if(hint != null)
                    showLog("THREADHINT:",hint);
                else
                    showLog("THREADHINT:","null");


                if (isencryption)
                    showFiles(view,"Şifreleniyor...");
                else
                    showFiles(view,"Deşifre ediliyor...");

                ArrayList<String> filenames = new ArrayList<String>();
                if(mode.equals("file")){
                    int i=0;
                    while(i<files.size()){
                        showFiles(view,files.get(i).getPath());
                        if(isencryption){
                            if(customProperties.sessionFilename == null){
                                filename = files.get(i).getName() + ".enc";
                            }
                            else{
                                if(fileCount==1){
                                    filename = customProperties.sessionFilename+".enc";
                                }
                                else{
                                    filename = customProperties.sessionFilename+i+".enc";
                                }

                            }
                            filenames.add(filename);
                        }
                        else{
                            if(customProperties.sessionFilename == null){
                                if(files.get(i).getName().contains(".enc")){
                                    int until = files.get(i).getName().lastIndexOf(".");
                                    filename ="dec"+ files.get(i).getName().substring(0,until);
                                }
                                else
                                    filename = files.get(i).getName();
                            }
                            else
                                filename = customProperties.sessionFilename;
                        }

                        if(customProperties.sessionSaveDirectory == null)
                            saveDirectory = files.get(i).getParentFile();
                        else
                            saveDirectory = customProperties.sessionSaveDirectory;

                        switch (algorithm) {
                            case "AES":
                                if (isencryption)
                                    fileEncryption(1, keylength, key, files.get(i), saveDirectory, filename);
                                else
                                    fileDecryption(1, keylength, key, files.get(i), saveDirectory, filename);
                                break;
                            case "Twofish":
                                if (isencryption)
                                    fileEncryption(2, keylength, key, files.get(i), saveDirectory, filename);
                                else
                                    fileDecryption(2, keylength, key, files.get(i), saveDirectory, filename);
                                break;
                            case "Serpent":
                                if (isencryption)
                                    fileEncryption(3, keylength, key, files.get(i), saveDirectory, filename);
                                else
                                    fileDecryption(3, keylength, key, files.get(i), saveDirectory, filename);
                                break;
                        }
                        i++;
                    }
                }
                else if(mode.equals("text")){
                    String text=null;
                    if(isencryption) {
                        text = data.getString("text");
                        Log.d("ALINAN:",text);
                    }
                    switch (algorithm) {
                        case "AES":
                            if (isencryption)
                                textEncryption(1, keylength, key, text, getFilesDir(), textfilename);
                            else
                                textDecryption(1, keylength, key, new File(getFilesDir(),textfilename));
                            break;
                        case "Twofish":
                            if (isencryption)
                                textEncryption(2, keylength, key, text, getFilesDir(), textfilename);
                            else
                                textDecryption(2, keylength, key, new File(getFilesDir(),textfilename));
                            break;
                        case "Serpent":
                            if (isencryption)
                                textEncryption(3, keylength, key, text, getFilesDir(), textfilename);
                            else
                                textDecryption(3, keylength, key, new File(getFilesDir(),textfilename));
                            break;
                    }
                    //mProgress.setProgress(100);
                    showFiles(view, "Metin şifrelendi.");
                }


                if(isencryption){
                    if(mode.equals("file")){
                        saveFileNames(saveDirectory,files,hint,filenames);
                    }
                    else if(mode.equals("text")){
                        saveTextName(files.get(0),hint);
                    }
                    showFiles(view,"Veriler başarılı bir şekilde şifrelendi.");
                }
                else{
                    showFiles(view,"Veriler başarılı bir şekilde deşifre edildi.");
                }
                showButton(backToMain);

            }

        }).start();

    }

    // 1-aes, 2-twofish, 3-serpent
    public void fileEncryption(int alg, int keylength, byte[] keybytes, File inFile, File saveDirectory, String outFilename){

        try{
            File outFile = new File(saveDirectory,outFilename);

            FileInputStream fileInputStream = new FileInputStream(inFile);
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);

            switch (alg){
                case 1:
                    aesEncryptionDecryption(keylength, keybytes, fileInputStream, fileOutputStream, true);
                    break;
                case 2:
                    twofishEncryptionDecryption(keylength,keybytes,fileInputStream,fileOutputStream,true);
                    break;
                case 3:
                    serpentEncryptionDecryption(keylength,keybytes,fileInputStream,fileOutputStream,true);
                    break;
            }

            fileInputStream.close();
            fileOutputStream.close();

            showToast(""+inFile.getName()+" isimli dosya şifrelendi.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fileDecryption(int alg, int keylength, byte[] keybytes, File inFile, File saveDirectory, String outFilename) {

        try {
            File outFile = new File(saveDirectory, outFilename);

            FileInputStream fileInputStream = new FileInputStream(inFile);
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);

            switch (alg) {
                case 1:
                    aesEncryptionDecryption(keylength, keybytes, fileInputStream, fileOutputStream, false);
                    break;
                case 2:
                    twofishEncryptionDecryption(keylength, keybytes, fileInputStream, fileOutputStream, false);
                    break;
                case 3:
                    serpentEncryptionDecryption(keylength, keybytes, fileInputStream, fileOutputStream, false);
                    break;
            }

            fileInputStream.close();
            fileOutputStream.close();

            showToast("" + inFile.getName() + " isimli dosyanın şifresi çözüldü.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void textEncryption(int alg, int keylength, byte[] keybytes, String inText, File saveDirectory, String outFilename) {

        try {
            File outFile = new File(saveDirectory, outFilename);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inText.getBytes("UTF-8"));
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);

            switch (alg) {
                case 1:
                    aesEncryptionDecryption(keylength, keybytes, byteArrayInputStream, fileOutputStream, true);
                    break;
                case 2:
                    twofishEncryptionDecryption(keylength, keybytes, byteArrayInputStream, fileOutputStream, true);
                    break;
                case 3:
                    serpentEncryptionDecryption(keylength, keybytes, byteArrayInputStream, fileOutputStream, true);
                    break;
            }

            byteArrayInputStream.close();
            fileOutputStream.close();

            showToast("Metin " + outFilename + " şifrelenerek isimli dosyaya kaydedildi.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void textDecryption(int alg, int keylength, byte[] keybytes, File inFile) {

        try {
            String outText;

            FileInputStream fileInputStream = new FileInputStream(inFile);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            switch (alg) {
                case 1:
                    aesEncryptionDecryption(keylength, keybytes, fileInputStream, byteArrayOutputStream, false);
                    break;
                case 2:
                    twofishEncryptionDecryption(keylength, keybytes, fileInputStream, byteArrayOutputStream, false);
                    break;
                case 3:
                    serpentEncryptionDecryption(keylength, keybytes, fileInputStream, byteArrayOutputStream, false);
                    break;
            }

            fileInputStream.close();
            outText = byteArrayOutputStream.toString("UTF-8");
            byteArrayOutputStream.close();

            showToast("" + inFile.getName() + " isimli dosya deşifre edildi.");


            Intent retintent = new Intent();
            retintent.putExtra("decryptedtext", outText);
            setResult(Activity.RESULT_OK, retintent);
            finish();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showToast(final String toast){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showLog(String tag, String message){
        Log.d(tag, message);
    }

    public void showFiles(final TextView view, final String filename){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setText(filename);
            }
        });
    }

    public void showButton(final Button view){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
            }
        });
    }

    public void backToMainMenu(View view){
        finish();
    }

    public void aesEncryptionDecryption(int keylength, byte[] key, InputStream inputStream, OutputStream outputStream, boolean isEncryption){
        try{
            Rijndael rijndael = new Rijndael();
            Log.d("KEYLENGTH:",""+(keylength/8));
            Object keyObject = rijndael.makeKey(key,keylength/8);

            byte[] inBytes = new byte[16];
            byte[] outBytes = new byte[16];

            int read = inputStream.read(inBytes);

            while(read != -1){
                if(read < 16){
                    for(int i=read; i<16; i++){
                        inBytes[i] = 32;
                    }
                }

                if(isEncryption)
                    rijndael.encrypt(inBytes,0,outBytes,0,keyObject,16);
                else
                    rijndael.decrypt(inBytes,0,outBytes,0,keyObject,16);

                progressSize += 16;

                outputStream.write(outBytes);
                read = inputStream.read(inBytes);

                Log.d("ILK 16 BYTE in:", new String(inBytes));
                Log.d("ILK 16 BYTE out:", new String(outBytes));

            }
            outputStream.flush();

        } catch (InvalidKeyException | IOException e) {
            e.printStackTrace();
        }
    }

    public void twofishEncryptionDecryption(int keylength, byte[] key, InputStream inputStream, OutputStream outputStream, boolean isEncryption) {
        try {
            Twofish twofish = new Twofish();
            Log.d("KEYLENGTH:",""+(keylength/8));
            Object keyObject = twofish.makeKey(key, keylength / 8);

            byte[] inBytes = new byte[16];
            byte[] outBytes = new byte[16];

            int read = inputStream.read(inBytes);

            while (read != -1) {
                if (read < 16) {
                    for (int i = read; i < 16; i++) {
                        inBytes[i] = 0;
                    }
                }

                if (isEncryption)
                    twofish.encrypt(inBytes, 0, outBytes, 0, keyObject, 16);
                else
                    twofish.decrypt(inBytes, 0, outBytes, 0, keyObject, 16);

                progressSize += 16;

                outputStream.write(outBytes);
                read = inputStream.read(inBytes);

                Log.d("ILK 16 BYTE in:", new String(inBytes));
                Log.d("ILK 16 BYTE out:", new String(outBytes));
            }
            outputStream.flush();

        } catch (InvalidKeyException | IOException e) {
            e.printStackTrace();
        }
    }

    public void serpentEncryptionDecryption(int keylength, byte[] key, InputStream inputStream, OutputStream outputStream, boolean isEncryption) {
        try {
            Serpent serpent = new Serpent();
            Object keyObject = serpent.makeKey(key, keylength / 8);

            byte[] inBytes = new byte[16];
            byte[] outBytes = new byte[16];

            int read = inputStream.read(inBytes);

            while (read != -1) {
                if (read < 16) {
                    for (int i = read; i < 16; i++) {
                        inBytes[i] = 0;
                    }
                }

                if (isEncryption)
                    serpent.encrypt(inBytes, 0, outBytes, 0, keyObject, 16);
                else
                    serpent.decrypt(inBytes, 0, outBytes, 0, keyObject, 16);

                progressSize += 16;

                outputStream.write(outBytes);
                read = inputStream.read(inBytes);

                Log.d("ILK 16 BYTE in:", new String(inBytes));
                Log.d("ILK 16 BYTE out:", new String(outBytes));
            }
            outputStream.flush();

        } catch (InvalidKeyException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveFileNames(File saveDirectory, ArrayList<File> files, String hint, ArrayList<String> filenames){

        if(hint == null)
            hint = "yok";

        if(files.size()==0)
            return;
        if(saveDirectory == null){
            try {
                FileOutputStream fos = openFileOutput("filenames"+ customProperties.username+".txt",MODE_APPEND);
                for(int i = 0; i<files.size(); i++){
                    String name = files.get(i).getParent()+"/"+ filenames.get(i)+" "+hint+" ";
                    fos.write(name.getBytes());
                }
                fos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        else{
            try {
                FileOutputStream fos = openFileOutput("filenames"+customProperties.username+".txt",MODE_APPEND);
                for(int i = 0; i<files.size(); i++){
                    String name = saveDirectory.getPath()+"/"+filenames.get(i) +" "+hint+" ";
                    fos.write(name.getBytes());
                }
                fos.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public void saveTextName(File textFile, String hint){
        if(textFile==null)
            return;
        try {
            FileOutputStream fos = openFileOutput("textfilenames"+customProperties.username+".txt",MODE_APPEND);
            String name = textFile.getName() + " " + hint + " ";
            fos.write(name.getBytes());
            fos.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

}
