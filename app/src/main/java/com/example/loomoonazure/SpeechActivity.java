package com.example.loomoonazure;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.segway.robot.sdk.voice.Languages;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.grammar.Slot;
import com.segway.robot.sdk.voice.recognition.RecognitionListener;
import com.segway.robot.sdk.voice.recognition.RecognitionResult;
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;
import com.segway.robot.sdk.voice.tts.TtsListener;

public class SpeechActivity extends AppCompatActivity implements WakeupListener, RecognitionListener, TtsListener {

    private Recognizer robotRecognizer;
    private Speaker robotSpeaker;
    private TextView heard;

    private void updateHeard(String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                heard.setText(text);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech);

        heard = (TextView)findViewById(R.id.heard);

        robotRecognizer = Recognizer.getInstance();
        robotSpeaker = Speaker.getInstance();

        try {
            int recognitionLanguage = robotRecognizer.getLanguage();
            if (recognitionLanguage == Languages.EN_US) {
                GrammarConstraint speakSlotGrammar = new GrammarConstraint();

                speakSlotGrammar.setName("speak orders");

                Slot saySlot = new Slot("speak");
                saySlot.setOptional(false);
                saySlot.addWord("say");
                saySlot.addWord("tell me");
                saySlot.addWord("tell us");
                saySlot.addWord("tell him");
                saySlot.addWord("tell her");
                speakSlotGrammar.addSlot(saySlot);

                Slot contentSlot = new Slot("content");
                contentSlot.setOptional(false);
                contentSlot.addWord("a joke");
                contentSlot.addWord("the time");
                contentSlot.addWord("hello");
                contentSlot.addWord("goodbye");
                speakSlotGrammar.addSlot(contentSlot);

                robotRecognizer.addGrammarConstraint(speakSlotGrammar);

                int speakerLanguage = robotSpeaker.getLanguage();
                if (speakerLanguage == Languages.EN_US) {
                    robotSpeaker.speak("Hello, my name is Loomo.", this);
                    robotRecognizer.startWakeupAndRecognition(this, this);
                }
            }
        }
        catch (Exception e) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            robotRecognizer.stopRecognition();
        }
        catch (Exception e) {

        }
    }

    // RecognitionListener
    @Override
    public void onRecognitionStart() {

    }

    @Override
    public boolean onRecognitionResult(RecognitionResult recognitionResult) {
        String result = recognitionResult.getRecognitionResult();
        updateHeard(result);

        try {
            if (result.contains("say") || result.contains("tell")) {
                if (result.contains("a joke")) {
                    robotSpeaker.speak("Here's a joke. I'm a weeble. I wobble, but I don't fall down.", this);
                    return false;
                } else if (result.contains("the time")) {
                    robotSpeaker.speak("Do I look like a watch?", this);
                    return false;
                } else if (result.contains("hello")) {
                    robotSpeaker.speak("Greetings humans!", this);
                    return false;
                } else if (result.contains("goodbye")) {
                    robotSpeaker.speak("goodbye", this);
                    robotSpeaker.waitForSpeakFinish(2000);
                    Thread.sleep(3000);
                    robotSpeaker.speak("so long", this);
                    robotSpeaker.waitForSpeakFinish(2000);
                    Thread.sleep(3000);
                    robotSpeaker.speak("hate to see you go", this);
                    robotSpeaker.waitForSpeakFinish(2000);
                    Thread.sleep(3000);
                    robotSpeaker.speak("bye bye", this);
                    robotSpeaker.waitForSpeakFinish(2000);
                    Thread.sleep(3000);
                    robotSpeaker.speak("are they gone now?", this);
                    return false;
                }
            }

            robotSpeaker.speak("You said, " + result + " I don't understand what you want me to do.", this);
        }
        catch (Exception e){

        }
        return false;
    }

    @Override
    public boolean onRecognitionError(String error) {
        updateHeard(error);
        return false;
    }

    // WakeupListener
    @Override
    public void onStandby() {

    }

    @Override
    public void onWakeupResult(WakeupResult wakeupResult) {
        updateHeard(wakeupResult.getResult() + " Angle:" + wakeupResult.getAngle());
    }

    @Override
    public void onWakeupError(String error) {
        updateHeard(error);
    }

    // TtsListener
    @Override
    public void onSpeechStarted(String word) {

    }

    @Override
    public void onSpeechFinished(String word) {

    }

    @Override
    public void onSpeechError(String word, String reason) {

    }
}
