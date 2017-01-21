package com.example.mss.encryptor.encryption_activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.mss.encryptor.encryption_utilities.EncryptionProgressionActivity;
import com.example.mss.encryptor.encryption_utilities.FileListActivity;
import com.example.mss.encryptor.R;
import com.example.mss.encryptor.logic.CustomProperties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoEncryptionActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentPhotoPath;
    private File photoFile = null;

    private LinearLayout optionsLayout;
    private Spinner algorithmSpinner;
    private Spinner keylengthSpinner;
    private RadioGroup keyRadioGroup;
    private RadioButton checkedRadioButton;
    private EditText newKeyEditText;
    private LinearLayout newKeyLayout;

    private CustomProperties customProperties;

    private boolean isVisible;
    private String savedir = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_encryption);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        customProperties = getIntent().getExtras().getParcelable("properties");

        optionsLayout = (LinearLayout) findViewById(R.id.options_layout);
        optionsLayout.setVisibility(View.GONE);
        isVisible = false;

        algorithmSpinner = (Spinner) findViewById(R.id.algorithm_spinner);
        keylengthSpinner = (Spinner) findViewById(R.id.keylength_spinner);

        ArrayAdapter<CharSequence> algArrayAdapter = ArrayAdapter.createFromResource(this, R.array.algorithms, android.R.layout.simple_spinner_item);
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

        newKeyLayout = (LinearLayout) findViewById(R.id.new_key_layout);
        newKeyLayout.setVisibility(View.GONE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK){
            if(requestCode == 2){
                savedir = data.getStringExtra("dir");
            }
        }
    }

    public void chooseDirectory(View view){
        Intent intent = new Intent(this,FileListActivity.class);
        intent.putExtra("mode",1);
        startActivityForResult(intent, 2);
    }

    public void expandContractOptionsPhoto(View view){
        if(isVisible){
            optionsLayout.setVisibility(View.GONE);
            isVisible=false;
        }
        else{
            optionsLayout.setVisibility(View.VISIBLE);
            isVisible=true;
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName ="JPEG_"+timeStamp+"_";
        //File storageDir = new File(extras.getString("directory"));
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = "file:"+image.getAbsolutePath();

        return image;
    }

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePictureIntent.resolveActivity(getPackageManager()) != null){
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(photoFile != null){
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    public void takePictureButtonMethod(View view){
        encryptPhoto(view);
        //dispatchTakePictureIntent();
    }

    public void encryptPhoto(View view){

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

            Log.d("HANGİŞİFRE:", rb.getText().toString());


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

            if(savedir == null){
                customProperties.sessionSaveDirectory = null;
                Toast.makeText(this,"Kaydedilecek dizini seçiniz",Toast.LENGTH_SHORT).show();
                return;
            }
            else
                customProperties.sessionSaveDirectory = new File(savedir);

            String hint = hintEditText.getText().toString();
            if(hint.equals(""))
                customProperties.sessionHint = "YOK";
            else
                customProperties.sessionHint = hint;

        }

        Intent intent = new Intent(this,CameraActivity.class);
        intent.putExtra("properties",customProperties);
        intent.putExtra("mode","picture");
        startActivity(intent);
        finish();

    }
}
