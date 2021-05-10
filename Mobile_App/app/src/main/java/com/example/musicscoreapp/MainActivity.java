package com.example.musicscoreapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_PHOTO_CODE = 2;

    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;

    Bitmap imageToSend;

    Button BSelectImage;
    ImageView IVPreviewImage;

    String imagePath;
    Uri selectedImage;
    Bitmap musicSheet;
    String ba1;
    //public static String destination = "http://35.232.70.229/";
    //public static String destination = "https://httpbin.org/get";
    public static String destination = "https://run.mocky.io/v3/905b9664-0425-4bdd-8742-b6a77aca9461";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageToSend = null;

        BSelectImage = findViewById(R.id.button2);
        IVPreviewImage = findViewById(R.id.imageView);
    }

    public void testMethod(View view) {
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
    }

    //Similar with the code that uses camera, the tutorial from which our code heavily referred is https://developer.android.com/training/camera/photobasics
    public void takePicture(View view) {
        Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(photoIntent, REQUEST_IMAGE_CAPTURE);
    }

    public void pickPhoto(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), PICK_PHOTO_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageToSend = (Bitmap) extras.get("data");
            IVPreviewImage.setImageBitmap(imageToSend);
        } else if (requestCode == PICK_PHOTO_CODE && resultCode == RESULT_OK) {
            selectedImage = data.getData();
            try {
                //getting bitmap object from uri
                imageToSend = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                //displaying selected image to imageview
                IVPreviewImage.setImageBitmap(imageToSend);
                //calling the method uploadBitmap to upload image
                //uploadBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Get path information
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();

            imagePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
        }
    }

    public void sendRequest(View view) {
        mRequestQueue = Volley.newRequestQueue(this);
        mStringRequest = new StringRequest(Request.Method.GET, destination, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Toast.makeText(getApplicationContext(), "Response :" + response.toString(), Toast.LENGTH_LONG).show();
            }
        }, error -> Log.i(MainActivity.class.getName(), "Error :" + error.toString()));
        mRequestQueue.add(mStringRequest);
    }

    /*
     * The method is takes an image in Bitmap form and
     * then it will return the byte[] array for the given bitmap.
     * This basically prepares the image to be sent to the server
     * */
    public byte[] getFileDataBitMap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public void uploadBitmap_(View view) {
        if (imageToSend == null) {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_LONG).show();
        } else {
            uploadBitmap(imageToSend);
        }
    }

    //UNTESTED
    private void uploadBitmap(final Bitmap bitmap) {

        //tag for the uploaded image - doesn't matter for now
        String tags = "Music Sheet";

        //custom volley request
        UploadToServer volleyMultipartRequest = new UploadToServer(Request.Method.POST, destination,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        try {
                            JSONObject obj = new JSONObject(new String(response.data));
                            Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("tags", tags);
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, UploadToServer.DataPart> getByteData() {
                Map<String, UploadToServer.DataPart> params = new HashMap<>();
                long imageName = System.currentTimeMillis();
                params.put("pic", new UploadToServer.DataPart(imageName + ".png", getFileDataBitMap(bitmap)));
                return params;
            }
        };

        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }


}