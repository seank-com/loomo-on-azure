package com.example.loomoonazure;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import com.segway.robot.sdk.emoji.BaseControlHandler;
import com.segway.robot.sdk.emoji.Emoji;
import com.segway.robot.sdk.emoji.EmojiPlayListener;
import com.segway.robot.sdk.emoji.EmojiView;
import com.segway.robot.sdk.emoji.HeadControlHandler;
import com.segway.robot.sdk.emoji.configure.BehaviorList;
import com.segway.robot.sdk.emoji.exception.EmojiException;
import com.segway.robot.sdk.emoji.player.RobotAnimator;
import com.segway.robot.sdk.emoji.player.RobotAnimatorFactory;
import com.segway.robot.sdk.locomotion.head.Head;
import com.segway.robot.sdk.locomotion.sbv.Base;
import com.segway.robot.sdk.voice.Languages;
import com.segway.robot.sdk.voice.Recognizer;
import com.segway.robot.sdk.voice.Speaker;
import com.segway.robot.sdk.voice.VoiceException;
import com.segway.robot.sdk.voice.grammar.GrammarConstraint;
import com.segway.robot.sdk.voice.grammar.Slot;
import com.segway.robot.sdk.voice.recognition.RecognitionListener;
import com.segway.robot.sdk.voice.recognition.RecognitionResult;
import com.segway.robot.sdk.voice.recognition.WakeupListener;
import com.segway.robot.sdk.voice.recognition.WakeupResult;
import com.segway.robot.sdk.voice.tts.TtsListener;

public class DemoActivity extends AppCompatActivity implements Handler.Callback, View.OnClickListener, BaseControlHandler, HeadControlHandler, WakeupListener, RecognitionListener, TtsListener {

    private static final int ACTION_SHOW_MSG = 1;
    private static final int ACTION_START_RECOGNITION = 2;
    private static final int ACTION_STOP_RECOGNITION = 3;
    private static final int ACTION_BEHAVE = 4;

    private Base robotBase;
    private Head robotHead;
    private Recognizer robotRecognizer;
    private Speaker robotSpeaker;
    private Emoji robotEmoji;

    private int speakerLanguage;
    private int recognitionLanguage;
    private GrammarConstraint moveSlotGrammar;

    private TextView textView;
    private EmojiView emojiView;

    private final Handler handler = new Handler(this);

    // Handler.Callback
    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case ACTION_SHOW_MSG:
                textView.setText(msg.obj.toString());
                break;
            case ACTION_START_RECOGNITION:
                try {
                    robotRecognizer.startWakeupAndRecognition(this, this);
                } catch (VoiceException e) {
                    e.printStackTrace();
                }
                break;
            case ACTION_STOP_RECOGNITION:
                try {
                    robotRecognizer.stopRecognition();
                } catch (VoiceException e) {
                    e.printStackTrace();
                }
                break;
            case ACTION_BEHAVE:
                try {
                    robotEmoji.startAnimation(RobotAnimatorFactory.getReadyRobotAnimator((Integer) msg.obj), new EmojiPlayListener() {
                        @Override
                        public void onAnimationStart(RobotAnimator animator) {
                        }

                        @Override
                        public void onAnimationEnd(RobotAnimator animator) {
                            emojiView.setClickable(true);
                            setWorldPitch(0.6f);
                        }

                        @Override
                        public void onAnimationCancel(RobotAnimator animator) {
                            emojiView.setClickable(true);
                            setWorldPitch(0.6f);
                        }
                    });
                } catch (EmojiException e) {
                    e.printStackTrace();
                }
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        emojiView = (EmojiView)findViewById(R.id.face);
        textView = (TextView)findViewById(R.id.textView);
        emojiView.setOnClickListener(this);

        robotBase = Base.getInstance();
        robotHead = Head.getInstance();
        robotRecognizer = Recognizer.getInstance();
        robotSpeaker = Speaker.getInstance();
        robotEmoji = Emoji.getInstance();


        robotEmoji.init(this);
        robotEmoji.setEmojiView(emojiView);

        if (robotHead.isBind()) {
            robotEmoji.setHeadControlHandler(this);
        }

        if (robotBase.isBind()) {
            robotEmoji.setBaseControlHandler(this);
        }

        try {
            recognitionLanguage = robotRecognizer.getLanguage();
            if (recognitionLanguage == Languages.EN_US) {
                moveSlotGrammar = new GrammarConstraint();
                moveSlotGrammar.setName("movement orders");

                Slot moveSlot = new Slot("movement");
                moveSlot.setOptional(false);
                moveSlot.addWord("look");
                moveSlot.addWord("turn");
                moveSlotGrammar.addSlot(moveSlot);

                Slot orientationSlot = new Slot("orientation");
                orientationSlot.setOptional(false);
                orientationSlot.addWord("right");
                orientationSlot.addWord("left");
                orientationSlot.addWord("up");
                orientationSlot.addWord("down");
                orientationSlot.addWord("full");
                orientationSlot.addWord("around");
                moveSlotGrammar.addSlot(orientationSlot);

                robotRecognizer.addGrammarConstraint(moveSlotGrammar);

                speakerLanguage = robotSpeaker.getLanguage();
                if (speakerLanguage == Languages.EN_US) {
                   robotSpeaker.speak("Hello, my name is Loomo.", this);

                    Message msg = handler.obtainMessage(ACTION_START_RECOGNITION);
                    handler.sendMessage(msg);
                } else {
                    emojiView.setClickable(false);
                    Message msg = handler.obtainMessage(ACTION_SHOW_MSG, "Only US English is supported");
                    handler.sendMessage(msg);
                }
            } else {
                emojiView.setClickable(false);
                Message msg = handler.obtainMessage(ACTION_SHOW_MSG, "Only US English is supported");
                handler.sendMessage(msg);
            }
        }
        catch (Exception e) {
            emojiView.setClickable(false);
            Message msg = handler.obtainMessage(ACTION_SHOW_MSG, "EXCEPTION: " + e.getMessage());
            handler.sendMessage(msg);
        }
    }

    // View.OnClickListener
    @Override
    public void onClick(View view) {
        emojiView.setClickable(false);
        int behavior;
        int randomSeed = (int) (Math.random() * 4);
        switch (randomSeed) {
            case 0:
                behavior = BehaviorList.LOOK_LEFT;
                break;
            case 1:
                behavior = BehaviorList.LOOK_RIGHT;
                break;
            case 2:
                behavior = BehaviorList.LOOK_AROUND;
                break;
            case 3:
                behavior = BehaviorList.LOOK_CURIOUS;
                break;
            default:
                behavior = BehaviorList.LOOK_AROUND;
                break;
        }
        Message msg = handler.obtainMessage(ACTION_BEHAVE, behavior);
        handler.sendMessage(msg);
    }

    // BaseControlHandler
    @Override
    public void setLinearVelocity(float velocity) {
        robotBase.setLinearVelocity(velocity);
    }

    @Override
    public void setAngularVelocity(float velocity) {
        robotBase.setAngularVelocity(velocity);
    }

    @Override
    public void stop() {
        robotBase.stop();
    }

    @Override
    public Ticks getTicks() {
        return null;
    }

    // HeadControlHandler
    @Override
    public int getMode() {
        return robotHead.getMode();
    }

    @Override
    public void setMode(int mode) {
        robotHead.setMode(mode);
    }

    @Override
    public void setWorldPitch(float angle) {
        robotHead.setWorldPitch(angle);
    }

    @Override
    public void setWorldYaw(float angle) {
        robotHead.setWorldYaw(angle);
    }

    @Override
    public float getWorldPitch() {
        return robotHead.getWorldPitch().getAngle();
    }

    @Override
    public float getWorldYaw() {
        return robotHead.getWorldYaw().getAngle();
    }

    // RecognitionListener
    @Override
    public void onRecognitionStart() {
        Message statusMsg = handler.obtainMessage(ACTION_SHOW_MSG, "Loomo begin to recognize, say:\n look up, look down, look left, look right," +
                " turn left, turn right, turn around, turn full");
        handler.sendMessage(statusMsg);
    }

    @Override
    public boolean onRecognitionResult(RecognitionResult recognitionResult) {
        //show the recognition result and recognition result confidence.
        String result = recognitionResult.getRecognitionResult();
        Message resultMsg = handler.obtainMessage(ACTION_SHOW_MSG, "recognition result: " + result + ", confidence:" + recognitionResult.getConfidence());
        handler.sendMessage(resultMsg);

        if (result.contains("look") && result.contains("left")) {
            Message msg = handler.obtainMessage(ACTION_BEHAVE, BehaviorList.LOOK_LEFT);
            handler.sendMessage(msg);
        } else if (result.contains("look") && result.contains("right")) {
            Message msg = handler.obtainMessage(ACTION_BEHAVE, BehaviorList.LOOK_RIGHT);
            handler.sendMessage(msg);
        } else if (result.contains("look") && result.contains("up")) {
            Message msg = handler.obtainMessage(ACTION_BEHAVE, BehaviorList.LOOK_UP);
            handler.sendMessage(msg);
        } else if (result.contains("look") && result.contains("down")) {
            Message msg = handler.obtainMessage(ACTION_BEHAVE, BehaviorList.LOOK_DOWN);
            handler.sendMessage(msg);
        } else if (result.contains("turn") && result.contains("left")) {
            Message msg = handler.obtainMessage(ACTION_BEHAVE, BehaviorList.TURN_LEFT);
            handler.sendMessage(msg);
        } else if (result.contains("turn") && result.contains("right")) {
            Message msg = handler.obtainMessage(ACTION_BEHAVE, BehaviorList.TURN_RIGHT);
            handler.sendMessage(msg);
        } else if (result.contains("turn") && result.contains("around")) {
            Message msg = handler.obtainMessage(ACTION_BEHAVE, BehaviorList.TURN_AROUND);
            handler.sendMessage(msg);
        } else if (result.contains("turn") && result.contains("full")) {
            Message msg = handler.obtainMessage(ACTION_BEHAVE, BehaviorList.TURN_FULL);
            handler.sendMessage(msg);
        }
        return false;
    }

    @Override
    public boolean onRecognitionError(String error) {
        Message errorMsg = handler.obtainMessage(ACTION_SHOW_MSG, "recognition error: " + error);
        handler.sendMessage(errorMsg);
        return false;
    }

    // WakeupListener
    @Override
    public void onStandby() {
        Message msg = handler.obtainMessage(ACTION_SHOW_MSG, "You can say \"Ok Loomo\" \n or touch the screen to wake up Loomo");
        handler.sendMessage(msg);
    }

    @Override
    public void onWakeupResult(WakeupResult wakeupResult) {

    }

    @Override
    public void onWakeupError(String error) {
        Message msg = handler.obtainMessage(ACTION_SHOW_MSG, "wakeup error:" + error);
        handler.sendMessage(msg);
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
        Message msg = handler.obtainMessage(ACTION_SHOW_MSG, "speech error: " + reason);
        handler.sendMessage(msg);
    }
}
