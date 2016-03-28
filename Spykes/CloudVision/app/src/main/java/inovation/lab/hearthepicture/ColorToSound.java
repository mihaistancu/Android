package inovation.lab.hearthepicture;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class ColorToSound {
    Context context;

    ColorToSound(Context context) {
        this.context = context;
    }

    public void play(int color) {
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
                myUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.red); // initialize Uri here
                break;
            case "green":
                myUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.green); // initialize Uri here
                break;
            case "blue":
                myUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.blue); // initialize Uri here
                break;
            case "white":
                myUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.white); // initialize Uri here
                break;
            case "black":
                myUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.black); // initialize Uri here
                break;
        }

        try {
            final MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(context.getApplicationContext(), myUri);
            mediaPlayer.setVolume(volume, volume);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    mediaPlayer.release();
                }

                ;
            });
        } catch (Exception e) {
            Log.d("color2Sound", "BUBA!!!");
        }
    }
}
