package com.sandbox.mihaistancu.texttospeech;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity implements OnClickListener
{
    Speech speech;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speech = new Speech(this);

        Button speakButton = (Button)findViewById(R.id.speak);
        speakButton.setOnClickListener(this);
    }

    public void onClick(View v)
    {
        EditText enteredText = (EditText)findViewById(R.id.enter);
        String words = enteredText.getText().toString();
        speech.speak(words);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == speech.CHECK_CODE)
        {
            speech.initialize(resultCode);
        }
    }
}
