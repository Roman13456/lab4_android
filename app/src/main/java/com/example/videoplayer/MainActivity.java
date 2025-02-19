package com.example.videoplayer;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainActivity extends AppCompatActivity {

    private Button video_btn, audio_btn;
    private Video_frag videoFrag;
    private Audio_frag audioFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        video_btn = findViewById(R.id.video_fragment_btn);
        audio_btn = findViewById(R.id.audio_fragment_btn);

        // Initialize fragments
        videoFrag = new Video_frag();
        audioFrag = new Audio_frag();

        // Set up the default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, videoFrag)
                    .commit();
        }

        // Listener for video button
        video_btn.setOnClickListener(v -> {
            switchFragment(videoFrag);
            v.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.active));
            audio_btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.non_active));
        });

        // Listener for audio button
        audio_btn.setOnClickListener(v -> {
            switchFragment(audioFrag);
            v.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.active));
            video_btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.non_active));
        });
    }

    private void switchFragment(Fragment fragment) {
        // Replace the current fragment with the one clicked
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}


