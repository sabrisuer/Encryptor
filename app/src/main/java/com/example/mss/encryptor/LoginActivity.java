package com.example.mss.encryptor;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mss.encryptor.logic.CustomProperties;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView mUsernameView;
    private EditText mPasswordView;
    private Button mLoginButton;

    CustomProperties customProperties = new CustomProperties();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUsernameView = (AutoCompleteTextView) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginButton = (Button) findViewById(R.id.user_sign_in_button);

        mPasswordView.setOnEditorActionListener(new EditText.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v,int actionId, KeyEvent event){
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    mainMenu(mLoginButton);
                    return true;
                }
                return false;
            }
        });

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

        boolean isFirstRun = sharedPref.getBoolean("isFirstRun",true);

        if(isFirstRun){
            mLoginButton.setText(R.string.newUserButtonText);
        }

    }

    public void adminPass(View view){
        Intent intent = new Intent(this,MainActivity.class);
        customProperties.username = "admin";
        customProperties.loginKey = "1234567890123456";
        customProperties.defaultAlgorithm = "AES";
        customProperties.defaultKeyLength = 128;
        customProperties.initializeLoginKey("1234567890123456");
        customProperties.initializeDefaultKey("1234567890123456",128);

        intent.putExtra("properties",customProperties);

        Log.d("LOGİNŞİFRE16", new String(customProperties.byteKey16login));

        startActivity(intent);
        finish();
    }

    public void mainMenu(View view){
        if(mPasswordView.getText().length()<16){
            Toast.makeText(getApplicationContext(),R.string.error_invalid_password,Toast.LENGTH_SHORT).show();
            return;
        }
        if(mPasswordView.getText().toString().equals("nopass")){
            Toast.makeText(getApplicationContext(),R.string.error_invalid_password_reserve,Toast.LENGTH_SHORT).show();
            return;
        }
        if(mUsernameView.getText().toString().equals("nouser")){
            Toast.makeText(getApplicationContext(),R.string.error_invalid_username_reserve,Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        boolean isFirstRun = sharedPref.getBoolean("isFirstRun",true);
        if(isFirstRun){
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("isFirstRun",false);
            editor.apply();
            newUser();
        }
        else{
            authenticate();
        }
    }

    public void newUser(){
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passhash = String.format("%064x", new java.math.BigInteger(1, hashSha512(password)));

        SharedPreferences preferences = getSharedPreferences(username, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("username", username);
        editor.putString("passhash", passhash);

        editor.apply();

        Intent intent = new Intent(this,MainActivity.class);
        customProperties.username = username;
        customProperties.initializeLoginKey(password);
        intent.putExtra("properties",customProperties);
        startActivity(intent);
        finish();
    }
    public void authenticate(){
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();
        String passhash = String.format("%064x", new java.math.BigInteger(1, hashSha512(password)));

        SharedPreferences preferences = getSharedPreferences(username, MODE_PRIVATE);

        String userCompare = preferences.getString("username", "nouser");
        String passhashCompare = preferences.getString("passhash","nopass");

        if(!userCompare.equals("nouser") && !passhashCompare.equals("nopass")){
            if(!userCompare.equals(username) || !passhashCompare.equals(passhash)){
                Toast.makeText(getApplicationContext(),R.string.error_incorrect_form,Toast.LENGTH_SHORT).show();
            }
            else{
                Intent intent = new Intent(this,MainActivity.class);
                customProperties.username = username;
                customProperties.initializeLoginKey(password);
                intent.putExtra("properties",customProperties);
                startActivity(intent);
                finish();
            }
        }
    }
    public byte[] hashSha512(String password){
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(password.getBytes("UTF-8"));
            return md.digest();

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
