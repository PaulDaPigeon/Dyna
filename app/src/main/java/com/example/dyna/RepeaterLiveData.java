package com.example.dyna;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.TextView;

public class RepeaterLiveData extends BaseLiveDataView {
    int setNum = 0;
    int repNum = 0;
    int countdownLeft = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repeater_live_data);

        lineChart = findViewById(R.id.lineChartRepeater);
        initializeButtons();
        timeLimit = session.workTime * 1000;
    }

    @Override
    public void updateStats() {
        ((TextView)findViewById(R.id.txtRepeaterCurrent)).setText(String.valueOf(session.getLatest()));
        ((TextView)findViewById(R.id.txtRepeaterRepNum)).setText(String.valueOf(repNum));
        ((TextView)findViewById(R.id.txtRepeaterSetNum)).setText(setNum + "/" + session.numSets);
        ((TextView)findViewById(R.id.txtCountdown)).setText(String.valueOf(countdownLeft));
    }

    public void initializeButtons() {
        findViewById(R.id.btnRepeaterStart).setOnClickListener(view -> {
            timerTowerStart();
        });
        findViewById(R.id.btnRepeaterStop).setOnClickListener(view -> {
            Log.d("stop", "scan stopped");
            dc.stopCollecting();
        });
    }

    private void timerTowerStart(){
        //Initial countdown timer
        countdownLeft = session.countdown;
        Log.d("TIMER", "initialTimer Started");
        new CountDownTimer(countdownLeft * 1000L, 1000) { // 10 seconds countdown, ticking every second
            public void onTick(long millisUntilFinished) {
                // Update UI with the time left
                countdownLeft--;
                Log.d("TIMER", "Seconds remaining: " + millisUntilFinished / 1000);
                updateStats();
            }

            public void onFinish() {
                Log.d("TIMER", "Initial timer finished");
                repNum = 1;
                setNum = 1;
                countdownLeft = session.workTime;
                updateStats();
                startRepTimer();
            }
        }.start();
    }

    public void startCountdownTimer(){
        Log.d("TIMER", "countdownTimer Started");
        new CountDownTimer(countdownLeft * 1000L, 1000) {
            public void onTick(long millisUntilFinished) {
                // Update UI with the time left
                // big font
                countdownLeft--;
                Log.d("TIMER", "Seconds remaining: " + millisUntilFinished / 1000);
                updateStats();
            }

            public void onFinish() {

                if(repNum == session.numReps) {
                    repNum = 1;
                    setNum ++;
                } else {
                    repNum++;
                }

                countdownLeft = session.workTime;
                updateStats();
                startRepTimer();
            }
        }.start();
    }
    public void startRepTimer(){
        Log.d("TIMER", "repTimer Started");
        dc.startCollecting();
        ((TextView) findViewById(R.id.txtPullRest)).setText("Pull");
        new CountDownTimer(session.workTime * 1000L, 1000) { // 10 seconds countdown, ticking every second

            public void onTick(long millisUntilFinished) {
                // Update UI with the time left on rep
                countdownLeft--;
                Log.d("TIMER", "Seconds remaining: " + millisUntilFinished / 1000);
                updateStats();
            }

            public void onFinish() {
                // Code to run when the timer finishes
                Log.d("TIMER", "rep finished");
                ((TextView) findViewById(R.id.txtPullRest)).setText("Rest");
                if(repNum < session.numReps) {
                    countdownLeft = session.restTime;
                    dc.stopCollecting();
                    startPauseBetweenRepsTimer();

                } else if(setNum < session.numSets) {
                    countdownLeft = session.pauseTime;
                    dc.stopCollecting();
                    startPauseBetweenSetsTimer();

                } else {
                    //Display final stats?
                    //stop collecting data
                    dc.stopScanning();
                }
                updateStats();

            }
        }.start();
    }
    public void startPauseBetweenRepsTimer(){
        dc.stopCollecting();
        Log.d("TIMER", "pauseBetweenReps Started");
        //if zero it'll never start, make sure it's never zero
        Log.d("TIMER",String.format("Rest: %d, \n CD: %d, \nPause: %d",session.restTime, session.countdown, Math.max(session.restTime - session.countdown, 0) * 1000L));
        new CountDownTimer(Math.max(session.restTime - session.countdown, 0) * 1000L, 1000) {
            public void onTick(long millisUntilFinished) {
                // Update UI with the time left before rep
                countdownLeft--;
                Log.d("TIMER", "Seconds remaining: " + millisUntilFinished / 1000);
                updateStats();
            }

            public void onFinish() {
                updateStats();
                startCountdownTimer();
            }
        }.start();
    }
    public void startPauseBetweenSetsTimer(){
        dc.stopCollecting();
        Log.d("TIMER", "pauseBetweenSets Started");
        new CountDownTimer(Math.max(session.pauseTime - session.countdown, 0) * 1000L, 1000) {
            public void onTick(long millisUntilFinished) {
                // Update UI with the time left on pause
                countdownLeft--;
                Log.d("TIMER", "Seconds remaining: " + millisUntilFinished / 1000);
                updateStats();
            }

            public void onFinish() {
                updateStats();
                startCountdownTimer();
            }
        }.start();
    }
}