package com.example.mss.encryptor.encryption_activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.example.mss.encryptor.R;
import com.example.mss.encryptor.logic.CustomProperties;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TextEncryptionActivity extends AppCompatActivity {

    private LinearLayout optionsLayout;
    private Spinner algorithmSpinner;
    private Spinner keylengthSpinner;
    private RadioGroup keyRadioGroup;
    private RadioButton checkedRadioButton;
    private EditText newKeyEditText;
    private LinearLayout newKeyLayout;

    private EditText textInputEditText;
    EditText hintEditText;
    
    CustomProperties customProperties;

    boolean isVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_encryption);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        customProperties = getIntent().getExtras().getParcelable("properties");

        textInputEditText = (EditText) findViewById(R.id.input_text_edit_view);
        hintEditText = (EditText) findViewById(R.id.hint);

        isVisible = false;
        optionsLayout = (LinearLayout) findViewById(R.id.options_layout_text);
        optionsLayout.setVisibility(View.GONE);

        algorithmSpinner = (Spinner) findViewById(R.id.algorithm_spinner_text);
        keylengthSpinner = (Spinner) findViewById(R.id.keylength_spinner_text);

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

        newKeyLayout = (LinearLayout) findViewById(R.id.new_key_layout);
        newKeyLayout.setVisibility(View.GONE);

    }

    public void expandContractOptionsText(View view){
        if(isVisible){
            optionsLayout.setVisibility(View.GONE);
            isVisible=false;
        }
        else{
            optionsLayout.setVisibility(View.VISIBLE);
            isVisible=true;
        }
    }

    public void encryptText(View view){
        if(textInputEditText.getText().toString().equals("")){
            Toast.makeText(TextEncryptionActivity.this, "Metin giriniz.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this,EncryptionProgressionActivity.class);
        intent.putExtra("mode","text");
        intent.putExtra("isencryption",true);

        if(optionsLayout.getVisibility() == View.GONE){

            try {
                customProperties.setSessionPreferencesForText();
                customProperties.sessionSaveDirectory = getFilesDir();
            } catch (Exception e) {
                Toast.makeText(this,"Varsayılan ayarlarda eksik bilgi var.",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
        }
        else{

            RadioGroup rg = (RadioGroup) findViewById(R.id.key_option_radio_group);
            RadioButton rb = (RadioButton) rg.findViewById(rg.getCheckedRadioButtonId());
            EditText newKeyEditText = (EditText) findViewById(R.id.new_key_edit_text);
            EditText hintEditText = (EditText) findViewById(R.id.hint);
            EditText newFilenameEditText = (EditText) findViewById(R.id.new_filename_edit_text);

            customProperties.sessionAlgorithm = algorithmSpinner.getSelectedItem().toString();
            customProperties.sessionKeyLength = Integer.parseInt(keylengthSpinner.getSelectedItem().toString());

            String supposedAlgorithm = rb.getText().toString();
            switch (supposedAlgorithm){
                case "Giriş şifresi":
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
            if(newFilename.equals("")){
                Date date = new Date();
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyy-HH-mm-ss");
                String filename = sdf.format(date);
                customProperties.sessionFilename = filename;
            }
            else
                customProperties.sessionFilename = newFilename;

            customProperties.sessionSaveDirectory = getFilesDir();

            String hint = hintEditText.getText().toString();
            if(hint.equals(""))
                customProperties.sessionHint = "YOK";
            else
                customProperties.sessionHint = hint;
        }

        intent.putExtra("properties",customProperties);

        intent.putExtra("text",textInputEditText.getText().toString());
        Log.d("KOYULAN:",textInputEditText.getText().toString());
        startActivity(intent);
        finish();
    }

}
