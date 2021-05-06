package com.example.musicscoreapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_PHOTO_CODE = 2;
    Button BSelectImage;
    ImageView IVPreviewImage;

    private String selectedImagePath;
    private String fileManagerString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BSelectImage = findViewById(R.id.button2);
        IVPreviewImage = findViewById(R.id.imageView);
    }

    public void testMethod(View view){
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }

    //Similar with the code that uses camera, the tutorial from which our code heavily referred is https://developer.android.com/training/camera/photobasics
    public void takePicture(View view){
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(photoIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            //We follwed an onlne tutorial and most of the code was taken from that tutorial, link: https://developer.android.com/training/basics/firstapp
        }
    }

    public void pickPhoto(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), PICK_PHOTO_CODE);
        /*
        if (galleryIntent.resolveActivity(getPackageManager()) != null) {
            //startActivityForResult(galleryIntent, PICK_PHOTO_CODE);
            startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), PICK_PHOTO_CODE);
        }
        */
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView imageView = (ImageView)findViewById(R.id.imageView);
            imageView.setImageBitmap(imageBitmap);
        } else if (requestCode == PICK_PHOTO_CODE && resultCode == RESULT_OK) {
            Uri photoUri = data.getData();
            if (photoUri != null) {
                IVPreviewImage.setImageURI(photoUri);
            }
        }
    }




}
