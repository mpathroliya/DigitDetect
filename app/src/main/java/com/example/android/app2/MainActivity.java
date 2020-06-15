package com.example.android.app2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private DigitsDetector mnistClassifier;
    ImageView imageView;
    Button chooseButton;
    Button detectButton;
    Button clearButton;
    TextView resultText;


    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
        imageView.setImageResource(R.drawable.ic_image_black_24dp);
        chooseButton = findViewById(R.id.choose_button);
        detectButton = findViewById(R.id.detect_button);
        clearButton = findViewById(R.id.clear_button);
        resultText = findViewById(R.id.result_text);
        mnistClassifier = new DigitsDetector(this);

        chooseButton.setOnClickListener(new View.OnClickListener() {
//            @override
            public void onClick (View v){
                //check runtime permission
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                    {//permission not granted, request it.
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        //show a popup for the runtime permission
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                    else{// permission already granted
                        PickImageFromGallery();
                    }
                }
                else{
                    //system os is less than marshmellow
                    PickImageFromGallery();
                }
                //set clear and detect button visible
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                imageView.setVisibility(View.VISIBLE);
                detectButton.setVisibility(View.VISIBLE);
                clearButton.setVisibility(View.VISIBLE);
                resultText.setVisibility(View.GONE);
                v.setVisibility(View.GONE);
            }


        });

        clearButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                // clear button instructions
                // set ImageView as invisible, detect button and clear button and resultText as gone, upload button as visible;
//                detectButton.setVisibility(View.GONE);
                resultText.setVisibility(View.VISIBLE);
                chooseButton.setVisibility(View.VISIBLE);
                imageView.setImageResource(R.drawable.ic_image_black_24dp);
                v.setVisibility(View.GONE);
            }
        });

        detectButton.setOnClickListener(new View.OnClickListener(){
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v){
                // checking if the image is uploaded
//                Bitmap currBmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
//                Drawable defaultImage =  getResources().getDrawable(R.drawable.ic_image_black_24dp);
//                Bitmap defaultBmap = ((BitmapDrawable) defaultImage).getBitmap();
//                if(currBmap.sameAs(defaultBmap)){
//                    Toast toast2 =Toast.makeText(getApplicationContext(),"no image uploaded!",Toast.LENGTH_SHORT);
//                    toast2.show();
//                    chooseButton.setVisibility(View.VISIBLE);
//                    v.setVisibility(View.GONE);
//                    clearButton.setVisibility(View.GONE);
//                }
//                else {


                    //1. run detectdigit or run the placeholder function
                    // run your function here

                    Bitmap bitmap = convertImageViewToBitmap(imageView);
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    int imageSize = Math.min(width, height);
                    Bitmap centreCroppedBitmap = BitmapUtil.centerCrop(bitmap, imageSize, imageSize);
                    bitmap = Bitmap.createScaledBitmap(centreCroppedBitmap, 28, 28, false);
//                Toast toast = Toast.makeText(getApplicationContext()," this works!! ", Toast.LENGTH_SHORT );
//                toast.show();

                    int digit = mnistClassifier.detectDigit(bitmap);
//                changeText(digit);
                    Toast toast1 = Toast.makeText(getApplicationContext(), getString(R.string.found_digits, String.valueOf(digit)), Toast.LENGTH_SHORT);
                    toast1.show();


                    //2. make detect button gone
                    v.setVisibility(View.GONE);
//                }
            }
        });


    }

    private void PickImageFromGallery() {
        //intent to pick image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    //handle request of runtime permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_CODE:{
                if(grantResults.length >0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    // permission was granted
                    PickImageFromGallery();
                }
                else{
                    //permission denied
                    Toast.makeText(this, "permission denied...!!",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK &&  requestCode == IMAGE_PICK_CODE){
            //setimage to image view
            imageView.setImageURI(data.getData());
        }
    }

    private Bitmap convertImageViewToBitmap(ImageView iView){
        BitmapDrawable drawable = (BitmapDrawable) iView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        return bitmap;
    }
    private void changeText(int digit ){
        TextView tv;
        tv = findViewById(R.id.result_text);
        if (digit >= 0) {
//                    Log.d(TAG, "Found Digit = " + digit);
            tv.setText(getString(R.string.found_digits, String.valueOf(digit)));
        } else {
            tv.setText(getString(R.string.not_detected));
        }
    }
}
