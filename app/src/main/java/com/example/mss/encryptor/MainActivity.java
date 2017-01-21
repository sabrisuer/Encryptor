package com.example.mss.encryptor;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.mss.encryptor.encryption_activities.FileEncryptionActivity;
import com.example.mss.encryptor.encryption_activities.PhotoEncryptionActivity;
import com.example.mss.encryptor.encryption_activities.TextEncryptionActivity;
import com.example.mss.encryptor.encryption_activities.VideoEncryptionActivity;
import com.example.mss.encryptor.encryption_utilities.ShowEncryptedFilesActivity;
import com.example.mss.encryptor.logic.CustomProperties;
import com.example.mss.encryptor.logic.algorithms.gnu.crypto.cipher.Rijndael;
import com.example.mss.encryptor.logic.algorithms.gnu.crypto.cipher.Serpent;
import com.example.mss.encryptor.logic.algorithms.gnu.crypto.cipher.Twofish;

import java.security.InvalidKeyException;

public class MainActivity extends AppCompatActivity {

    CustomProperties customProperties;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        customProperties = getIntent().getExtras().getParcelable("properties");
        if(customProperties != null){
            if(customProperties.username != null)
                toolbar.setTitle("Encryptor | "+ customProperties.username);
            else
                toolbar.setTitle("Encryptor");
        }
        else
            toolbar.setTitle("Encryptor");
        setSupportActionBar(toolbar);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK && requestCode == 1){
            customProperties.initializeLastUsedKey(data.getStringExtra("lastusedkey"));
            Log.d("SONUÇ:",customProperties.lastUsedKey);
        }
        else if(resultCode == RESULT_OK && requestCode == 2){
            customProperties = data.getParcelableExtra("newproperties");
        }
    }

    public void jumpToFileEncryption(View view){
        Intent intent = new Intent(this, FileEncryptionActivity.class);
        intent.putExtra("properties",customProperties);
        startActivityForResult(intent, 1);
    }
    public void jumpToTextEncryption(View view){
        Intent intent = new Intent(this,TextEncryptionActivity.class);
        intent.putExtra("properties",customProperties);
        startActivity(intent);
    }
    public void jumpToPhotoEncryption(View view){
        Intent intent = new Intent(this,PhotoEncryptionActivity.class);
        intent.putExtra("properties",customProperties);
        startActivity(intent);
    }
    public void jumpToVideoEncryption(View view){
        Intent intent = new Intent(this,VideoEncryptionActivity.class);
        intent.putExtra("properties",customProperties);
        startActivity(intent);
    }
    public void jumpToDecryption(View view){
        Intent intent = new Intent(this,ShowEncryptedFilesActivity.class);
        intent.putExtra("properties",customProperties);
        startActivity(intent);
    }
    public void jumpToPrefs(View view){
        Intent intent = new Intent(this,PreferencesActivity.class);
        intent.putExtra("properties",customProperties);
        startActivityForResult(intent,2);
    }
    public void jumpToAbout(View view){
        Intent intent = new Intent(this,AboutAppActivity.class);
        intent.putExtra("properties",customProperties);
        startActivity(intent);
    }
}
