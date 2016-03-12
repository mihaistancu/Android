package com.sandbox.mihaistancu.texttospeech;

import android.app.Activity;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;
import java.util.Locale;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnInitListener
{
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button speakButton = (Button)findViewById(R.id.speak);
        speakButton.setOnClickListener(this);

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    public void onClick(View v)
    {
        EditText enteredText = (EditText)findViewById(R.id.enter);
        String words = enteredText.getText().toString();
        speakWords(words);
    }

    private void speakWords(String speech)
    {
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == MY_DATA_CHECK_CODE)
        {
            if (resultCode== TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)
            {
                myTTS = new TextToSpeech(this, this);
            }
            else
            {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    public void onInit(int initStatus)
    {
        if (initStatus == TextToSpeech.SUCCESS)
        {
            if (myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
            {
                myTTS.setLanguage(Locale.US);
            }
        }
        else if (initStatus == TextToSpeech.ERROR)
        {
            Toast.makeText(this, "Sorry! Text to Speech failed...", Toast.LENGTH_LONG).show();
        }
    }
}
