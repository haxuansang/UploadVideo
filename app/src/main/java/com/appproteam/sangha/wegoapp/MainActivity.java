package com.appproteam.sangha.wegoapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import android.widget.MediaController;
import android.widget.VideoView;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "sangha123";
    private StorageReference mStorageRef;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 1111;
    private Button btnChoose;
    public ProgressDialog progressDialog;
    MediaController mediaController;
    SeekBar seekBar;
    private PlayerView playerView;
    private boolean playWhenReady;
    private int currentWindow=0;
    private long playbackPosition=0;
    final String vidAddress = "https://firebasestorage.googleapis.com/v0/b/wegoapp-935c3.appspot.com/o/videos%2F15994?alt=media&token=8ec9dfce-4eed-4912-a22d-af991d1ee67a";
    private SimpleExoPlayer player;
    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer-codelab")).
                createMediaSource(uri);
    }

    private void initializePlayer() {
        player = ExoPlayerFactory.newSimpleInstance(
                new DefaultRenderersFactory(this),
                new DefaultTrackSelector(), new DefaultLoadControl());
        updateVideoBar();
        player.addListener(new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

            }

            @Override
            public void onLoadingChanged(boolean isLoading) {

            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if (playbackState == ExoPlayer.STATE_ENDED) {
                    Log.e(TAG, "onPlayerStateChanged: 1" );
                    Toast.makeText(getApplicationContext(), "Playback ended", Toast.LENGTH_LONG).show();
                }
                else if (playbackState == ExoPlayer.STATE_BUFFERING)
                {
                    Log.e(TAG, "onPlayerStateChanged: 2" );
                    Toast.makeText(getApplicationContext(), "Buffering..", Toast.LENGTH_SHORT).show();
                }
                else if (playbackState == ExoPlayer.STATE_READY)
                {

                    Log.e(TAG, "onPlayerStateChanged: 3" );
                }

            }

            @Override
            public void onRepeatModeChanged(int repeatMode) {

            }

            @Override
            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

            }

            @Override
            public void onPlayerError(ExoPlaybackException error) {

            }

            @Override
            public void onPositionDiscontinuity(int reason) {

            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

            }

            @Override
            public void onSeekProcessed() {

            }
        });
        playerView.setPlayer(player);
        player.setPlayWhenReady(playWhenReady);
        player.seekTo(currentWindow, playbackPosition);
        Uri uri = Uri.parse(vidAddress);
        MediaSource mediaSource = buildMediaSource(uri);
        player.prepare(mediaSource, true, false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Util.SDK_INT > 23) {
            initializePlayer();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();

    }
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }
    @Override
    public void onPause() {
        super.onPause();
        if (Util.SDK_INT <= 23) {
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT > 23) {
            releasePlayer();
        }
    }


    private void releasePlayer() {
        if (player != null) {
            playbackPosition = player.getCurrentPosition();
            currentWindow = player.getCurrentWindowIndex();
            playWhenReady = player.getPlayWhenReady();
            player.release();
            player = null;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerView = findViewById(R.id.myVideo);
        mediaController = new MediaController(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Video");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        btnChoose = findViewById(R.id.button);

      /*  seekBar = findViewById(R.id.seekbar);
        runningTime = (TextView) findViewById(R.id.runningTime);
        imvPlay = (ImageView) findViewById(R.id.controlVideo);*/
       // runningTime.setText("00:00");
       // videoView.setVideoURI(Uri.parse(vidAddress));
      /*  imvPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoView != null) {
                    if (videoView.isPlaying()) {
                        Log.e(TAG, "onClick: 1");
                        videoView.pause();
                        imvPlay.setImageResource(R.drawable.ic_play);
                    } else {
                        Log.e(TAG, "onClick: 2");
                        if (videoView.isPlaying())
                            videoView.resume();
                        else
                            videoView.start();
                        imvPlay.setImageResource(R.drawable.ic_pause);
                    }
                }

            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int x = videoView.getDuration();
                seekBar.setMax(x);
                updateVideoBar();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                imvPlay.setImageResource(R.drawable.ic_play);

            }
        });
*/
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseVideo();
            }
        });

   /*     seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo(progress);
                    videoView.start();
                    imvPlay.setImageResource(R.drawable.ic_pause);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                videoView.seekTo(seekBar.getProgress());
            }
        });*/


    }


    private void updateVideoBar() {
        new Thread(new Runnable() {
            public void run() {
                do {
                    playerView.post(new Runnable() {
                        public void run() {
                            Log.e(TAG, "run: "+ player.getCurrentPosition());
                        }
                    });
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                while (true);
            }
        }).start();
    }

    private void chooseVideo() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_TAKE_GALLERY_VIDEO);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                progressDialog.show();
                Uri selectedImageUri = data.getData();
                //filemanagerstring = getPath(selectedImageUri);
                uploadVideo(selectedImageUri);
            }
        }
    }

    private void uploadVideo(Uri uri) {
        long id = Long.valueOf(uri.getLastPathSegment());
        StorageReference ref = mStorageRef.child("videos/" + id);
        ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                Task<Uri> result = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(MainActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage("uploaded " + progress + "%");
            }
        });
    }


    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }
}
