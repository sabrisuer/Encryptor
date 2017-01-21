package com.example.mss.encryptor;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mss.encryptor.encryption_utilities.FileListActivity;
import com.example.mss.encryptor.logic.CustomProperties;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PreferencesActivity extends AppCompatActivity {

    private Dialog dialog;
    private Spinner algorithmSpinner;
    private Spinner keylengthSpinner;
    private RadioGroup keyRadioGroup;
    private RadioButton checkedRadioButton;
    private LinearLayout newKeyLayout;

    private EditText newKeyEditText;
    private EditText hint;
    
    private CustomProperties customProperties;

    private String savedir=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        customProperties = getIntent().getExtras().getParcelable("properties");

        newKeyEditText = (EditText) findViewById(R.id.new_key_edit);
        hint = (EditText) findViewById(R.id.hint);
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
        if(requestCode==2){
            if(resultCode==RESULT_OK){
                TextView textView = (TextView) findViewById(R.id.dirTextView);
                textView.setText(data.getStringExtra("dir"));
                savedir = data.getStringExtra("dir");
            }

        }
    }

    public void savePreferences(View view){


        customProperties.defaultKeyLength = Integer.parseInt(keylengthSpinner.getSelectedItem().toString());

        checkedRadioButton = (RadioButton) keyRadioGroup.findViewById(keyRadioGroup.getCheckedRadioButtonId());
        if(checkedRadioButton!=null){
            if(checkedRadioButton.getText().equals("Giriş şifresi")){
                customProperties.defaultKey = customProperties.loginKey;
                customProperties.initializeDefaultKey(customProperties.loginKey,customProperties.defaultKeyLength);
            } else if (checkedRadioButton.getText().equals("Yeni şifre")){
                if(!newKeyEditText.getText().equals("")){
                    customProperties.defaultKey = newKeyEditText.getText().toString();
                    customProperties.initializeDefaultKey(newKeyEditText.getText().toString(),customProperties.defaultKeyLength);
                }
                else
                    return;
            }
        }
        customProperties.defaultHint = hint.getText().toString();
        if(hint.getText().equals("")){
            customProperties.defaultHint = "yok";
        }
        customProperties.defaultAlgorithm = algorithmSpinner.getSelectedItem().toString();
        customProperties.defaultSaveDirectory = new File(savedir);
        if(savedir.equals("")){
            return;
        }
        EditText newfilename = (EditText) findViewById(R.id.new_filename_edit_text);
        if(newfilename.getText().toString().equals("")){
            return;
        }
        customProperties.defaultFilename = newfilename.getText().toString();

        SharedPreferences preferences = getSharedPreferences(customProperties.username,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("hint", customProperties.defaultHint);
        editor.putInt("keylength", customProperties.defaultKeyLength);
        editor.putString("algorithm", customProperties.defaultAlgorithm);
        editor.putString("savedir", customProperties.defaultSaveDirectory.getPath());
        editor.apply();

        Intent retIntent = new Intent();
        retIntent.putExtra("newproperties",customProperties);

        setResult(RESULT_OK,retIntent);

        Log.d("SONUÇLAR", "Şifre:" + customProperties.defaultKey + " İpucu:" + customProperties.defaultHint + " Algoritma:" + customProperties.defaultAlgorithm + " Uzunluk:" + customProperties.defaultKeyLength + " Dizin:" + customProperties.defaultSaveDirectory);
        finish();
    }

    public void newUser(View view){
        dialog = new Dialog(this);
        dialog.setContentView(R.layout.new_user_layout);
        dialog.setTitle("Yeni Kullanıcı");
        Button create = (Button) dialog.findViewById(R.id.create_new_user);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewUser(v);
            }
        });
        Button cancel = (Button) dialog.findViewById(R.id.cancel_new_user);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitNewUserDialog();
            }
        });
        dialog.show();
    }

    public void createNewUser(View view){
        EditText usernameView = (EditText) dialog.findViewById(R.id.new_user_new_username);
        EditText keyView = (EditText) dialog.findViewById(R.id.new_user_new_key);

        String username = usernameView.getText().toString();
        if(username.equals("")){
            Toast.makeText(this,"Kullanıcı adı giriniz.",Toast.LENGTH_SHORT).show();
            return;
        }
        String loginkey = keyView.getText().toString();
        if(loginkey.getBytes().length<5){
            Toast.makeText(this,"Şifreniz 5 karakterden uzun olmalıdır.",Toast.LENGTH_SHORT).show();
            return;
        }
        String keyhash = String.format("%064x", new java.math.BigInteger(1, hashSha512(loginkey)));

        SharedPreferences preferences = getSharedPreferences(username,MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("username", username);
        editor.putString("passhash", keyhash);

        editor.apply();
        dialog.cancel();
    }

    public void exitNewUserDialog(){
        dialog.cancel();
    }

    public byte[] hashSha512(String password){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(password.getBytes("UTF-8"));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void chooseDirectory(View view){
        Intent intent = new Intent(this,FileListActivity.class);
        intent.putExtra("mode",1);
        startActivityForResult(intent, 2);
    }

}
