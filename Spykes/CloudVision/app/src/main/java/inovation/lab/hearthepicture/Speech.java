package inovation.lab.hearthepicture;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

public class Speech implements TextToSpeech.OnInitListener {
    public int CHECK_CODE = 0;
    public int LONG_DURATION = 5000;
    public int SHORT_DURATION = 1200;

    private boolean ready = false;
    private boolean allowed = false;
    private Activity activity;
    private TextToSpeech tts;

    public Speech(Activity activity) {
        this.activity = activity;
        prepareInitialization();
    }

    public void allow(boolean allowed){
        this.allowed = allowed;
    }

    public void speak(String text) {
        // Speak only if the TTS is ready and the user has allowed speech
        if(ready && allowed) {
            HashMap<String, String> hash = new HashMap<String,String>();
            hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                    String.valueOf(AudioManager.STREAM_NOTIFICATION));
            tts.speak(text, TextToSpeech.QUEUE_ADD, hash);
        }
    }

    public void pause(int duration){
        tts.playSilence(duration, TextToSpeech.QUEUE_ADD, null);
    }

    private void prepareInitialization() {
        Intent initialization = new Intent();
        initialization.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        activity.startActivityForResult(initialization, CHECK_CODE);
    }

    public void initialize(int resultCode) {
        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            tts = new TextToSpeech(activity, this);
        } else {
            Intent installTTSIntent = new Intent();
            installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            activity.startActivity(installTTSIntent);
        }
    }

    @Override
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            // Change this to match your locale
            tts.setLanguage(Locale.US);
            ready = true;
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(activity, "Sorry! Text to Speech failed...", Toast.LENGTH_LONG).show();
            ready = false;
        }
    }

    public void shutDown() {
        tts.stop();
        tts.shutdown();
    }
}