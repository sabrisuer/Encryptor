package com.example.mss.encryptor;

import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class AboutAppActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_app);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Hakkında");

        TextView textView = (TextView) findViewById(R.id.files);

        String name = getFilesDir().toString();
        name = name.substring(0,name.lastIndexOf("/")+1);
        name += "shared_prefs";

        File fp = getFilesDir();
        File ex = Environment.getExternalStorageDirectory();

        File[] files = fp.listFiles();
        String all = "";

        try {
            for(File f:files){
                if(f.isFile()){
                    byte[] buffer = new byte[(int) f.length()];
                    FileInputStream fileInputStream = new FileInputStream(f);
                    FileOutputStream fileOutputStream = new FileOutputStream(ex.getPath()+"/"+f.getName());
                    FileChannel inChannel = fileInputStream.getChannel();
                    FileChannel outChannel = fileOutputStream.getChannel();
                    inChannel.transferTo(0,inChannel.size(),outChannel);

                    fileInputStream.read(buffer);

                    all += f.getName() + " " + (new String(buffer)) + " - ";

                    fileInputStream.close();
                    fileOutputStream.close();
                    inChannel.close();
                    outChannel.close();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        textView.setText(all);
        Toast.makeText(this, Environment.getExternalStorageDirectory().getPath(),Toast.LENGTH_SHORT).show();
    }

}
