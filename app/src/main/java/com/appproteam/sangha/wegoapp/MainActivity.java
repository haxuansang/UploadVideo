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
    VideoView videoView;
    MediaController mediaController;
    SeekBar seekBar;
    private TextView runningTime;
    private ImageView imvPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mediaController = new MediaController(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Uploading Video");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        btnChoose = findViewById(R.id.button);
        seekBar = findViewById(R.id.seekbar);
        videoView = (VideoView) findViewById(R.id.myVideo);
        runningTime = (TextView) findViewById(R.id.runningTime);
        imvPlay = (ImageView) findViewById(R.id.controlVideo);
        runningTime.setText("00:00");
        final String vidAddress = "https://firebasestorage.googleapis.com/v0/b/wegoapp-935c3.appspot.com/o/videos%2F16912?alt=media&token=d544757b-4c77-4bab-b5c8-4973f5581c22";
        videoView.setVideoURI(Uri.parse(vidAddress));
        imvPlay.setOnClickListener(new View.OnClickListener() {
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

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseVideo();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        });


    }

    private Runnable refreshTime = new Runnable() {
        @Override
        public void run() {
            seekBar.setProgress(videoView.getCurrentPosition());
            if (videoView.isPlaying())
                videoView.postDelayed(refreshTime, 1000);
            int time = videoView.getCurrentPosition() / 1000;
            int minute = time / 60;
            int second = time % 60;
            if (minute < 10) {
                if (second < 10)
                    runningTime.setText("0" + minute + ":0" + second);
                else
                    runningTime.setText("0" + minute + ":" + second);
            } else {
                if (second < 10)
                runningTime.setText(minute + ":0" + second);
                else
                runningTime.setText(minute + ":" + second);
            }


        }
    };

    private void updateVideoBar() {
        new Thread(new Runnable() {
            public void run() {
                do {
                    seekBar.post(new Runnable() {
                        public void run() {
                            seekBar.setProgress(videoView.getCurrentPosition());
                            int time = videoView.getCurrentPosition() / 1000;
                            int minute = time / 60;
                            int second = time % 60;
                            if (minute < 10) {
                                if (second < 10)
                                    runningTime.setText("0" + minute + ":0" + second);
                                else
                                    runningTime.setText("0" + minute + ":" + second);
                            } else {
                                if (second < 10)
                                    runningTime.setText(minute + ":0" + second);
                                else
                                    runningTime.setText(minute + ":" + second);
                            }
                        }
                    });
                    try {
                        Thread.sleep(200);
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
                        videoView.setVideoURI(uri);
                        videoView.setVisibility(View.VISIBLE);
                        videoView.start();
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
