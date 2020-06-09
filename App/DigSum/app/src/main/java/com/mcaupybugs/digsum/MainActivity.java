package com.mcaupybugs.digsum;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    Button submitButton;
    Paint paintDraw;
    RelativeLayout drawingSpace;

    final int ROS_IMAGE1 = 1;

    Uri source;
    Bitmap bitmapMaster;
    Canvas canvasMaster;
    MyDrawView myDrawView;
    int prvX,prvY;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        submitButton = findViewById(R.id.submit_button);
        drawingSpace = findViewById(R.id.drawingSpace);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage();
            }
        });
        paintDraw = new Paint();
        paintDraw.setStyle(Paint.Style.FILL);
        paintDraw.setColor(Color.WHITE);
        paintDraw.setStrokeWidth(10);

        myDrawView = new MyDrawView(this);
        drawingSpace.addView(myDrawView);
        requestPermissionForExternalStorage(1);

    }

    private void saveImage(){
        drawingSpace.setDrawingCacheEnabled(true);
        Bitmap b = drawingSpace.getDrawingCache();
        FileOutputStream fos = null;
        try{
            String path = Environment.getExternalStorageDirectory().toString();
            File file = new File(path,"me.png");
            fos = new FileOutputStream(file);
        }catch (Exception e){
            e.printStackTrace();
        }
        b.compress(Bitmap.CompressFormat.PNG,95,fos);
    }
    public void requestPermissionForExternalStorage(int requestCode){
        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            Toast.makeText(this.getApplicationContext(), "External Storage permission needed. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},requestCode);
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
                    Toast.makeText(this,"PERMISSION DENIED!!!!You cannot play the game",Toast.LENGTH_LONG).show();

                }
                break;
        }
    }
}