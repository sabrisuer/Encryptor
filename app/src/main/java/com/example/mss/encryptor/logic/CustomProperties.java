package com.example.mss.encryptor.logic;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by mss on 02/01/16.
 */
public final class CustomProperties implements Parcelable {

    public CustomProperties(){

    }

    public String username=null;

    public String loginKey=null;
    public String lastUsedKey=null;
    public String defaultKey = null;

    public String defaultAlgorithm = null;
    public int defaultKeyLength = 0;
    public byte[] defaultKeyBytes = null;
    public String defaultHint = null;
    public String defaultFilename = null;
    public File defaultSaveDirectory = null;

    public byte[] byteKey16default=null;
    public byte[] byteKey24default=null;
    public byte[] byteKey32default=null;

    public byte[] byteKey16lastUsed =null;
    public byte[] byteKey24lastUsed =null;
    public byte[] byteKey32lastUsed =null;

    public byte[] byteKey16login=null;
    public byte[] byteKey24login=null;
    public byte[] byteKey32login=null;

    public String sessionAlgorithm = null;
    public int sessionKeyLength = 0;
    public byte[] sessionKey = null;
    public String sessionHint = null;
    public String sessionFilename = null;
    public File sessionSaveDirectory = null;

    public void initializeLoginKey(String newLoginKey){
        loginKey = newLoginKey;

        try{
            byte[] allBytes = newLoginKey.getBytes("UTF-8");
            if(allBytes.length>=16)
                byteKey16login = Arrays.copyOf(allBytes,16);
            if(allBytes.length>=24)
                byteKey24login = Arrays.copyOf(allBytes,24);
            if(allBytes.length>=32)
                byteKey32login = Arrays.copyOf(allBytes,32);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void initializeLastUsedKey(String newLastUsedKey){
        lastUsedKey = newLastUsedKey;

        try{
            byte[] allBytes = newLastUsedKey.getBytes("UTF-8");
            if(allBytes.length>=16)
                byteKey16lastUsed = Arrays.copyOf(allBytes,16);
            if(allBytes.length>=24)
                byteKey24lastUsed = Arrays.copyOf(allBytes,24);
            if(allBytes.length>=32)
                byteKey32lastUsed = Arrays.copyOf(allBytes,32);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void initializeDefaultKey(String newDefaultKey, int length){
        lastUsedKey = newDefaultKey;

        try{
            byte[] allBytes = newDefaultKey.getBytes("UTF-8");
            if(allBytes.length>=16)
                byteKey16default = Arrays.copyOf(allBytes,16);
            if(allBytes.length>=24)
                byteKey24default = Arrays.copyOf(allBytes,24);
            if(allBytes.length>=32)
                byteKey32default = Arrays.copyOf(allBytes,32);

            if(length == 128)
                defaultKeyBytes = byteKey16default;
            else if(length == 192)
                defaultKeyBytes = byteKey24default;
            else if(length == 256)
                defaultKeyBytes = byteKey32default;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void setSessionKeyFromLastUsedKey(int length){
        if(length!=128 && length!=192 && length!=256){
            Log.d("CUSTOMPROPERTIES:","setSessionKeyFromLastUsedKey yanlış uzunluk");
            return;
        }
        switch (length){
            case 128:
                sessionKey = byteKey16lastUsed;
                break;
            case 192:
                sessionKey = byteKey24lastUsed;
                break;
            case 256:
                sessionKey = byteKey32lastUsed;
                break;
        }
        sessionKeyLength = length;
    }

    public void setSessionKeyFromLoginKey(int length){
        switch (length){
            case 128:
                sessionKey = byteKey16login;
                break;
            case 192:
                sessionKey = byteKey24login;
                break;
            case 256:
                sessionKey = byteKey32login;
                break;
        }
    }

    public byte[] getLoginKey(int keylength){

        if(keylength!=128 && keylength!=192 && keylength!=256)
            return null;

        if(keylength==128)
            return byteKey16login;
        else if(keylength==192)
            return byteKey24login;
        else
            return byteKey32login;
    }

    public byte[] getLastUsedKey(int keylength){

        if(keylength!=128 && keylength!=192 && keylength!=256)
            return null;

        if(keylength==128)
            return byteKey16lastUsed;
        else if(keylength==192)
            return byteKey24lastUsed;
        else
            return byteKey32lastUsed;
    }

    public byte[] getDefaultKey(int keylength){

        if(keylength!=128 && keylength!=192 && keylength!=256)
            return null;

        if(keylength==128)
            return byteKey16default;
        else if(keylength==192)
            return byteKey24default;
        else
            return byteKey32default;
    }

    public void setSessionPreferences() throws Exception{

        printDefaultProperties("DEFAULTSETTING");

        if(defaultAlgorithm == null ||
           defaultKeyLength == 0 ||
           defaultKeyBytes == null ||
           defaultHint == null ||
           defaultFilename == null ||
           defaultSaveDirectory == null){

            Log.d("DEFALG",defaultAlgorithm);
            Log.d("DEFKEYLENGTH",""+defaultKeyLength);
            Log.d("DEFKEY",new String(defaultKeyBytes));
            Log.d("DEFHINT",defaultHint);
            Log.d("DEFFILENAME",defaultFilename);
            Log.d("DEFSAVEDIR",defaultSaveDirectory.toString());

            throw new Exception();
        }

        sessionAlgorithm = defaultAlgorithm;
        sessionKeyLength = defaultKeyLength;
        sessionKey = defaultKeyBytes;
        sessionHint = defaultHint;
        sessionFilename = defaultFilename;
        sessionSaveDirectory = defaultSaveDirectory;
    }

    public void setSessionPreferencesForText() throws Exception{

        printDefaultProperties("DEFAULTSETTINGFORTEXT");

        if(defaultAlgorithm == null ||
                defaultKeyLength == 0 ||
                defaultKeyBytes == null ||
                defaultHint == null ||
                defaultFilename == null){

            throw new Exception();
        }

        sessionAlgorithm = defaultAlgorithm;
        sessionKeyLength = defaultKeyLength;
        sessionKey = defaultKeyBytes;
        sessionHint = defaultHint;
        sessionFilename = defaultFilename;
    }

    public void printSessionProperties(String header){
        Log.d("HEADER:", header);
        Log.d("ALGORITHM:",sessionAlgorithm);
        Log.d("KEYLENGTH:",""+sessionKeyLength);

        if(sessionKey == null)
            Log.d("KEY:","null");
        else
            Log.d("KEY:",new String(sessionKey));

        if(sessionHint == null)
            Log.d("HINT:","null");
        else
            Log.d("HINT:",sessionHint);

        if(sessionFilename == null)
            Log.d("FILENAME","null");
        else
            Log.d("FILENAME:",sessionFilename);

        if(sessionSaveDirectory == null)
            Log.d("SAVEDIRECTOY:","null");
        else
            Log.d("SAVEDIRECTORY:",sessionSaveDirectory.getPath());
    }

    public void printDefaultProperties(String header){
        Log.d("HEADER:",header);
        Log.d("ALGORITHM:",defaultAlgorithm);
        Log.d("KEYLENGTH:",""+defaultKeyLength);

        if(defaultKey == null)
            Log.d("KEY:","null");
        else
            Log.d("KEY:",new String(defaultKey));

        if(defaultHint == null)
            Log.d("HINT:","null");
        else
            Log.d("HINT:",defaultHint);

        if(defaultFilename == null)
            Log.d("FILENAME","null");
        else
            Log.d("FILENAME:",defaultFilename);

        if(defaultSaveDirectory == null)
            Log.d("SAVEDIRECTOY:","null");
        else
            Log.d("SAVEDIRECTORY:",defaultSaveDirectory.getPath());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(loginKey);
        dest.writeString(lastUsedKey);

        dest.writeString(defaultKey);
        dest.writeString(defaultAlgorithm);
        dest.writeInt(defaultKeyLength);
        dest.writeByteArray(defaultKeyBytes);
        dest.writeString(defaultHint);
        dest.writeString(defaultFilename);
        if(defaultSaveDirectory != null)
            dest.writeString(defaultSaveDirectory.getPath());
        else
            dest.writeString(null);

        dest.writeByteArray(byteKey16default);
        dest.writeByteArray(byteKey24default);
        dest.writeByteArray(byteKey32default);

        dest.writeByteArray(byteKey16lastUsed);
        dest.writeByteArray(byteKey24lastUsed);
        dest.writeByteArray(byteKey32lastUsed);

        dest.writeByteArray(byteKey16login);
        dest.writeByteArray(byteKey24login);
        dest.writeByteArray(byteKey32login);

        dest.writeString(sessionAlgorithm);
        dest.writeInt(sessionKeyLength);
        dest.writeByteArray(sessionKey);
        dest.writeString(sessionHint);
        dest.writeString(sessionFilename);
        if(sessionSaveDirectory != null)
            dest.writeString(sessionSaveDirectory.getPath());
        else
            dest.writeString(null);
    }

    public static final Parcelable.Creator<CustomProperties> CREATOR = new Parcelable.Creator<CustomProperties>(){

        @Override
        public CustomProperties createFromParcel(Parcel source) {
            return new CustomProperties(source);
        }

        @Override
        public CustomProperties[] newArray(int size) {
            return new CustomProperties[size];
        }
    };

    public CustomProperties(Parcel source){
        username=source.readString();
        loginKey=source.readString();
        lastUsedKey=source.readString();

        defaultKey =source.readString();
        defaultAlgorithm = source.readString();
        defaultKeyLength =source.readInt();
        defaultKeyBytes = source.createByteArray();
        defaultHint = source.readString();
        defaultFilename = source.readString();
        String defaultSaveDirectoryPath = source.readString();
        if(defaultSaveDirectoryPath != null)
            defaultSaveDirectory = new File(defaultSaveDirectoryPath);
        else defaultSaveDirectory = null;


        byteKey16default=source.createByteArray();
        byteKey24default=source.createByteArray();
        byteKey32default=source.createByteArray();

        byteKey16lastUsed =source.createByteArray();
        byteKey24lastUsed =source.createByteArray();
        byteKey32lastUsed =source.createByteArray();

        byteKey16login=source.createByteArray();
        byteKey24login=source.createByteArray();
        byteKey32login=source.createByteArray();

        sessionAlgorithm = source.readString();
        sessionKeyLength = source.readInt();
        sessionKey = source.createByteArray();
        sessionHint = source.readString();
        sessionFilename = source.readString();
        String sessionSaveDirectoryPath = source.readString();
        if(sessionSaveDirectoryPath != null)
            sessionSaveDirectory = new File(sessionSaveDirectoryPath);
        else
            sessionSaveDirectory = null;
    }
}
