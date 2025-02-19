package com.example.videoplayer;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;


public class Video_frag extends Fragment {
    private ExoPlayer exoPlayer;
    private PlayerView playerView;

    private EditText textField;

    ActivityResultLauncher<Intent> videoPicker;
    private Uri videoUri;
    private Button buttonStop, buttonStream;
    private void playVideo(Uri uri) {
        MediaItem mediaItem = MediaItem.fromUri(uri);
        exoPlayer.setMediaItem(mediaItem);
        exoPlayer.prepare();
        exoPlayer.setPlaybackParameters(new PlaybackParameters(1.0f));
        exoPlayer.play();
        buttonStop.setEnabled(true);

    }
    private boolean isVideoUrl(String url) {
        return url.matches(".*\\.(mp4|m3u8|webm|mkv|avi|mov|flv|wmv|3gp|ogg)$");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (exoPlayer != null) {
            if (exoPlayer.isPlaying()) {
                exoPlayer.stop();
            }
            exoPlayer.clearMediaItems();
            exoPlayer.release();
            exoPlayer = null;
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_video_frag, container, false);
        Button buttonSelect = view.findViewById(R.id.button_select);
        buttonStop = view.findViewById(R.id.button_stop);
        buttonStream = view.findViewById(R.id.button_stream);
        textField = view.findViewById(R.id.stream_link);
        playerView = view.findViewById(R.id.playerView);

        buttonStop.setEnabled(false);

        exoPlayer = new ExoPlayer.Builder(requireContext()).build();
        playerView.setPlayer(exoPlayer);

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(@NonNull com.google.android.exoplayer2.PlaybackException error) {
                Toast.makeText(requireContext(), "❌ Cannot play video!", Toast.LENGTH_LONG).show();
            }
        });

        videoPicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
                        videoUri = result.getData().getData();
                        playVideo(videoUri);
                    }
                });

        buttonSelect.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("video/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            videoPicker.launch(intent);
        });

        buttonStream.setOnClickListener(v -> {
            String videoUrl = textField.getText().toString().trim();
            if (isVideoUrl(videoUrl)) {
                Uri videoUri = Uri.parse(videoUrl);
                playVideo(videoUri);
            } else {
                Toast.makeText(requireActivity(), "❌ invalid link for the video", Toast.LENGTH_LONG).show();
            }
        });

        buttonStop.setOnClickListener(v -> {
            if (exoPlayer != null) {
                exoPlayer.stop();
                exoPlayer.prepare(); // Робимо плеєр знову готовим до відтворення
                exoPlayer.seekTo(0);
                exoPlayer.pause();
            }
        });
        return view;
    }
}