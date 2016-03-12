package inovation.lab.cloudvision;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.R.color;
import android.media.MediaPlayer;
import java.io.File;
import java.io.IOException;
import android.media.AudioManager;
import sample.google.com.cloudvision.R;

public class MainActivity extends AppCompatActivity {
    public static final String FILE_NAME = "temp.jpg";

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    GoogleCloudVision api;
    private TextView mImageDetails;
    private ImageView mMainImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        api = new GoogleCloudVision();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setMessage(R.string.dialog_select_prompt)
                        .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startGalleryChooser();
                            }
                        })
                        .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startCamera();
                            }
                        });
                builder.create().show();
            }
        });

        mImageDetails = (TextView) findViewById(R.id.image_details);
        mMainImage = (ImageView) findViewById(R.id.main_image);
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        mImageDetails.setText(R.string.loading_message);

        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params)
            {
                return api.Analyze(bitmap);
            }

            protected void onPostExecute(String result)
            {
                mImageDetails.setText(result);
            }
        }.execute();
    }

    public void startGalleryChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                GALLERY_IMAGE_REQUEST);
    }

    public void startCamera() {
        if (PermissionUtils.requestPermission(this, CAMERA_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getCameraFile()));
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    public File getCameraFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());
        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            uploadImage(Uri.fromFile(getCameraFile()));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
            startCamera();
        }
    }

    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to 800px to save on bandwidth
                final Bitmap bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), uri), 1200);

                callCloudVision(bitmap);
                mMainImage.setImageBitmap(bitmap);

                mMainImage.setOnTouchListener(new View.OnTouchListener(){
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        int action = event.getAction();
                        int x = (int) event.getX();
                        int y = (int) event.getY();

                        switch(action){
                            case MotionEvent.ACTION_UP :
                                int touchedRGB = getProjectedColor((ImageView) v, bitmap, x, y);
                                //int touchedRGB = bitmap.getPixel(x, y);
                                ColorToSOund(touchedRGB);
                                break;
                            default:
                                break;
                        }

                        //Return 'true' to indicate that the event have been consumed.
                        return true;
                    }});

            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private void ColorToSOund(int color) {
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        Log.d("R", Integer.toString(red));
        Log.d("G", Integer.toString(green));
        Log.d("B", Integer.toString(blue));

        if (blue == 255 && red == 255 && green == 255) {
            // play white
            PlayColor("white", 1);
        } else if (blue == 0 && red == 0 && green == 0) {
            // play black
            PlayColor("black", 1);
        } else {
            if (blue > 128 && red > 128 && green > 128) {
                // play white half volume
                PlayColor("white", 0.5f);
            }

            if (blue < 128 && red < 128 && green < 128) {
                // play black half volume
                PlayColor("black", 0.5f);
            }

            // set volume color/255
            // play red
            PlayColor("red", (float) red / 255);
            // play green
            PlayColor("green", (float) green / 255);
            // play blue
            PlayColor("blue", (float) blue / 255);
        }
    }

    private void PlayColor(String color, float volume) {
        Uri myUri = null;

        switch (color) {
            case "red":
                myUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.red); // initialize Uri here
                break;
            case "green":
                myUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.green); // initialize Uri here
                break;
            case "blue":
                myUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.blue); // initialize Uri here
                break;
            case "white":
                myUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.white); // initialize Uri here
                break;
            case "black":
                myUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.black); // initialize Uri here
                break;
        }

        try {
            final MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(getApplicationContext(), myUri);
            mediaPlayer.setVolume(volume, volume);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.release();
                } ;
            });
        } catch (Exception e) {
            Log.d(TAG, "BUBA!!!");
        }
    }

    /*
  * Project position on ImageView to position on Bitmap
  * return the color on the position
  */
    private int getProjectedColor(ImageView iv, Bitmap bm, int x, int y){
        if(x<0 || y<0 || x > iv.getWidth() || y > iv.getHeight()){
            //outside ImageView
            return color.background_light;
        }else{
            int projectedX = (int)((double)x * ((double)bm.getWidth()/(double)iv.getWidth()));
            int projectedY = (int)((double)y * ((double)bm.getHeight()/(double)iv.getHeight()));

            return bm.getPixel(projectedX, projectedY);
        }
    }
}