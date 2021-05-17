package com.example.musicscoreapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.service.autofill.FieldClassification;
import android.util.Log;
import android.view.View;

import android.content.Intent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.midi.Sequence;

import es.ua.dlsi.im3.core.conversions.ScoreToPlayed;
import es.ua.dlsi.im3.core.played.PlayedSong;
import es.ua.dlsi.im3.core.played.io.MidiSongExporter;
import es.ua.dlsi.im3.core.score.ScoreSong;
import es.ua.dlsi.im3.omr.encoding.semantic.SemanticImporter;


public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_PHOTO_CODE = 2;

    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;

    Bitmap imageToSend;

    Button BSelectImage;
    ImageView IVPreviewImage;

    private static final String outputMIDI = "output.mid";
    private boolean outputExists;

    public static String destination = "http://34.123.251.91/image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageToSend = null;
        outputExists = false;

        BSelectImage = findViewById(R.id.button2);
        IVPreviewImage = findViewById(R.id.imageView);
    }

    public void takePicture(View view) {
        CropImage.activity().start(MainActivity.this);
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
            Uri selectedImage = data.getData();
            try {
                imageToSend = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                IVPreviewImage.setImageBitmap(imageToSend);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    imageToSend = MediaStore.Images.Media.getBitmap(this.getContentResolver(), resultUri);
                    IVPreviewImage.setImageBitmap(imageToSend);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

    /*
     * Wrapper function for uploading the image to the ML server
     * */
    public void uploadBitmap_(View view) {
        if (imageToSend == null) {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_LONG).show();
        } else {
            uploadBitmap(imageToSend);
        }
    }

    /*
     * Credits to Belal for this custom multipart library to ease the image uploading since Volley
     * does not support a MultiPart request by default
     * https://www.simplifiedcoding.net/upload-image-to-server/#Android-Upload-Image-to-Server-using-Volley
     */
    private void uploadBitmap(final Bitmap bitmap) {
        //custom volley request
        UploadToServer volleyMultipartRequest = new UploadToServer(Request.Method.POST, destination,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String musicTokens = new String(response.data);
                        Toast.makeText(getApplicationContext(), "sent to web app:" + destination + ", music tokens are:" + musicTokens, Toast.LENGTH_LONG).show();
                        String musicTokenVerified = failSafe(musicTokens);
                        generateMIDIFile(musicTokenVerified);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "failed to send to web app:" + destination, Toast.LENGTH_SHORT).show();
                    }
                })
        {

            /*
             * If you want to add more parameters with the image
             * you can do it here
             * here we have only one parameter with the image
             * which is tags
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("tags", "tags");
                return params;
            }

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, UploadToServer.DataPart> getByteData() {
                Map<String, UploadToServer.DataPart> params = new HashMap<>();
                String imageName = "image";
                params.put("pic", new UploadToServer.DataPart(imageName + ".png", getFileDataBitMap(bitmap)));
                return params;
            }
        };
                ;

        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

    /*
     * This function was used for testing purposes - it was instrumental to ensuring correctness of the generateMIDIFile method
     */
    public void testMID(View view)  {
        String testNote = "clef-G2\tkeySignature-EbM\ttimeSignature-C\trest-half\tnote-G4_eighth\tnote-C5_quarter\tnote-B4_eighth\tbarline\tnote-C5_thirty_second\tnote-D5_thirty_second\tnote-Eb5_sixteenth\tnote-Eb5_quarter\tnote-D5_eighth\tnote-G5_eighth\tbarline\t";
        String testNote2 = "clef-G2\tkeySignature-EbM\ttimeSignature-C\trest-half\tnote-G4_eighth\tnote-C5_quarter\tbarline\tnote-B4_eighth\tnote-C5_thirty_second\tnote-D5_thirty_second\tnote-Eb5_sixteenth\tnote-Eb5_quarter\tnote-D5_eighth\tnote-G5_eighth\t";
        String testNote3 = "clef-G2\tkeySignature-EbM\trest-half\tnote-G4_eighth\tnote-G4_eighth\tnote-G4_eighth\tnote-G4_eighth\tnote-C5_quarter\tnote-C5_quarter\tnote-G4_eighth\tnote-C5_quarter\tnote-C5_whole\tnote-B4_eighth\tnote-C5_thirty_second\tnote-D5_thirty_second\tnote-Eb5_sixteenth\tnote-Eb5_quarter\tnote-D5_eighth\tnote-G5_eighth\t";
        generateMIDIFile(testNote);
    }
    /*
     * This function is a failSafe in cases when the machine learning algorithm passes back a string that cannot be correctly
     * parsed by the PriMus semantic converter (ie. greatly mitigates the chance of error when passing a string into generateMIDIFile)
     *
     */
    public static String failSafe(String response) {
        Pattern regexClef = Pattern.compile("clef-[CFG][12345]");
        Pattern regexKey = Pattern.compile("keySignature-...");
        Matcher clef = regexClef.matcher(response);
        Matcher key = regexKey.matcher(response);
        String clefString = "";
        String keyString = "";
        StringBuilder failSafe = new StringBuilder();

        if (clef.find()) {
            clefString = clef.group(0);
            failSafe.append(clefString);
            failSafe.append("\t");
        }
        if (key.find()) {
            keyString = key.group(0);
            failSafe.append(keyString);
            failSafe.append("\t");
        }
        String notesOnly = response.replaceAll("clef-[CFG][12345]\t", "");
        String notesOnly2 = notesOnly.replaceAll("keySignature-...\t", "");
        String notesOnly3 = notesOnly2.replaceAll("barline\t", "");
        String notesOnly4 = notesOnly3.replaceAll("timeSignature-.{1,4}\t", "");
        String notesOnly5 = notesOnly4.replaceAll("tie\t","");

        failSafe.append(notesOnly5);

        return failSafe.toString();
    }

    public void generateMIDIFile(String response)  {
        try {
            SemanticImporter semanticImporter = new SemanticImporter();
            ScoreSong scoreSong = semanticImporter.importSong(response);
            new PlayedSong();
            ScoreToPlayed scoreToPlayed = new ScoreToPlayed();
            PlayedSong playedSong = scoreToPlayed.createPlayedSongFromScore(scoreSong);
            FileOutputStream midiOutStream = openFileOutput(outputMIDI, MODE_PRIVATE);
            MidiSongExporterWrapper midiSongExporter = new MidiSongExporterWrapper();
            midiSongExporter.exportSongHook(midiOutStream, playedSong);
            Toast.makeText(this, "Music successfully generated", Toast.LENGTH_LONG).show();
            outputExists = true;
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Music failed to generate", Toast.LENGTH_LONG).show();
        }
    }

    public void playAudio(View view) {
        if (!outputExists) {
            Toast.makeText(this, "Audio file does not exist", Toast.LENGTH_LONG).show();
        } else {
            File audioFile = new File(getApplicationContext().getFilesDir(), outputMIDI);
            MediaPlayer mediaPlayer = MediaPlayer.create(this, Uri.fromFile(audioFile));
            mediaPlayer.start();
        }
    }
}