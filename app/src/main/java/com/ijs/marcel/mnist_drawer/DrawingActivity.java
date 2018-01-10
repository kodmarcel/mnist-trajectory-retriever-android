package com.ijs.marcel.mnist_drawer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DrawingActivity extends AppCompatActivity {
    private StorageManager storageManager;
    private MnistLoader imageLoader;
    private int current_image = 0;
    private DrawingView view;
    private TextView textView;
    private int screenWidth;
    private int screenHeight;
    private Trajectory path;
    private TextView imageLabelView;
    public static File mnist_save_folder;
    private int scale;
    private MnistImage image;
    private SharedPreferences mPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        mPrefs = getPreferences(MODE_PRIVATE);
        current_image = mPrefs.getInt("current_image",0);

        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        //Uri labelFileName = Uri.parse(extras.getString("labelFile"));
        //Uri imageFileName = Uri.parse(extras.getString("imageFile"));

         mnist_save_folder = new File(getExternalFilesDir(null) + "/mnist/");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
//        requestWindowFeature( Window.FEATURE_NO_TITLE );
//
//        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN );


        InputStream labelFileStream = getResources().openRawResource(R.raw.train_labels);
        InputStream imageFileStream = getResources().openRawResource(R.raw.train_images);


        mnist_save_folder.mkdirs();

        try {
            this.imageLoader = new MnistLoader(labelFileStream, imageFileStream, this);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        setContentView(R.layout.activity_drawing);
//        ImageView imageView = new ImageView(this);
//
//        MnistImage image = images.get(0);
//        //Toast.makeText(this,image.getLabel(),Toast.LENGTH_LONG);
//        Bitmap bMap = Bitmap.createBitmap(image.getImageData(), image.getColumns(), image.getRows(), Bitmap.Config.ARGB_8888);
//
//        imageView.setImageBitmap(toGrayscale(bMap));
//        setContentView(imageView);
        Log.w("Activity","Setting view");
        View layout = getLayoutInflater().inflate(R.layout.activity_drawing,null);

        view = layout.findViewById(R.id.drawer);
        textView = layout.findViewById(R.id.currentImageText);
        imageLabelView = layout.findViewById(R.id.imageLabelText);
        setImage();
        setContentView(layout);

    }

    private void setImage() {
        Log.w("Activity","Setting image");
        try {
            image = imageLoader.getImage(current_image);
        } catch (IOException e) {
            Log.e("Loader","No image");
            e.printStackTrace();
        }
        textView.setText(String.valueOf(current_image + 1));
        imageLabelView.setText("Shape: " + String.valueOf(image.getLabel()) + " will be saved to folder: " + mnist_save_folder.getAbsolutePath());
        //Toast.makeText(this,image.getLabel(),Toast.LENGTH_LONG);
        //Try to use BitmapFactory.decodeStream
        Bitmap bMap = Bitmap.createBitmap(image.getImageData(), image.getColumns(), image.getRows(), Bitmap.Config.ARGB_8888);
        scale = screenHeight;
        if (screenHeight > screenWidth){
            scale = screenWidth;
        }

        bMap = Bitmap.createScaledBitmap(bMap, scale, scale, false);
        view.setmBitmap(toGrayscale(bMap));
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
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

    public void onContinue(View button){
        path = view.retrievePath();
        path.reScale(scale);
        String filename = savePath(path, current_image);
        current_image += 1;
        Log.w("Saved","Path save to" + filename);
        setImage();
    }

    private String savePath(Trajectory path, int current_image) {
        JSONObject json_path = new JSONObject();
        try {
            json_path.put("Image number", current_image);
            json_path.put("Image shape", image.getLabel());
            json_path.put("Path", path.getAsString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
//        /String root = Environment.getExternalStorageDirectory().toString();
        File file =  new File( mnist_save_folder.getAbsolutePath()+ "/image_" + String.valueOf(current_image) + ".json");
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(json_path.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    public void goTo(View button){
        createGoToDialog();
    }

    private Dialog createGoToDialog(){
        final NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMaxValue(imageLoader.getNumberOfImages());
        numberPicker.setMinValue(1);
        numberPicker.setValue(current_image +1 );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select image to go to");
        builder.setMessage("Choose a value :");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                current_image = numberPicker.getValue() - 1;
                setImage();
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        builder.setView(numberPicker);
        builder.create();
        return builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Store values between instances here
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putInt("current_image", current_image);
        // Commit to storage
        editor.commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("current_image",current_image);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        current_image = savedInstanceState.getInt("current_image");
    }
}
