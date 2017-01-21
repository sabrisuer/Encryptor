package com.example.mss.encryptor.encryption_activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mss.encryptor.encryption_utilities.EncryptionProgressionActivity;
import com.example.mss.encryptor.encryption_utilities.FileListActivity;
import com.example.mss.encryptor.R;
import com.example.mss.encryptor.logic.CustomProperties;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class FileEncryptionActivity extends AppCompatActivity {

    private TextView selectedFilesView;
    private LinearLayout optionsLayout;
    private Spinner algorithmSpinner;
    private Spinner keylengthSpinner;
    private RadioGroup keyRadioGroup;
    private RadioButton checkedRadioButton;
    private EditText newKeyEditText;
    private LinearLayout newKeyLayout;

    private CustomProperties customProperties;

    boolean isVisible;

    ArrayList<File> files = new ArrayList<>();
    String savedir = null;

    private EditText hintEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_encryption);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        customProperties = getIntent().getExtras().getParcelable("properties");

        hintEditText = (EditText) findViewById(R.id.hint);

        selectedFilesView = (TextView) findViewById(R.id.result);
        optionsLayout = (LinearLayout) findViewById(R.id.options_layout);
        optionsLayout.setVisibility(View.GONE);
        isVisible = false;

        algorithmSpinner = (Spinner) findViewById(R.id.algorithm_spinner);
        keylengthSpinner = (Spinner) findViewById(R.id.keylength_spinner);

        ArrayAdapter<CharSequence> algArrayAdapter = ArrayAdapter.createFromResource(this,R.array.algorithms,android.R.layout.simple_spinner_item);
        algArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        algorithmSpinner.setAdapter(algArrayAdapter);


        algorithmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            ArrayAdapter<CharSequence> keyArrayAdapter;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    keyArrayAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.rijndael_bitlength, android.R.layout.simple_spinner_item);
                } else if (position == 1) {
                    keyArrayAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.twofish_bitlength, android.R.layout.simple_spinner_item);
                } else if (position == 2) {
                    keyArrayAdapter = ArrayAdapter.createFromResource(getBaseContext(), R.array.serpent_bitlength, android.R.layout.simple_spinner_item);
                }
                keylengthSpinner.setAdapter(keyArrayAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        keyRadioGroup = (RadioGroup) findViewById(R.id.key_option_radio_group);
        keyRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                checkedRadioButton = (RadioButton) radioGroup.findViewById(id);
                if (checkedRadioButton.getText().equals("Giriş şifresi")) {
                    newKeyLayout.setVisibility(View.GONE);
                } else if (checkedRadioButton.getText().equals("Son kullanılan şifre")) {
                    newKeyLayout.setVisibility(View.GONE);
                } else if (checkedRadioButton.getText().equals("Yeni şifre")) {
                    newKeyLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        RadioButton radioButton = (RadioButton) findViewById(R.id.radioButton);
        if(customProperties.getLoginKey(128) == null){
            radioButton.setEnabled(false);
        }

        RadioButton radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
        if(customProperties.getLastUsedKey(128) == null){
            radioButton2.setEnabled(false);
        }

        newKeyLayout = (LinearLayout) findViewById(R.id.new_key_layout);
        newKeyLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case 0:
                    Uri uri = data.getData();
                    selectedFilesView.setText(uri.toString());

                case 1:
                    int fileCount = data.getIntExtra("fileCount",0);

                    for(int i=0;i<fileCount;i++){
                        files.add((File)data.getSerializableExtra("fileobject"+i));
                    }

                    String totalText=""+fileCount+" adet dosya seçildi."+System.getProperty("line.separator");
                    for(int i=0;i<files.size();i++){
                        totalText +=files.get(i).getPath() + System.getProperty("line.separator");
                    }
                    selectedFilesView.setText(totalText);

                case 2:
                    TextView textView = (TextView) findViewById(R.id.dirTextView);
                    textView.setText(data.getStringExtra("dir"));
                    savedir = data.getStringExtra("dir");
            }
        }
    }

    public void chooseFile(View view){
        Intent intent = new Intent(this,FileListActivity.class);
        startActivityForResult(intent, 1);
    }

    public void chooseDirectory(View view){
        Intent intent = new Intent(this,FileListActivity.class);
        intent.putExtra("mode",1);
        startActivityForResult(intent, 2);
    }

    public void expandContractOptions(View view){
        if(isVisible){
            optionsLayout.setVisibility(View.GONE);
            isVisible=false;
        }
        else{
            optionsLayout.setVisibility(View.VISIBLE);
            isVisible=true;
        }
    }

    public void encryptFiles(View view){

        if(files.size()<1){
            Toast.makeText(this,R.string.no_files_selected,Toast.LENGTH_SHORT).show();
        }
        else{
            Intent intent = new Intent(this,EncryptionProgressionActivity.class);
            intent.putExtra("mode","file");
            intent.putExtra("isencryption",true);

            int keylength = 0;

            if(optionsLayout.getVisibility() == View.GONE){

                try {
                    customProperties.setSessionPreferences();
                } catch (Exception e) {
                    Toast.makeText(this,"Varsayılan ayarlarda eksik bilgi var.",Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    return;
                }

            }
            else{
                RadioGroup rg = (RadioGroup) findViewById(R.id.key_option_radio_group);
                if(rg.getCheckedRadioButtonId() == -1){
                    Toast.makeText(this, "Şifre Yöntemeni Seçin", Toast.LENGTH_SHORT).show();
                    return;
                }
                RadioButton rb = (RadioButton) rg.findViewById(rg.getCheckedRadioButtonId());
                EditText newKeyEditText = (EditText) findViewById(R.id.new_key_edit_text);
                EditText hintEditText = (EditText) findViewById(R.id.hint);
                EditText newFilenameEditText = (EditText) findViewById(R.id.new_filename_edit_text);

                customProperties.sessionAlgorithm = algorithmSpinner.getSelectedItem().toString();
                customProperties.sessionKeyLength = Integer.parseInt(keylengthSpinner.getSelectedItem().toString());

                Log.d("HANGİŞİFRE:",rb.getText().toString());


                switch (rb.getText().toString()){
                    case "Giriş şifresi":
                        Log.d("LOGİNŞİFRE16", new String(customProperties.byteKey16login));
                        if(customProperties.getLoginKey(customProperties.sessionKeyLength) == null){
                            Toast.makeText(this,"Giriş şifresi çok kısa.",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        customProperties.setSessionKeyFromLoginKey(customProperties.sessionKeyLength);
                        break;
                    case "Son kullanılan şifre":
                        customProperties.setSessionKeyFromLastUsedKey(customProperties.sessionKeyLength);
                        break;
                    case "Yeni şifre":
                        byte[] newKeyBytes = null;
                        try {
                            newKeyBytes = newKeyEditText.getText().toString().getBytes("UTF-8");
                            if(newKeyBytes.length < customProperties.sessionKeyLength/8){
                                Toast.makeText(this,"Yeni şifre çok kısa.",Toast.LENGTH_SHORT).show();
                                return;
                            }
                            customProperties.sessionKey = newKeyBytes;
                            customProperties.initializeLastUsedKey(newKeyEditText.getText().toString());
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        break;
                }

                String newFilename = newFilenameEditText.getText().toString();
                if(newFilename.equals(""))
                   customProperties.sessionFilename = null;
                else
                    customProperties.sessionFilename = newFilename;

                if(savedir == null)
                    customProperties.sessionSaveDirectory = null;
                else
                    customProperties.sessionSaveDirectory = new File(savedir);

                String hint = hintEditText.getText().toString();
                if(hint.equals(""))
                    customProperties.sessionHint = "YOK";
                else
                    customProperties.sessionHint = hint;

            }

            intent.putExtra("fileCount", files.size());
            for(int i=0; i<files.size(); i++){
                intent.putExtra("file"+i,files.get(i).getPath());
                intent.putExtra("fileobject"+i,files.get(i));
            }
            intent.putExtra("properties", customProperties);

            customProperties.printSessionProperties("FROMFILEENCRYPTION");

            startActivity(intent);
            finish();
        }

    }

}
