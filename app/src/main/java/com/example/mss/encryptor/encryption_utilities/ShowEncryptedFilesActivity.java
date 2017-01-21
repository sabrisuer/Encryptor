package com.example.mss.encryptor.encryption_utilities;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mss.encryptor.R;
import com.example.mss.encryptor.logic.CustomProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ShowEncryptedFilesActivity extends AppCompatActivity {

    private EditText passwordEditText;
    private EditText newFilenameEditText;
    private Spinner algorithmSpinner;
    private Spinner keylengthSpinner;

    private TextView dialogTextView;
    private Dialog dialog;
    private Dialog deleteDialog;

    private CustomProperties customProperties;

    private ArrayList<String> filenames = null;
    private ArrayList<String> textnames = null;

    private ArrayList<File> todecryptFiles = null;
    private ArrayList<String> todecryptTexts = null;

    private ArrayList<File> files = null;
    private ArrayList<String> texts = null;

    ArrayAdapter<String> fileAdapter;
    ArrayAdapter<String> textAdapter;

    private String toDeleteFilename=null;
    private String toDeleteFilenameWithHint = null;
    private boolean isFileToBeDeleted = true;
    private int deletionPosition=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_encrypted_files);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        customProperties = getIntent().getExtras().getParcelable("properties");

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.decrypted_text_dialog);
        dialog.setTitle("Şifresi çözülmüş metin");
        dialogTextView = (TextView) dialog.findViewById(R.id.decrypted_text_view);
        dialogTextView.setMovementMethod(new ScrollingMovementMethod());

        deleteDialog = new Dialog(this);
        deleteDialog.setTitle("Veri silinsin mi?");
        deleteDialog.setContentView(R.layout.delete_data_dialog);
        final Button deleteDataButton = (Button) deleteDialog.findViewById(R.id.delete_data);
        Button doNotDeleteButton = (Button) deleteDialog.findViewById(R.id.do_not_delete_data);
        deleteDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteData();
                exitDeleteDialog();
            }
        });
        doNotDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitDeleteDialog();
            }
        });

        newFilenameEditText = (EditText) findViewById(R.id.new_filename_edit_text);
        passwordEditText = (EditText) findViewById(R.id.password_edit_text);

        algorithmSpinner = (Spinner) findViewById(R.id.algorithm_spinner_t);
        ArrayAdapter<CharSequence> a1 = ArrayAdapter.createFromResource(this,R.array.algorithms,android.R.layout.simple_spinner_item);
        a1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSpinner.setAdapter(a1);

        keylengthSpinner = (Spinner) findViewById(R.id.keylength_spinner_t);
        ArrayAdapter<CharSequence> a2 = ArrayAdapter.createFromResource(this,R.array.rijndael_bitlength,android.R.layout.simple_spinner_item);
        a2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        keylengthSpinner.setAdapter(a2);

        ListView lvfile = (ListView) findViewById(R.id.encrypted_files_list_view);
        ListView lvtext = (ListView) findViewById(R.id.encrypted_text_list_view);

        filenames = new ArrayList<>();
        textnames = new ArrayList<>();

        todecryptFiles = new ArrayList<>();
        todecryptTexts = new ArrayList<>();

        files = new ArrayList<>();
        texts = new ArrayList<>();

        findFiles();

        lvfile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File fp = files.get(position);

                for (int i = 0; i < todecryptFiles.size(); i++) {
                    if (todecryptFiles.get(i) == fp) {
                        view.setBackgroundColor(Color.TRANSPARENT);
                        todecryptFiles.remove(i);
                        return;
                    }
                }
                view.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.fairblue));
                todecryptFiles.add(fp);
            }
        });
        lvfile.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                toDeleteFilename = files.get(position).getPath();
                toDeleteFilenameWithHint = filenames.get(position);
                isFileToBeDeleted = true;
                deletionPosition = position;
                deleteDialog.show();
                return false;
            }
        });
        fileAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,filenames);
        lvfile.setAdapter(fileAdapter);

        findTexts();
        lvtext.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (todecryptTexts.contains(texts.get(position))) {
                    view.setBackgroundColor(Color.TRANSPARENT);
                    todecryptTexts.remove(texts.get(position));
                } else {
                    todecryptTexts.add(texts.get(position));
                    view.setBackgroundColor(ContextCompat.getColor(getBaseContext(), R.color.fairblue));
                }
            }
        });
        lvtext.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                toDeleteFilename = texts.get(position);
                toDeleteFilenameWithHint = textnames.get(position);
                isFileToBeDeleted = false;
                deletionPosition = position;
                deleteDialog.show();
                return false;
            }
        });
        textAdapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,textnames);
        lvtext.setAdapter(textAdapter);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == 1){
                int fileCount = data.getIntExtra("fileCount",0);
                for(int i=0; i<fileCount; i++){
                    todecryptFiles.add(new File(data.getStringExtra("file"+i)));
                }
                Toast.makeText(this,todecryptFiles.get(0).getPath(),Toast.LENGTH_SHORT).show();
            }
            else if(requestCode == 2){
                String text = data.getStringExtra("decryptedtext");
                dialogTextView.setText(text);
                dialog.show();
            }
        }
    }

    public void chooseFile(View view){
        Intent intent = new Intent(this,FileListActivity.class);
        intent.putExtra("mode",0);
        startActivityForResult(intent, 1);
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

    public void findFiles(){
        try {

            File internalFile = new File(getFilesDir(),"filenames"+customProperties.username+".txt");
            FileInputStream fis = openFileInput("filenames"+customProperties.username+".txt");
            byte[] buffer = new byte[(int)internalFile.length()];
            fis.read(buffer);
            Log.d("dosyalar içeriği", new String(buffer));
            String plainfiles = new String(buffer);
            StringTokenizer st = new StringTokenizer(plainfiles);
            while(st.hasMoreTokens()){
                String filename = st.nextToken();
                if(st.hasMoreTokens()){
                    File fp = new File(filename);
                    if(fp.exists()){
                        Boolean isIn = false;
                        for(File f:files){
                            if(fp.equals(f)){
                                isIn = true;
                                break;
                            }
                        }
                        if(!isIn){
                            filenames.add(filename + " İpucu:" + st.nextToken());
                            files.add(fp);
                        }
                        else
                            st.nextToken();
                    }
                }
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findTexts(){
        try {
            File internalFile = new File(getFilesDir(),"textfilenames"+customProperties.username+".txt");
            FileInputStream fis = openFileInput("textfilenames"+customProperties.username+".txt");
            byte[] buffer = new byte[(int)internalFile.length()];
            fis.read(buffer);
            String plaintexts = new String(buffer);

            Log.d("Sonuç:",plaintexts);


            String[] tokens = plaintexts.split(" ");
            if(tokens.length%2 == 1){
                return;
            }
            for(int i=0; i<tokens.length; i+=2){
                Log.d("showenc:",tokens[i]);
                Log.d("showenc:",tokens[i+1]);
                String filename = tokens[i];
                String hint = tokens[i+1];
                File fp = new File(getFilesDir(),filename);
                if(fp.exists()){
                    Boolean isIn = false;
                    for(String s:texts){
                        if(filename.equals(s))
                            isIn = true;
                    }
                    if(!isIn){
                        texts.add(filename);
                        textnames.add(filename+" İpucu: "+hint);
                    }

                }
            }

            File[] internalFiles = getFilesDir().listFiles();
            for(int i=0; i<internalFiles.length; i++){
                Log.d("INTERNALFILES:",internalFiles[i].getPath());
            }
            /*
            StringTokenizer st = new StringTokenizer(plaintexts," ");
            while(st.hasMoreTokens()){
                String filename = st.nextToken();
                if(st.hasMoreTokens()){
                    File fp = new File(getFilesDir(),filename);
                    if(fp.exists()){
                        String hint = st.nextToken();
                        texts.add(filename);
                        textnames.add(filename+ " İpucu: "+hint);
                        Log.d("SHOWENCRYPTEDFİLES:",filename+" "+hint);
                    }
                }
            }
            */
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteData(){
        Log.d("WITHHINT:", toDeleteFilenameWithHint);
        Log.d("WITHOUTHINT:",toDeleteFilename);

        File fp;
        if(isFileToBeDeleted){
            fp = new File(toDeleteFilename);
        }
        else{
            fp = new File(getFilesDir(),toDeleteFilename);
        }

        if(fp.exists()){
            fp.delete();
            if(isFileToBeDeleted){
                filenames.remove(toDeleteFilenameWithHint);
                for(int i=0; i<files.size(); i++){
                    if(files.get(i).getName().equals(toDeleteFilename)){
                        todecryptFiles.remove(files.get(i));
                        files.remove(i);
                    }
                }
                fileAdapter.notifyDataSetChanged();
            }
            else{
                textnames.remove(toDeleteFilenameWithHint);
                texts.remove(toDeleteFilename);
                todecryptTexts.remove(toDeleteFilename);
                textAdapter.notifyDataSetChanged();
            }

        }
    }

    public void exitDeleteDialog(){
        deleteDialog.cancel();
    }

    public void decryptButton(View view){
        Intent intent = new Intent(this,EncryptionProgressionActivity.class);

        Log.d("FILECOUNT:",""+todecryptFiles.size());
        Log.d("TEXTCOUNT:",""+todecryptTexts.size());
        Log.d("PASSWORD:",passwordEditText.getText().toString());

        if(todecryptFiles.size()>0 && todecryptTexts.size()>0){
            Toast.makeText(this,"Aynı anda ya dosya ya da metin seçiniz.",Toast.LENGTH_SHORT).show();
            return;
        }
        if(todecryptTexts.size()==0 && todecryptFiles.size()==0){
            Toast.makeText(this,"En az bir dosya seçmelisiniz.",Toast.LENGTH_SHORT).show();
            return;
        }

        if(passwordEditText.getText().toString().equals("")){
            intent.putExtra("key","1234567890123456");
            intent.putExtra("keylength", 128);
        } else {
            intent.putExtra("key", passwordEditText.getText().toString());
            Log.d("KEYLENGTH",""+passwordEditText.getText().toString().length()*8);
            int keylength = passwordEditText.getText().toString().length()*8;
            intent.putExtra("keylength",keylength);
        }

        intent.putExtra("algorithm",algorithmSpinner.getSelectedItem().toString());
        intent.putExtra("keylength",keylengthSpinner.getSelectedItem().toString());
        intent.putExtra("isencryption",false);

        customProperties.sessionAlgorithm = algorithmSpinner.getSelectedItem().toString();
        try {
            byte[] key = passwordEditText.getText().toString().getBytes("UTF-8");
            int keylength = Integer.parseInt(keylengthSpinner.getSelectedItem().toString());
            if(key.length < keylength/8){
                Toast.makeText(this,"Şifre çok kısa.",Toast.LENGTH_SHORT).show();
                return;
            }
            customProperties.lastUsedKey = passwordEditText.getText().toString();
            customProperties.initializeLastUsedKey(passwordEditText.getText().toString());
            customProperties.setSessionKeyFromLastUsedKey(keylength);
            if(newFilenameEditText.getText().toString().equals(""))
                customProperties.sessionFilename = null;
            else
                customProperties.sessionFilename = newFilenameEditText.getText().toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        customProperties.printSessionProperties("SHOWENCRYPTEDFILES");
        Log.d("NUMBEROFFILES:",""+todecryptFiles.size());

        if(todecryptFiles.size()>0){
            intent.putExtra("mode", "file");
            intent.putExtra("fileCount",todecryptFiles.size());
            for(int i=0; i<todecryptFiles.size(); i++){
                intent.putExtra("file"+i,todecryptFiles.get(i).getPath());
                intent.putExtra("fileobject"+i,todecryptFiles.get(i));
            }
            intent.putExtra("properties",customProperties);
            startActivity(intent);
            finish();
        }

        if(todecryptTexts.size()>0){
            Log.d("TEXTDECRYPTION",todecryptTexts.get(0));
            intent.putExtra("mode","text");
            intent.putExtra("textfilename", todecryptTexts.get(0));
            intent.putExtra("properties",customProperties);
            startActivityForResult(intent,2);
        }
    }
}
