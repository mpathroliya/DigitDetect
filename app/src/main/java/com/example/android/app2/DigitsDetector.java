package com.example.android.app2;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import static android.content.ContentValues.TAG;

public class DigitsDetector {

    // Name of the file in the assets folder
    private static final String MODEL_PATH = "mnist.tflite";

    private Interpreter tflite;

    // Input Byte buffer
    private ByteBuffer inputBuffer = null;


    // output array [batchSize, 10]
    private float[][] mnistOutput = null;


    //specify output size
    private static final int NUMBER_LENGTH = 10;

    //specify input size
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_IMAGE_SIZE_X = 28;
    private static final int DIM_IMAGE_SIZE_Y = 28;
    private static final int DIM_PIXEL_SIZE = 1;

    // Number of bytes to hold a float (32 bits/float) / (8 bits /  byte) = 4 bytes / float
    private static final int BYTE_SIZE_OF_FLOAT = 4;

    public DigitsDetector(Activity activity) {
        try {
            // Define the TensorFlow Lite Interpreter with the model
            tflite = new Interpreter(loadModelFile(activity));
            inputBuffer = ByteBuffer.allocateDirect(BYTE_SIZE_OF_FLOAT * DIM_BATCH_SIZE * DIM_IMAGE_SIZE_X * DIM_IMAGE_SIZE_Y * DIM_PIXEL_SIZE);
            inputBuffer.order(ByteOrder.nativeOrder());
            mnistOutput = new float[DIM_BATCH_SIZE][NUMBER_LENGTH];
        } catch (IOException e) {
            Log.e(TAG, "IOException loading the tflite file");
        }
    }

    //...
    /**
     * Load the model file from the assets folder
     */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_PATH);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /**
     * Take in a bitmap and identify the number drawn on it.
     *
     * @param bitmap - the raw bitmap input.
     * @returns an integer from 0-9 of the predicted number.
     **/
    public int detectDigit(Bitmap bitmap){
        if (tflite == null) {
            Log.e(TAG, "Image classifier has not been initialized; Skipped.");
        }
        preprocess(bitmap);
        runInference();
        int predictedNumber = postProcess();
        return predictedNumber;
    }

    public void preprocess(Bitmap bitmap){
        if(bitmap ==null || inputBuffer ==null){
            return;
        }

        inputBuffer.rewind();


        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        long startTime = SystemClock.uptimeMillis();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels,0,width,0,0, width,height);


        for (int i =0; i < pixels.length; ++i){
            // set 0 for white and 255 for black pixels
            int pixel = pixels[i];
            // The color of the input is black so the blue channel will be 0xff
            int channel = pixel & 0xff;
            inputBuffer.putFloat(0xff - channel);
        }
        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Time cost to put values into ByteBuffer: " + Long.toString(endTime - startTime));
    }

    // this method runs inference
    public void runInference() {
        tflite.run(inputBuffer, mnistOutput);
    }



    // this method runs postprocess
    public int postProcess() {
        for( int i=0;i<mnistOutput[0].length;i++){

            float value = mnistOutput[0][i];
            Log.d(TAG,"Output for " + Integer.toString(i)+":" + Float.toString(value));
            if(value ==1f){
                return i;
            }
        }
        return -1;
    }


}