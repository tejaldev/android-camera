package com.example.takingphoto;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private final static int IMAGE_CAPTURE_REQUEST_CODE = 1011;
    private final static String PHOTO_FILE_NAME = "photo.jpg";

    private ImageView photoImageView;
    private Bitmap photoBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button launchCameraButton = (Button) findViewById(R.id.launchCameraButton);
        launchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLaunchCamera();
            }
        });

        Button rotateImageButton = (Button) findViewById(R.id.rotateImageButton);
        rotateImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri photoUri = getPhotoUri(PHOTO_FILE_NAME);

                if(photoBitmap == null) {
                    photoBitmap = BitmapFactory.decodeFile(photoUri.getPath());
                }

                photoBitmap =  rotateBitmap(photoBitmap); // rotateImage(photoUri.getPath());

                if(photoBitmap != null) {
                    photoImageView.setImageBitmap(photoBitmap);
                }
            }
        });

        photoImageView = (ImageView) findViewById(R.id.photoImageView);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //If result is OK then Show the photo in an ImageView inside activity
        if(requestCode == IMAGE_CAPTURE_REQUEST_CODE) {
            if(resultCode == RESULT_OK && photoImageView != null) {
                Uri photoUri = getPhotoUri(PHOTO_FILE_NAME);

                Bitmap photoBitmap = BitmapFactory.decodeFile(photoUri.getPath());

                if(photoBitmap != null) {
                    this.photoBitmap = photoBitmap;
                    photoImageView.setImageBitmap(photoBitmap);
                }
            }
            else {
                Toast.makeText(this, "Photo capture was not successful.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void onLaunchCamera() {

        //1. Create intent with Image capture action, add photoUri
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getPhotoUri(PHOTO_FILE_NAME));

        //2. Start activity with above intent
        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, IMAGE_CAPTURE_REQUEST_CODE);
        }
    }

    private Uri getPhotoUri(String photoFileName) {
        String storageState = Environment.getExternalStorageState();

        //1. start only if sdcard is available
        if(storageState.equals(Environment.MEDIA_MOUNTED)) {

            //2. get photo storage directory
            File externalPicDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            //3. if directory doesn't exists create it
            if(!externalPicDir.exists()) {
                externalPicDir.mkdirs();
            }

            //4. Finally create the photo file to save the image after capture
            return Uri.fromFile(new File(externalPicDir.getPath() + File.separator + photoFileName));
        }

        return null;
    }

    private Bitmap rotateBitmap(Bitmap bitmap) {

        //Now rotate Bitmap with the angle
        Matrix matrix = new Matrix();
        matrix.setRotate(90, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return rotatedBitmap;
    }

    private Bitmap rotateImage(String photoFilePath) {
        //configure bounds
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoFilePath, bounds);

        //
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(photoFilePath, options);

        //Read EXIF data to get image orientation info
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(photoFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String orientationInfo = exif.getAttribute(ExifInterface.TAG_ORIENTATION);
        int orientation = orientationInfo != null ? Integer.parseInt(orientationInfo) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationAngle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationAngle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationAngle = 270;
                break;
            default:
                rotationAngle = 180;
        }

        //Now rotate Bitmap with the angle
        Matrix matrix = new Matrix();
        matrix.setRotate(rotationAngle, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bounds.outWidth, bounds.outHeight, matrix, true);

        return rotatedBitmap;
    }
}
