package com.example.loomoonazure.util;

import android.util.Log;

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

import java.util.LinkedList;

public class RobotConversation implements WakeupListener, RecognitionListener, TtsListener {
    private static final String TAG = "RobotConversation";

    private Robot robot;
    private Recognizer robotRecognizer;
    private Speaker robotSpeaker;

    private boolean grammarInit = false;
    private boolean started = false;
    private boolean speaking = false;
    private LinkedList<String> monologue = new LinkedList<String>();

    public RobotConversation(Robot robot) {
        this.robot = robot;
        robotRecognizer = Recognizer.getInstance();
        robotSpeaker = Speaker.getInstance();
    }

    public synchronized void start() {
        if (!started) {
            try {
                robotSpeaker.setVolume(50);

                int recognitionLanguage = robotRecognizer.getLanguage();
                if (recognitionLanguage == Languages.EN_US) {
                    if (!grammarInit) {
                        Slot helpingSlot = new Slot("helping", true);
                        helpingSlot.addWord("can you");
                        helpingSlot.addWord("could you");
                        helpingSlot.addWord("would you");
                        helpingSlot.addWord("will you");
                        helpingSlot.addWord("can you please");
                        helpingSlot.addWord("could you please");
                        helpingSlot.addWord("would you please");
                        helpingSlot.addWord("will you please");

                        Slot speakSlot = new Slot("speak", false);
                        speakSlot.addWord("tell");
                        speakSlot.addWord("tell us");
                        speakSlot.addWord("tell me");
                        speakSlot.addWord("toggle");

                        Slot speakWhatSlot = new Slot("what", false);
                        speakWhatSlot.addWord("a joke");
                        speakWhatSlot.addWord("your status");
                        speakWhatSlot.addWord("debug mode");
                        speakWhatSlot.addWord("monitor mode");

                        GrammarConstraint saySlotGrammar = new GrammarConstraint();
                        saySlotGrammar.setName("speak");
                        saySlotGrammar.addSlot(helpingSlot);
                        saySlotGrammar.addSlot(speakSlot);
                        saySlotGrammar.addSlot(speakWhatSlot);

                        robotRecognizer.addGrammarConstraint(saySlotGrammar);

                        Slot helpingSlot2 = new Slot("helping", true);
                        helpingSlot2.addWord("can you");
                        helpingSlot2.addWord("could you");
                        helpingSlot2.addWord("would you");
                        helpingSlot2.addWord("will you");
                        helpingSlot2.addWord("can you please");
                        helpingSlot2.addWord("could you please");
                        helpingSlot2.addWord("would you please");
                        helpingSlot2.addWord("will you please");

                        Slot moveSlot = new Slot("move", false);
                        moveSlot.addWord("wait here");
                        moveSlot.addWord("wait with me");
                        moveSlot.addWord("wait with us");
                        moveSlot.addWord("come here");
                        moveSlot.addWord("come with me");
                        moveSlot.addWord("come with us");
                        moveSlot.addWord("follow me");
                        moveSlot.addWord("move forward");
                        moveSlot.addWord("move back");
                        moveSlot.addWord("turn left");
                        moveSlot.addWord("turn right");
                        moveSlot.addWord("turn around");
                        moveSlot.addWord("take a picture");

                        GrammarConstraint moveSlotGrammar = new GrammarConstraint();
                        moveSlotGrammar.setName("move");
                        moveSlotGrammar.addSlot(helpingSlot2);
                        moveSlotGrammar.addSlot(moveSlot);

                        robotRecognizer.addGrammarConstraint(moveSlotGrammar);
                        grammarInit = true;
                    }

                    int speakerLanguage = robotSpeaker.getLanguage();
                    if (speakerLanguage == Languages.EN_US) {
                        robotRecognizer.startWakeupAndRecognition(this, this);
                        started = true;

                        // Now that we have started, lets speak everything we've been bottling up.
                        speak("");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception initing", e);
            }
        }
    }

    public synchronized void stop() {
        Log.d(TAG, String.format("stop threadId=%d", Thread.currentThread().getId()));
        try {
            robotRecognizer.stopRecognition();
            started = false;
        }
        catch (Exception e) {
            Log.e(TAG, "Exception stopping", e);
        }

    }

    public synchronized void speak(String message) {
        Log.d(TAG, String.format("speak threadId=%d", Thread.currentThread().getId()));

        if (!message.isEmpty()) {
            monologue.add(message);
        }

        if (started && !speaking && !monologue.isEmpty()) {
            String nextLine = monologue.remove();
            try {
                speaking = true;
                robotSpeaker.speak(nextLine, this);
            } catch (Exception e) {
                Log.e(TAG, "Exception speaking", e);
            }
        }
    }

    public void waitUntilFinishedSpeaking() {
        Log.d(TAG, String.format("waitUntilFinishedSpeaking threadId=%d", Thread.currentThread().getId()));

        boolean loop = true;
        do {
            synchronized(this){
                loop = speaking;
            }

            if (loop == true) {
                try {
                    Thread.sleep(500);
                }
                catch (Exception e) {
                    Log.e(TAG, "Exception sleeping", e);
                }
            }
        } while (loop == true);
    }


    // RecognitionListener
    @Override
    public void onRecognitionStart() {
        Log.d(TAG, String.format("onRecognitionStart threadId=%d", Thread.currentThread().getId()));
    }

    @Override
    public boolean onRecognitionResult(RecognitionResult recognitionResult) {
        String result = recognitionResult.getRecognitionResult();

        Log.d(TAG, String.format("onRecognitionResult result=%s threadId=%d", result, Thread.currentThread().getId()));

        try {
            if (result.contains("tell")) {
                if (result.contains("joke")) {
                    speak("OK, here's a joke. I'm a weeble, I wobble, but I don't fall down.");
                } else if (result.contains("status")) {
                    speak(robot.getStatus());
                }
            } else if (result.contains("toggle") && result.contains("debug")) {
                boolean mode = robot.getDebug();
                if (mode) {
                    robot.setDebug(false);
                    speak("Debug mode is now off");
                } else {
                    robot.setDebug(true);
                    speak("Debug mode is now on");
                }
            } else if (result.contains("toggle") && result.contains("monitor")) {
                boolean mode = robot.getMonitor();
                if (mode) {
                    robot.setMonitor(false);
                    speak("Monitor mode is now off");
                } else {
                    speak("Monitor mode is now on");
                    waitUntilFinishedSpeaking();
                    robot.setMonitor(true);
                }
            } else if (result.contains("wait")) {
                speak("Acknowledged");
                RobotAction ra = RobotAction.getTrack(Robot.TRACK_BEHAVIOR_WATCH);
                robot.actionDo(ra);
            } else if (result.contains("come") || result.contains("follow")) {
                speak("Acknowledged");
                RobotAction ra = RobotAction.getTrack(Robot.TRACK_BEHAVIOR_FOLLOW);
                robot.actionDo(ra);
            } else if (result.contains("move")) {
                RobotAction ra;
                if (result.contains("back")) {
                    ra = RobotAction.getMovement(Robot.MOVEMENT_BEHAVIOR_MOVE_VELOCITY, -0.5f, 0);
                    robot.actionDo(ra);
                    return true;
                } else {
                    ra = RobotAction.getMovement(Robot.MOVEMENT_BEHAVIOR_MOVE_VELOCITY, 0.5f, 0);
                    robot.actionDo(ra);
                    return true;
                }
            } else if (result.contains("turn")) {
                float theta = 1.0f;
                if (result.contains("right")) {
                    theta = -1.0f;
                }
                RobotAction ra = RobotAction.getMovement(Robot.MOVEMENT_BEHAVIOR_MOVE_TARGET, 0, theta);
                if (result.contains("around")) {
                    robot.actionDo(ra);
                    robot.actionDo(ra);
                    robot.actionDo(ra);
                    robot.actionDo(ra);
                    robot.actionDo(ra);
                    robot.actionDo(ra);
                    robot.actionDo(ra);
                }
                robot.actionDo(ra);
            } else if (result.contains("take a picture")) {
                robot.takePicture();
            } else {
                speak("You make no sense");
                return false;
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Exception recognizing", e);
        }
        return false;
    }

    @Override
    public boolean onRecognitionError(String error) {
        Log.d(TAG, String.format("onRecognitionError threadId=%d", Thread.currentThread().getId()));
        return false;
    }

    // WakeupListener
    @Override
    public void onStandby() {
        Log.d(TAG, String.format("onStandby threadId=%d", Thread.currentThread().getId()));
    }

    @Override
    public void onWakeupResult(WakeupResult wakeupResult) {
        Log.d(TAG, String.format("onWakeupResult threadId=%d", Thread.currentThread().getId()));
    }

    @Override
    public void onWakeupError(String error) {
        Log.d(TAG, String.format("onWakeupError threadId=%d", Thread.currentThread().getId()));
    }

    // TtsListener
    @Override
    public synchronized void onSpeechStarted(String word) {
        speaking = true;
        Log.d(TAG, String.format("onSpeechStarted threadId=%d", Thread.currentThread().getId()));
    }

    @Override
    public synchronized void onSpeechFinished(String word) {
        Log.d(TAG, String.format("onSpeechFinished threadId=%d", Thread.currentThread().getId()));
        try {
            Thread.sleep(500);
        }
        catch (Exception e) {
            Log.e(TAG, "Exception sleeping", e);
        }
        finally {
            speaking = false;
            speak("");
        }
    }

    @Override
    public synchronized void onSpeechError(String word, String reason) {
        Log.d(TAG, String.format("onSpeechError threadId=%d", Thread.currentThread().getId()));
        try {
            Thread.sleep(500);
        }
        catch (Exception e) {
            Log.e(TAG, "Exception sleeping", e);
        }
        finally {
            speaking = false;
            speak("");
        }
    }
}
