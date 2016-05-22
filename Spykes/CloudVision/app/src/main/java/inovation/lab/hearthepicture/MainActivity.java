package inovation.lab.hearthepicture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ImageView mainImage;
    private ResultMessage imageDetails;
    private GoogleCloudVision visionApi;
    private ColorToSound colorToSound;
    private ImageManager imageManager;
    private Button repeatBtn;
    private Button textBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        visionApi = new GoogleCloudVision();
        colorToSound = new ColorToSound(this);
        imageManager = new ImageManager(this);

        Button galleryBtn = (Button) findViewById(R.id.gallery_button);
        Button cameraBtn = (Button) findViewById(R.id.camera_button);
        repeatBtn = (Button) findViewById(R.id.repeat_button);
        textBtn = (Button) findViewById(R.id.text_button);

        repeatBtn.setVisibility(View.INVISIBLE);
        textBtn.setVisibility(View.INVISIBLE);

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageManager.startGalleryChooser();
            }
        });

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageManager.startCamera();
            }
        });

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),
                        imageDetails.getLabels() + "\n\n" + imageDetails.getFeelings(),
                        Toast.LENGTH_LONG).show();
            }
        });

        textBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), imageDetails.getText(), Toast.LENGTH_LONG).show();
            }
        });

        mainImage = (ImageView) findViewById(R.id.main_image);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) return;

        try {
            Bitmap bitmap = imageManager.ReadImage(requestCode, data);
            analyze(bitmap);
        } catch (IOException exception) {
            Log.d("main", "Image picking failed because " + exception.getMessage());
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        Toast.makeText(this, R.string.loading_message, Toast.LENGTH_LONG).show();

        new AsyncTask<Object, Void, ResultMessage>() {
            @Override
            protected ResultMessage doInBackground(Object... params) {
                return visionApi.Analyze(bitmap);
            }

            protected void onPostExecute(ResultMessage result) {
                imageDetails = result;

                if(imageDetails != null) {
                    Toast.makeText(getApplicationContext(),
                            imageDetails.getLabels() + "\n\n" + imageDetails.getFeelings(),
                            Toast.LENGTH_LONG).show();

                    repeatBtn.setVisibility(View.VISIBLE);
                    textBtn.setVisibility(View.VISIBLE);
                }else{
                    Toast.makeText(getApplicationContext(),
                            "Cloud Vision API request failed. Check logs for details.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }.execute();
    }

    public void analyze(final Bitmap bitmap) throws IOException {
        callCloudVision(bitmap);
        mainImage.setImageBitmap(bitmap);

        mainImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                int x = (int) event.getX();
                int y = (int) event.getY();

                switch (action) {
                    case MotionEvent.ACTION_UP:
                        int touchedRGB = imageManager.getProjectedColor((ImageView) v, bitmap, x, y);
                        colorToSound.play(touchedRGB);
                        break;
                    default:
                        break;
                }

                return true;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionUtils.permissionGranted(requestCode, ImageManager.CAMERA_PERMISSIONS_REQUEST, grantResults)) {
            imageManager.startCamera();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}