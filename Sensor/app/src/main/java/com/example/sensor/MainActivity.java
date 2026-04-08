package com.example.sensor;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer, light, proximity;

    private TextView accText, lightText, proximityText;
    private TextView lightDescription, proximityDistance;
    private TextView statusAcc, statusLight, statusProximity;
    private TextView labelX, labelY, labelZ;

    private ProgressBar progressX, progressY, progressZ;
    private ProgressBar lightProgressBar;

    private View dotAcc, dotLight, dotProximity;

    private LinearLayout cardAccelerometer, cardLight, cardProximity;

    private final DecimalFormat df = new DecimalFormat("#.00");
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ValueAnimator pulseAnimator;

    private float accMaxRange = 20f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();
        initSensors();
        setupSensorAvailability();
        startEntranceAnimations();
        startDotPulse();
    }

    private void bindViews() {
        accText = findViewById(R.id.accText);
        lightText = findViewById(R.id.lightText);
        proximityText = findViewById(R.id.proximityText);
        lightDescription = findViewById(R.id.lightDescription);
        proximityDistance = findViewById(R.id.proximityDistance);
        statusAcc = findViewById(R.id.statusAcc);
        statusLight = findViewById(R.id.statusLight);
        statusProximity = findViewById(R.id.statusProximity);
        labelX = findViewById(R.id.labelX);
        labelY = findViewById(R.id.labelY);
        labelZ = findViewById(R.id.labelZ);
        progressX = findViewById(R.id.progressX);
        progressY = findViewById(R.id.progressY);
        progressZ = findViewById(R.id.progressZ);
        lightProgressBar = findViewById(R.id.lightProgressBar);
        dotAcc = findViewById(R.id.dotAcc);
        dotLight = findViewById(R.id.dotLight);
        dotProximity = findViewById(R.id.dotProximity);
        cardAccelerometer = findViewById(R.id.cardAccelerometer);
        cardLight = findViewById(R.id.cardLight);
        cardProximity = findViewById(R.id.cardProximity);
    }

    private void initSensors() {
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

            if (accelerometer != null) {
                accMaxRange = accelerometer.getMaximumRange();
                if (accMaxRange <= 0) accMaxRange = 20f;
            }
        }
    }

    private void setupSensorAvailability() {
        if (accelerometer == null) setOffline(dotAcc, statusAcc);
        if (light == null) setOffline(dotLight, statusLight);
        if (proximity == null) setOffline(dotProximity, statusProximity);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sensorManager == null) return;

        if (accelerometer != null)
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

        if (light != null)
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_UI);

        if (proximity != null)
            sensorManager.registerListener(this, proximity, SensorManager.SENSOR_DELAY_UI);

        if (pulseAnimator != null) pulseAnimator.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (sensorManager != null)
            sensorManager.unregisterListener(this);

        if (pulseAnimator != null) pulseAnimator.cancel();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            handleAccelerometer(event.values);
        }

        else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            handleLight(event.values[0]);
        }

        else if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            handleProximity(event.values[0]);
        }
    }

    private void handleAccelerometer(float[] values) {
        float x = values[0], y = values[1], z = values[2];

        accText.setText("X: " + df.format(x) + "\nY: " + df.format(y) + "\nZ: " + df.format(z));

        labelX.setText(df.format(x));
        labelY.setText(df.format(y));
        labelZ.setText(df.format(z));

        animateProgress(progressX, mapToProgress(x));
        animateProgress(progressY, mapToProgress(y));
        animateProgress(progressZ, mapToProgress(z));

        setOnline(dotAcc, statusAcc);
    }

    private void handleLight(float lux) {
        lightText.setText(df.format(lux) + " lx");

        lightDescription.setText(lux > 1000 ? "Bright" : "Dim");

        animateProgress(lightProgressBar, (int) Math.min(lux, 1000));

        setOnline(dotLight, statusLight);
    }

    private void handleProximity(float value) {
        if (value < proximity.getMaximumRange()) {
            proximityText.setText("NEAR");
            proximityText.setTextColor(Color.RED);
        } else {
            proximityText.setText("FAR");
            proximityText.setTextColor(Color.GREEN);
        }

        proximityDistance.setText(df.format(value) + " cm");
        setOnline(dotProximity, statusProximity);
    }

    private int mapToProgress(float value) {
        return (int) ((value / accMaxRange) * 100 + 100);
    }

    private void setOnline(View dot, TextView status) {
        dot.setBackgroundResource(R.drawable.indicator_dot);
        status.setText("LIVE");
    }

    private void setOffline(View dot, TextView status) {
        dot.setBackgroundResource(R.drawable.indicator_dot_off);
        status.setText("OFFLINE");
    }

    private void startEntranceAnimations() {
        mainHandler.postDelayed(() ->
                cardAccelerometer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)), 100);

        mainHandler.postDelayed(() ->
                cardLight.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)), 250);

        mainHandler.postDelayed(() ->
                cardProximity.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up_fade)), 400);
    }

    private void startDotPulse() {
        View globalDot = findViewById(R.id.globalDot);
        if (globalDot == null) return;

        pulseAnimator = ValueAnimator.ofFloat(1f, 0.3f, 1f);
        pulseAnimator.setDuration(1500);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.addUpdateListener(a -> globalDot.setAlpha((float) a.getAnimatedValue()));
        pulseAnimator.start();
    }

    private void animateProgress(ProgressBar bar, int target) {
        ObjectAnimator.ofInt(bar, "progress", bar.getProgress(), target)
                .setDuration(200)
                .start();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}