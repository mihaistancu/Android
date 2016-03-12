package inovation.lab.cloudvision;

import android.app.Activity;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import java.util.Locale;

public class Speech implements TextToSpeech.OnInitListener
{
    public int CHECK_CODE = 0;
    private Activity activity;
    private TextToSpeech tts;

    public Speech(Activity activity)
    {
        this.activity = activity;
        prepareInitialization();
    }

    public void speak(String speech)
    {
        tts.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    private void prepareInitialization()
    {
        Intent initialization = new Intent();
        initialization.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        activity.startActivityForResult(initialization, CHECK_CODE);
    }

    public void initialize(int resultCode)
    {
        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
        {
            tts = new TextToSpeech(activity, this);
        }
        else
        {
            Intent installTTSIntent = new Intent();
            installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            activity.startActivity(installTTSIntent);
        }
    }

    public void onInit(int initStatus)
    {
        if (initStatus == TextToSpeech.SUCCESS)
        {
            if (tts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
            {
                tts.setLanguage(Locale.US);
            }
        }
        else if (initStatus == TextToSpeech.ERROR)
        {
            Toast.makeText(activity, "Sorry! Text to Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    public void shutDown()
    {
        tts.stop();
        tts.shutdown();
    }
}