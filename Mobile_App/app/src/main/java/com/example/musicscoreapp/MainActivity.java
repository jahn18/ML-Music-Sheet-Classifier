package com.example.musicscoreapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void testMethod(View view){
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }

    public void takePicture(View view){
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(photoIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            //We follwed an onlne tutorial and most of the code was taken from that tutorial, link: https://developer.android.com/training/basics/firstapp
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView imageView = (ImageView)findViewById(R.id.imageView);
            imageView.setImageBitmap(imageBitmap);
        }
        //Similar with the code that uses camera, the tutorial from which our code heavily referred is https://developer.android.com/training/camera/photobasics
    }
}
