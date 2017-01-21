package com.example.mss.encryptor.encryption_utilities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.mss.encryptor.R;
import com.example.mss.encryptor.logic.FileAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class FileListActivity extends AppCompatActivity {

    ArrayList<File> files;
    ArrayList<File> sendfiles;
    ArrayList<String> filenames;
    File parentfile;
    ArrayAdapter<String> arrayAdapter;
    BaseAdapter fileAdapter;
    ListView lv;
    int level;
    int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        level = 0;
        mode = getIntent().getIntExtra("mode",0);

        if(mode==1){
            Button sendButton = (Button) findViewById(R.id.file_list_view_button);
            sendButton.setText(R.string.select_directory_button);
            toolbar.setTitle(R.string.select_file_save_location_button);
        }
        setSupportActionBar(toolbar);

        lv = (ListView) findViewById(R.id.file_list_view);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                level++;


                File file = files.get(position);

                Log.d("SELECTEDFILE:",file.getPath());


                if(file==null)
                    return;

                if(file.isFile()){
                    if(mode==1){
                        return;
                    }

                    if(sendfiles.remove(file)){
                        view.setBackgroundColor(Color.TRANSPARENT);
                        return;
                    }

                    view.setBackgroundColor(ContextCompat.getColor(getBaseContext(),R.color.fairblue));
                    sendfiles.add(file);

                }
                if(file.isDirectory()){
                    parentfile = file;
                    loadNewFiles(parentfile);
                    fileAdapter.notifyDataSetChanged();



                }
            }
        });

        files = new ArrayList<File>();
        loadNewFiles(Environment.getExternalStorageDirectory().getParentFile());

        sendfiles = new ArrayList<File>();

        fileAdapter = new FileAdapter(this,files);
        lv.setAdapter(fileAdapter);

    }

    @Override
    public void onBackPressed(){
        if(level==0){
            finish();
        }
        else{
            level--;
            parentfile = parentfile.getParentFile();
            loadNewFiles(parentfile);
            for(int i=0; i<sendfiles.size(); i++){
                getViewByPosition(i,lv).setBackgroundColor(Color.TRANSPARENT);
            }

            fileAdapter.notifyDataSetChanged();
        }

    }

    private void loadNewFiles(File parent){

        if(parent == null){
            return;
        }

        files.clear();
        File[] fileList = parent.listFiles();
        if(fileList == null){
            return;
        }

        int fileCount=0, dirCount=0;

        for(File f: fileList){
            if(f.isDirectory())
                dirCount++;
            else
                fileCount++;
        }
        File[] fileArray = new File[fileCount];
        File[] dirArray = new File[dirCount];
        int i=0, j=0;
        for(File f: fileList){
            if(f.isDirectory())
                dirArray[i++] = f;
            else{
                fileArray[j++] = f;
            }

        }
        Arrays.sort(fileArray);
        Arrays.sort(dirArray);

        Collections.addAll(files, dirArray);
        Collections.addAll(files, fileArray);
    }

    public void chooseButton(View view){

        Intent retIntent = new Intent();

        if(mode==1){
            if(level==0){
                Toast.makeText(this,R.string.first_directory_selected,Toast.LENGTH_SHORT).show();
                return;
            }
            else{
                retIntent.putExtra("dir",parentfile.toString());
            }
        }
        else{
            retIntent.putExtra("fileCount", sendfiles.size());
            for(int i=0; i<sendfiles.size(); i++){
                retIntent.putExtra("file"+i,sendfiles.get(i).toString());
                retIntent.putExtra("fileobject"+i,sendfiles.get(i));
            }
        }

        setResult(Activity.RESULT_OK,retIntent);
        finish();
    }

    public View getViewByPosition(int position, ListView listView){
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount()-1;
        if(position< firstListItemPosition || position > lastListItemPosition){
            return listView.getAdapter().getView(position,null,listView);
        }
        else{
            final int childIndex = position - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

}
