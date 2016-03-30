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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private ImageView mainImage;
    private GoogleCloudVision visionApi;
    private ColorToSound colorToSound;
    private ImageManager imageManager;
    private Speech speech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        visionApi = new GoogleCloudVision();
        colorToSound = new ColorToSound(this);
        imageManager = new ImageManager(this);
        speech = new Speech(this);
        speech.allow(true);

        TextView selectImage = (TextView)findViewById(R.id.select_image);
        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageManager.selectImage();
            }
        });

        mainImage = (ImageView) findViewById(R.id.main_image);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == speech.CHECK_CODE) {
            speech.initialize(resultCode);
            return;
        }

        if (resultCode != RESULT_OK) return;

        try {
            Bitmap bitmap = imageManager.ReadImage(requestCode, data);
            analyze(bitmap);
        } catch (IOException exception) {
            Log.d("main", "Image picking failed because " + exception.getMessage());
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            speech.speak(getString(R.string.image_picker_error));
        }
    }

    private void callCloudVision(final Bitmap bitmap) throws IOException {
        Toast.makeText(this, R.string.loading_message, Toast.LENGTH_LONG).show();
        speech.speak(getString(R.string.loading_message));

        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                return visionApi.Analyze(bitmap);
            }

            protected void onPostExecute(String result) {
                speech.speak(result);
                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
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
        speech.shutDown();
    }
}