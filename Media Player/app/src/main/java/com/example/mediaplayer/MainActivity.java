package com.example.mediaplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {

    Button openFile, openVideo, play, pause, stop, restart;
    TextView fileName;
    VideoView videoView;
    SeekBar seekBar;

    MediaPlayer mediaPlayer;
    Uri mediaUri;
    boolean isAudio = false;

    Handler handler = new Handler();

    Runnable seekBarUpdater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                seekBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 500);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ Initialize views
        openFile = findViewById(R.id.openFile);
        openVideo = findViewById(R.id.openVideo);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        stop = findViewById(R.id.stop);
        restart = findViewById(R.id.restart);
        fileName = findViewById(R.id.fileName);
        videoView = findViewById(R.id.videoView);
        seekBar = findViewById(R.id.seekBar);

        requestStoragePermission();

        // 🎵 OPEN AUDIO
        openFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
            startActivityForResult(intent, 1);
        });

        // 🎬 OPEN VIDEO
        openVideo.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            startActivityForResult(intent, 2);
        });

        // ▶ PLAY
        play.setOnClickListener(v -> {
            if (isAudio) {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                    handler.post(seekBarUpdater);
                    fileName.setText("▶ Playing audio...");
                } else {
                    Toast.makeText(this, "No audio selected", Toast.LENGTH_SHORT).show();
                }
            } else {
                if (mediaUri != null) {
                    videoView.start();
                    fileName.setText("▶ Playing video...");
                } else {
                    Toast.makeText(this, "No video selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // ⏸ PAUSE
        pause.setOnClickListener(v -> {
            if (isAudio) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    handler.removeCallbacks(seekBarUpdater);
                    fileName.setText("⏸ Paused");
                }
            } else {
                if (videoView.isPlaying()) {
                    videoView.pause();
                    fileName.setText("⏸ Paused");
                }
            }
        });

        // ⏹ STOP
        stop.setOnClickListener(v -> {
            if (isAudio) {
                if (mediaPlayer != null) {
                    mediaPlayer.stop();
                    handler.removeCallbacks(seekBarUpdater);
                    seekBar.setProgress(0);

                    mediaPlayer.release();
                    mediaPlayer = MediaPlayer.create(this, mediaUri);

                    if (mediaPlayer != null) {
                        seekBar.setMax(mediaPlayer.getDuration());
                    }

                    fileName.setText("⏹ Stopped");
                }
            } else {
                videoView.stopPlayback();
                fileName.setText("⏹ Stopped");
            }
        });

        // 🔄 RESTART
        restart.setOnClickListener(v -> {
            if (isAudio) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                    handler.post(seekBarUpdater);
                    fileName.setText("↩ Restarted");
                }
            } else {
                if (mediaUri != null) {
                    videoView.setVideoURI(mediaUri);
                    videoView.start();
                    fileName.setText("↩ Restarted video...");
                }
            }
        });

        // 🎚 SEEKBAR
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (isAudio && fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(seekBarUpdater);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isAudio && mediaPlayer != null && mediaPlayer.isPlaying()) {
                    handler.post(seekBarUpdater);
                }
            }
        });
    }

    // 🎯 HANDLE RESULT
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK || data == null) return;

        mediaUri = data.getData();

        if (requestCode == 1) {
            // AUDIO
            releaseMediaPlayer();
            isAudio = true;

            mediaPlayer = MediaPlayer.create(this, mediaUri);

            if (mediaPlayer != null) {
                seekBar.setMax(mediaPlayer.getDuration());
                seekBar.setProgress(0);
                videoView.setVisibility(android.view.View.GONE);
                fileName.setText("🎵 Audio loaded");

                mediaPlayer.setOnCompletionListener(mp -> {
                    seekBar.setProgress(0);
                    fileName.setText("✅ Audio complete");
                    handler.removeCallbacks(seekBarUpdater);
                });
            }

        } else if (requestCode == 2) {
            // VIDEO
            releaseMediaPlayer();
            isAudio = false;

            videoView.setVisibility(android.view.View.VISIBLE);
            seekBar.setProgress(0);

            videoView.setVideoURI(mediaUri);
            videoView.start();
            fileName.setText("🎬 Playing video");

            videoView.setOnCompletionListener(mp ->
                    fileName.setText("✅ Video complete")
            );
        }
    }

    // 🔐 PERMISSION
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO}, 100);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }

    // 🧹 CLEANUP
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) mediaPlayer.stop();
            handler.removeCallbacks(seekBarUpdater);
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        releaseMediaPlayer();
        super.onDestroy();
    }
}