package com.mezan.touchpad;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    Button btnSave,btnClear;
    SignaturePad Spad;

    private static final int RSE = 1;
    private static String[] PS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSave = (Button)findViewById(R.id.savebtn);
        btnClear = (Button)findViewById(R.id.clrbtn);
        Spad = (SignaturePad)findViewById(R.id.pad);
        Spad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {
                Toast.makeText(MainActivity.this,"onStartSigning",Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSigned() {
                btnSave.setEnabled(true);
                btnClear.setEnabled(true);
            }

            @Override
            public void onClear() {
                btnSave.setEnabled(false);
                btnClear.setEnabled(false);
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Spad.clear();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap signBitmap = Spad.getSignatureBitmap();
                if(addJpgSignatureToGallery(signBitmap)){
                    Toast.makeText(MainActivity.this,"Signature Saved",Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(MainActivity.this,"Unable to Save the Signature",Toast.LENGTH_LONG).show();
                }
                if(addSvgSignatureToGallery(Spad.getSignatureSvg())){
                    Toast.makeText(MainActivity.this,"SVG Signature saved",Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(MainActivity.this,"Unable to Store the SVG",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case RSE : {
                if(grantResults.length<=0 || grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this,"Can't write file!",Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    public File getAlbumStorageDir(String albumName){
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),albumName);
        if(!file.mkdirs()){
            Toast.makeText(MainActivity.this,"SignaturePad Directory not created!",Toast.LENGTH_LONG).show();
        }
        return file;
    }
    public void SaveBitmapToJPG(Bitmap bitmap, File photo){
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap,0,0,null);
        try {
            OutputStream stream = new FileOutputStream(photo);
            newBitmap.compress(Bitmap.CompressFormat.JPEG,80,stream);
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public boolean addJpgSignatureToGallery(Bitmap signature){
        boolean result = false;
        File photo = new File(getAlbumStorageDir("SignaturePad"),String.format("Signature_%d.jpg",System.currentTimeMillis()));
        SaveBitmapToJPG(signature,photo);
        scanMediaFile(photo);
        result = true;
        return result;
    }

    private void scanMediaFile(File photo){
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri=Uri.fromFile(photo);
        mediaScanIntent.setData(contentUri);
        MainActivity.this.sendBroadcast(mediaScanIntent);

    }

    public boolean addSvgSignatureToGallery(String signatureSvg){
        boolean result = false;
        File svgFile = new File(getAlbumStorageDir("SignaturePad"),String.format("Signature_%d.svg",System.currentTimeMillis()));
        try {
            OutputStream stream = new FileOutputStream(svgFile);
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            writer.write(signatureSvg);
            writer.close();
            stream.flush();
            stream.close();
            scanMediaFile(svgFile);
            result = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  result;
    }
    public static void verifyStoragePermission(Activity activity){
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if(permission != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(activity,PS,RSE);
        }
    }

}
