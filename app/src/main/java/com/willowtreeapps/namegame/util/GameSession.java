package com.willowtreeapps.namegame.util;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.willowtreeapps.namegame.ui.GamePlayFragment;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

/**
 * Created by th on 10/10/16.
 */

public class GameSession {
    private double averageTime = 0;
    private double totalTimeSeconds = 0;
    private int questionsAsked = 0;
    private int questionsCorrect = 0;

    private long countDownDuration = 30000; // 30 seconds before all hints are showed
    private CountDownTimer countdownTimer;
    private long millisUntilFinished = 0;

    private int currentRando; // represents the index of the person in question;

    private final String TAG = "GameSession";

    private int faceCount;

    // this array list will hold 1, 2, 3, 4, 5
    // and items will be removed as they are faded out and made unclickable to user
    private Stack<Integer> facesNotYetFaded = new Stack<>();

    // keeps track of the state of faces so they can be restored easily
    private ArrayList<Integer> facesAlreadyFaded = new ArrayList<>();

    GamePlayFragment gamePlayFragment;

    private Random random = new Random();

    int fadeGap = 0;

    /**
     * The constructor for the GameState class
     */
    public GameSession(int faceCount, GamePlayFragment gamePlayFragment){
        Log.i(TAG, "Starting a new game Session");
        this.faceCount = faceCount;
        this.gamePlayFragment = gamePlayFragment;
    }

    /**
     * This method will instantiate the timer
     */
    public void instantiateTimer(final long countDownDuration){
        Log.i(TAG, "Timer instantiated");
        if (fadeGap == 0) { // fadeGap wasn't recovered after orientation change
            fadeGap = (int) (countDownDuration/1000) / faceCount;
        }
        countdownTimer = new CountDownTimer(countDownDuration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.i(TAG, "Mills " + Long.toString(millisUntilFinished)
                        + " countdown " + Long.toString(countDownDuration));
                setMillisUntilFinished(millisUntilFinished);
                if (millisUntilFinished != 0 && fadeGap != 0){
                    Log.i(TAG, Integer.toString((int) (millisUntilFinished/1000) % fadeGap));
                    if ((int) (millisUntilFinished/1000) % fadeGap == 0){
                        fadeAFace();
                    }
                }
            }

            @Override
            public void onFinish() {
                gamePlayFragment.outOfTime();
            }
        };
    }

    /**
     * This method will start the timer
     */
    public void startTimer(){
        try{
            countdownTimer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * A method to cancel the timer
     */
    public void cancelTimer() {
        try {
            countdownTimer.cancel();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This method will update the Average time.
     */
    public void updateAverage(double timeToAdd) {
        setTotalTimeSeconds(getTotalTimeSeconds() + timeToAdd);
        double timeSpentInSeconds = calculateTimeSpent();
        if (getAverageTime() == 0){
            setAverageTime(timeSpentInSeconds);
        } else {
            setAverageTime((getTotalTimeSeconds() + timeSpentInSeconds) / questionsAsked);
        }
        millisUntilFinished = 0;
    }

    /**
     * A method that returns the time spent on a question
     * @return
     */
    public double calculateTimeSpent() {
        return ((countDownDuration / 1000) - (millisUntilFinished / 1000));
    }

    /**
     * This method will reset the state
     * This is used for an orientation change
     */
    public void resetSessionState(){
        int next = random.nextInt(faceCount);
        setCurrentRando(next);

        for (FrameLayout frameLayout : gamePlayFragment.getFrames()) {
            frameLayout.getChildAt(0).setAlpha(1f);
            frameLayout.getChildAt(0).setClickable(true);

        }
        // reset which faces have been faded to none
        try {
            // clears stack
            facesNotYetFaded.clear();
            facesAlreadyFaded.clear();

            // creates a list of unique values 0 -> faceCount - 1
            ArrayList<Integer> tempList = new ArrayList<>(faceCount);
            for (int i = 0; i < faceCount; i++) {
                if (i == currentRando) {
                    // doesn't add the current rando so that face won't be faded
                } else {
                    tempList.add(i);
                }
            }
            // pushes a random unique value to stack
            int size = tempList.size();
            for (int x = 0; x < size; x++) {
                int temp = random.nextInt(tempList.size());
                facesNotYetFaded.push(tempList.get(temp));
                tempList.remove(temp);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void restoreSessionState() {
        // resets all of the frames
        for (FrameLayout frameLayout : gamePlayFragment.getFrames()) {
            frameLayout.getChildAt(0).setAlpha(1f);
            frameLayout.getChildAt(0).setClickable(true);
            frameLayout.getChildAt(1).setVisibility(View.GONE);
        }

        // fades faces that were previously faded
        for (int alreadyFadedPos : facesAlreadyFaded) {
            ImageView temp = (ImageView) gamePlayFragment.getFrames().get(alreadyFadedPos).getChildAt(0);
            temp.setAlpha(0.2f);
            temp.setClickable(false);
        }

    }

    /**
     * A method that fades a face and makes it unclickable
     */
    private void fadeAFace(){
        Log.i(TAG, "Fading a face");
        try {
            int actualFaceToFade = facesNotYetFaded.pop();
            facesAlreadyFaded.add(actualFaceToFade);
            // In FrameLayout: index 0 = ImageView, index 1 = ProgressView
            ImageView temp = (ImageView) gamePlayFragment.getFrames().get(actualFaceToFade).getChildAt(0);
            temp.setAlpha(0.2f);
            temp.setClickable(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    ********************* Getters and Setters *********************
     */

    public double getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(double averageTime) {
        this.averageTime = averageTime;
    }

    public int getQuestionsCorrect() {
        return questionsCorrect;
    }

    public void setQuestionsCorrect(int questionsCorrect) {
        this.questionsCorrect = questionsCorrect;
    }

    public int getQuestionsAsked() {
        return questionsAsked;
    }

    public void setQuestionsAsked(int questionsAsked) {
        this.questionsAsked = questionsAsked;
    }

    public long getCountDownDuration() {
        return countDownDuration;
    }

    public void setCountDownDuration(long countDownDuration) {
        this.countDownDuration = countDownDuration;
    }

    public int getCurrentRando() {
        return currentRando;
    }

    public void setCurrentRando(int currentRando) {
        this.currentRando = currentRando;
    }

    public long getMillisUntilFinished() {
        return millisUntilFinished;
    }

    public void setMillisUntilFinished(long millisUntilFinished) {
        this.millisUntilFinished = millisUntilFinished;
    }

    public Stack<Integer> getFacesNotYetFaded() {
        return facesNotYetFaded;
    }

    public void setFacesNotYetFaded(Stack<Integer> facesNotYetFaded) {
        this.facesNotYetFaded = facesNotYetFaded;
    }

    public ArrayList<Integer> getFacesAlreadyFaded() {
        return facesAlreadyFaded;
    }

    public void setFacesAlreadyFaded(ArrayList<Integer> facesAlreadyFaded) {
        this.facesAlreadyFaded = facesAlreadyFaded;
    }

    public int getFadeGap() {
        return fadeGap;
    }

    public void setFadeGap(int fadeGap) {
        this.fadeGap = fadeGap;
    }

    public double getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public void setTotalTimeSeconds(double totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public CountDownTimer getCountdownTimer() {
        return countdownTimer;
    }

    public void setCountdownTimer(CountDownTimer countdownTimer) {
        this.countdownTimer = countdownTimer;
    }
}
