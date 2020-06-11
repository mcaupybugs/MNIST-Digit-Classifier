package com.mcaupybugs.digsum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    Button submitButton;
    Paint paintDraw;
    RelativeLayout drawingSpace;
    Interpreter tflite;
    final int ROS_IMAGE1 = 1;

    Uri source;
    Bitmap bitmapMaster;
    Canvas canvasMaster;
    MyDrawView myDrawView;
    int prvX, prvY;
    int modelInputDim = 28;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        submitButton = findViewById(R.id.submit_button);
        drawingSpace = findViewById(R.id.drawingSpace);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runModel();
            }
        });
        paintDraw = new Paint();
        paintDraw.setStyle(Paint.Style.FILL);
        paintDraw.setColor(Color.WHITE);
        paintDraw.setStrokeWidth(21);   
        myDrawView = new MyDrawView(this);
        drawingSpace.addView(myDrawView);
        requestPermissionForExternalStorage(1);
        try {
            tflite = new Interpreter(loadModelFile());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private float[][][][] convertImageToFloatArray ( Bitmap image ) {
        float[][][][] imageArray = new   float[1][modelInputDim][modelInputDim][1] ;
        for ( int x = 0 ; x < modelInputDim ; x ++ ) {
            for ( int y = 0 ; y < modelInputDim ; y ++ ) {
                float R = ( float )Color.red( image.getPixel( x , y ) );
                float G = ( float )Color.green( image.getPixel( x , y ) );
                float B = ( float )Color.blue( image.getPixel( x , y ) );
                double grayscalePixel = (( 0.3 * R ) + ( 0.59 * G ) + ( 0.11 * B )) / 255;
                imageArray[0][x][y][0] = (float)grayscalePixel ;
            }
        }
        return imageArray ;
    }
    private void runModel(){
        drawingSpace.setDrawingCacheEnabled(true);
        Bitmap b = drawingSpace.getDrawingCache();
        Bitmap image = toGrayscale(b);
        float [][] result = new float[1][10];
        ByteBuffer byteBuffer;
        Bitmap image2 = Bitmap.createScaledBitmap(image,28,28,true);
//        save(image2);
//        int size = image2.getRowBytes()*image2.getHeight();
//        System.out.println(size);
//        byteBuffer = ByteBuffer.allocate(size);
//        image2.copyPixelsToBuffer(byteBuffer);
//        if(byteBuffer==null){
//            System.out.println("Yp");
//        }
        float [][][][] input = new float[1][modelInputDim][modelInputDim][1];
        input = convertImageToFloatArray(image2);
        tflite.run(input,result);
        for(int i=0;i<=9;i++)
        System.out.println(result[0][i]);
    }
    private ByteBuffer convertBitmapToByteBuffer_float(Bitmap bitmap, int
            BATCH_SIZE, int inputSize, int PIXEL_SIZE) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * BATCH_SIZE *
                inputSize * inputSize * PIXEL_SIZE); //float_size = 4 bytes
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                final int val = intValues[pixel++];


                byteBuffer.putFloat( ((val >> 16) & 0xFF)* (1.f/255.f));
                byteBuffer.putFloat( ((val >> 8) & 0xFF)* (1.f/255.f));
                byteBuffer.putFloat( (val & 0xFF)* (1.f/255.f));
            }
        }
        return byteBuffer;
    }
    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    private MappedByteBuffer loadModelFile() throws IOException{
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declaredLength);
    }
    public void save(Bitmap b){
        FileOutputStream fos = null;
        try {
            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path, "there.png");
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        b.compress(Bitmap.CompressFormat.PNG, 95, fos);
    }

    private void saveImage() {
        drawingSpace.setDrawingCacheEnabled(true);
        Bitmap b = drawingSpace.getDrawingCache();
        FileOutputStream fos = null;
        try {
            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path, "me.png");
            fos = new FileOutputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        b.compress(Bitmap.CompressFormat.PNG, 95, fos);
    }

    public void requestPermissionForExternalStorage(int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this.getApplicationContext(), "External Storage permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted successfully
                } else {
                    //permission denied
                    Toast.makeText(this, "PERMISSION DENIED!!!!You cannot play the game", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}