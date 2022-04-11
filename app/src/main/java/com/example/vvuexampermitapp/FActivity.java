package com.example.vvuexampermitapp;



import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;

//import com.example.myapplication.ml.Model1;

public class FActivity extends AppCompatActivity {

    public static final int CAMERA_ACTION_CODE=1;
    public static final int REQUEST_IMAGE_CAPTURE=1;
    private static final int MAX_RESULTS = 4 ;
    ImageView Imageview;
    Button capture;
    Button upload;
    Button verify;
    EditText textview;
    Bitmap bitmap;
    ByteBuffer byteBuffer;
    TensorImage tbuffer;
    private int imageSizeX;
    private int imageSizeY;
    private TensorBuffer outputProbabilityBuffer;
    TensorProcessor probabilityProcessor;
    MappedByteBuffer tfliteModel;
    private static final float IMAGE_MEAN = 0.0f;
    private static final float IMAGE_STD = 1.0f;
    private static final float PROBABILITY_MEAN = 0.0f;
    private static final float PROBABILITY_STD = 255.0f;
    List<String> Nlabels;


    ArrayList<Float> Entries = new ArrayList<>();

    Interpreter tflite;

    public Bitmap oribitmap,testbitmap;
    public static Bitmap cropped;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_factivity);
        Imageview = findViewById(R.id.View_id);
        capture = findViewById(R.id.imageBtn);
        textview = findViewById(R.id.idET);
        upload = findViewById(R.id.uploadBtn);
        verify = findViewById(R.id.VerifyBtn);

        upload.setVisibility(View.INVISIBLE);
        textview.setVisibility(View.INVISIBLE);


/*

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FActivity.this,CamActivity.class);
                startActivity(intent);

                openCamera();
            }
        });

*/


        capture.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                askCameraPermission();
            }
        });
    }

    private void askCameraPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 101);
        }
        else{
            openCamera();
        }
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //if(intent.resolveActivity(getPackageManager()) != null)
        try{
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
        catch(ActivityNotFoundException e){
            Toast.makeText(FActivity.this, "There is no app that support this action",Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length < 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            openCamera();

        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        capture.setVisibility(View.GONE);
        upload.setVisibility(View.VISIBLE);
        textview.setVisibility(View.VISIBLE);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            Imageview.setImageBitmap(imageBitmap);

            upload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    while(textview.getText().toString().trim().length() > 0){
                        String file_name = textview.getText().toString();
                        saveToInternalStorage(imageBitmap,file_name);

                    }

                }
            });


        }
    }


    private String saveToInternalStorage(Bitmap bitmapImage, String file_name){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,file_name+".jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }
}


