package com.example.videoplayer;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.content.Intent;
import java.io.IOException;

public class Audio_frag extends Fragment {

    private MediaPlayer mediaPlayer;
    private Uri audioUri;
    private boolean isStreaming = false;

    private SeekBar seekBar;
    private Handler handler = new Handler();
    private Button buttonPlay, buttonStop, buttonStream;
    private EditText streamLink;

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_audio_frag, container, false);

        Button buttonSelect = view.findViewById(R.id.button_select);
        buttonPlay = view.findViewById(R.id.button_play);
        buttonStop = view.findViewById(R.id.button_stop);
        buttonStream = view.findViewById(R.id.button_stream);
        streamLink = view.findViewById(R.id.stream_link);
        seekBar = view.findViewById(R.id.seekBar);

        buttonPlay.setEnabled(false);
        buttonStop.setEnabled(false);
        seekBar.setEnabled(false);

        ActivityResultLauncher<Intent> audioPicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        if (mediaPlayer != null) {
                            mediaPlayer.stop();
                            mediaPlayer.release();
                            mediaPlayer = null;
                            resetPlayer();
                        }
                        isStreaming = false;
                        audioUri = result.getData().getData();
                        mediaPlayer = MediaPlayer.create(getContext(), audioUri);
                        buttonPlay.setEnabled(true);
                        buttonStop.setEnabled(true);
                        seekBar.setEnabled(true);

                        mediaPlayer.start();
                        updateSeekBar();
                        buttonPlay.setText("⏸ Pause");

                        seekBar.setMax(mediaPlayer.getDuration());
                        mediaPlayer.setOnCompletionListener(mp -> resetPlayer());
                    }
                });

        buttonSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("audio/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            audioPicker.launch(intent);
        });

        buttonPlay.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    buttonPlay.setText("▶ Play");
                } else {
                    mediaPlayer.start();
                    updateSeekBar();
                    buttonPlay.setText("⏸ Pause");
                }
            }
        });

        buttonStop.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
                resetPlayer();
                if (isStreaming){
                    streamAudio(true);
                } else if(audioUri != null) {
                    mediaPlayer = MediaPlayer.create(getContext(), audioUri);
                    mediaPlayer.setOnCompletionListener(mp -> resetPlayer());
                }
            }
        });

        buttonStream.setOnClickListener(v -> streamAudio(false));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        return view;
    }

    private void streamAudio(boolean startPaused){
        isStreaming = true;
        String url = streamLink.getText().toString().trim();
        if (!isValidAudioUrl(url)) {
            Toast.makeText(getContext(), "❌ Invalid audio URL!", Toast.LENGTH_LONG).show();
            return;
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            resetPlayer();
        }

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                buttonPlay.setEnabled(true);
                buttonStop.setEnabled(true);
                seekBar.setEnabled(true);
                seekBar.setMax(mediaPlayer.getDuration());
                if (!startPaused){
                    mediaPlayer.start();
                    updateSeekBar();
                    buttonPlay.setText("⏸ Pause");
                }

            });
            mediaPlayer.setOnCompletionListener(mp -> resetPlayer());
        } catch (IOException e) {
            Toast.makeText(getContext(), "❌ Unable to stream audio!", Toast.LENGTH_LONG).show();
        }
    }

    private void updateSeekBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            handler.postDelayed(this::updateSeekBar, 500);
        }
    }

    private void resetPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(0);
        }
        buttonPlay.setText("▶ Play");
        seekBar.setProgress(0);
        handler.removeCallbacksAndMessages(null);
    }

    private boolean isValidAudioUrl(String url) {
        return url.matches(".*\\.(mp3|wav|aac|ogg|m4a|flac)$");
    }
}
