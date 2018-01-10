package com.ijs.marcel.mnist_drawer;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MainActivity extends AppCompatActivity {
    private final static String IMAGES_URL = "http://yann.lecun.com/exdb/mnist/train-images-idx3-ubyte.gz";
    private final static String LABELS_URL = "http://yann.lecun.com/exdb/mnist/train-labels-idx1-ubyte.gz";
    private final static String SAVE_DIR = "";
    private static final int BUFFER_SIZE = 4096;
    private Uri imageFile;
    private Uri labelFile;
    private File zipFile;
    private File mnist_save_folder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mnist_save_folder = new File(getExternalFilesDir(null) + "/mnist/");
    }

    public void onStart(View view) {
        Log.w("Image file:", String.valueOf(this.labelFile));
        startDrawingActivity();
        //getFile("Select label file",1);
    }

    private void getFile(String title, int requestCode){
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, title), requestCode);
    }

    private void startDrawingActivity() {
        Intent intent = new Intent(this, DrawingActivity.class);
        //intent.putExtra("labelFile", this.labelFile.toString());
        //intent.putExtra("imageFile", this.imageFile.toString());
        Log.w("Starting", "starting drawing activity:");
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            this.labelFile = data.getData(); //The uri with the location of the file
            Log.w("Label file:", String.valueOf(this.labelFile));
            getFile("Select image file",2);
        }else if(requestCode == 2 && resultCode == RESULT_OK) {
                this.imageFile = data.getData(); //The uri with the location of the file
            Log.w("Image file:", String.valueOf(this.labelFile));
            startDrawingActivity();
        }
    }

    public void zipData(){
        try {
            BufferedInputStream origin = null;
            File directory = mnist_save_folder;
            zipFile = new File(directory.getAbsoluteFile() + "data.zip");
            FileOutputStream zipFileStream = new FileOutputStream(zipFile);
            ZipOutputStream out;
            out = new ZipOutputStream(new BufferedOutputStream(zipFileStream));
            byte data[] = new byte[BUFFER_SIZE];

            File[] files = directory.listFiles();

            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                Log.v("Compress", "Adding: " + file.getName());
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER_SIZE);

                ZipEntry entry = new ZipEntry(files[i].getName());
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER_SIZE)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
            deleteFiles(files);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void deleteFiles(File[] files) {

    }

    public void onSendMail(View view) {
        zipData();
        Uri path = Uri.fromFile(zipFile);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        // set the type to 'email'
        emailIntent .setType("vnd.android.cursor.dir/email");
        String to[] = {"mnist.drawer@gmail.com"};
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        // the attachment
        emailIntent .putExtra(Intent.EXTRA_STREAM, path);
        // the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "MNIST_drawer data");
        startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }

}

