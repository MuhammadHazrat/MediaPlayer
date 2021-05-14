package com.example.mediaplayer;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {

    ImageButton btnplay, btnnext, btnprev, btnff, btnfr;
    TextView textsname, textsstart, textsstop;
    SeekBar seekMusic;
    BarVisualizer visualizer;
    ImageView imageView;

    String sname;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;
    Thread updateSeekbar;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (visualizer != null) {
            visualizer.release();
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getSupportActionBar().setTitle("Now Playing");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnprev = findViewById(R.id.btnprev);
        btnnext = findViewById(R.id.btnnext);
        btnplay = findViewById(R.id.playbtn);
        btnff = findViewById(R.id.btnff);
        btnfr = findViewById(R.id.btnfr);
        textsname = findViewById(R.id.txtsn);
        textsstart = findViewById(R.id.textsstart);
        textsstop = findViewById(R.id.textsstop);
        seekMusic = findViewById(R.id.seekbar);
        imageView = findViewById(R.id.imageview);
        visualizer = (BarVisualizer) findViewById(R.id.blast);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        Intent i = getIntent();
        Bundle bundle = i.getExtras();

        mySongs = (ArrayList) bundle.getParcelableArrayList("songs");
        String songName = i.getStringExtra("songname");
        position = bundle.getInt("pos", 0);
        textsname.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        sname = mySongs.get(position).getName();
        textsname.setText(sname);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.start();

        updateSeekbar = new Thread() {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition = 0;
                while (currentPosition < totalDuration) {

                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekMusic.setProgress(currentPosition);
                    }
                    catch (InterruptedException | IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        seekMusic.setMax(mediaPlayer.getDuration());
        updateSeekbar.start();
        seekMusic.getProgressDrawable().setColorFilter(Color.parseColor("#FF362E"), PorterDuff.Mode.MULTIPLY);
        seekMusic.getThumb().setColorFilter(Color.parseColor("#FF362E"), PorterDuff.Mode.SRC_IN);
        seekMusic.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        String endTime = createTime(mediaPlayer.getDuration());
        textsstop.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                textsstart.setText(currentTime);
                handler.postDelayed(this, delay);
            }
        }, delay);



        btnplay.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                btnplay.setBackgroundResource(R.drawable.ic_baseline_play_arrow_24);
                mediaPlayer.pause();
            }
            else {
                btnplay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                mediaPlayer.start();
            }
        });


        int audioSessionId = mediaPlayer.getAudioSessionId();
        if (audioSessionId != -1) {
            visualizer.setAudioSessionId(audioSessionId);
        }

        btnnext.setOnClickListener(v -> {
            mediaPlayer.stop();
            mediaPlayer.release();
            position = ((position+1) % mySongs.size());
            Uri u = Uri.parse(mySongs.get(position).toString());
            mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
            sname = mySongs.get(position).getName();
            textsname.setText(sname);
            mediaPlayer.start();
            btnplay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
            startAnimation(imageView);

            int audioSessionId1 = mediaPlayer.getAudioSessionId();
            if (audioSessionId1 != -1) {
                visualizer.setAudioSessionId(audioSessionId1);
            }

        });

        btnprev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), u);
                sname = mySongs.get(position).getName();
                textsname.setText(sname);
                mediaPlayer.start();
                btnplay.setBackgroundResource(R.drawable.ic_baseline_pause_24);
                startAnimation(imageView);

                int audioSessionId = mediaPlayer.getAudioSessionId();
                if (audioSessionId != -1) {
                    visualizer.setAudioSessionId(audioSessionId);
                }

            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                btnnext.performClick();
            }
        });

        btnff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }
            }
        });

        btnfr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }
            }
        });

    }

    public void startAnimation(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(imageView, "rotation",0f,360f);
        animator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animator);
        animatorSet.start();
    }

    public String createTime(int duration) {
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;

        time += min+":";

        if (sec < 10) {
            time += "0";
        }
        time+=sec;

        return time;
    }
}