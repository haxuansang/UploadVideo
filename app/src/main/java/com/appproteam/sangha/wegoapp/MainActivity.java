package com.appproteam.sangha.wegoapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.MediaController;

import com.appproteam.sangha.wegoapp.ConfigController.JakeController;
import com.appproteam.sangha.wegoapp.ConfigController.TimeCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.PlayerManager;
import tcking.github.com.giraffeplayer2.VideoInfo;
import tcking.github.com.giraffeplayer2.VideoView;
import tv.danmaku.ijk.media.player.IjkTimedText;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "sangha123";
    private StorageReference mStorageRef;
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 1111;
    private Button btnChoose;
    public ProgressDialog progressDialog;
    MediaController mediaController;
    VideoView videoView;
    GiraffePlayer mediaPlayer;
    private Handler threadHandler = new Handler();
    public int currentDuration;
    public boolean pickedtime=false;
    MyAsync myAsync;



    @Override
    protected void onPause() {
        super.onPause();
        if (myAsync != null) {
            myAsync.cancel(true);
        }
    }

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
        final String vidAddress = "https://firebasestorage.googleapis.com/v0/b/wegoapp-935c3.appspot.com/o/videos%2F17182?alt=media&token=43102d94-3cb6-4ba5-ad8a-132029d1d9fa";
        videoView = (VideoView) findViewById(R.id.video_view);
        videoView.setVideoPath(vidAddress);
        mediaPlayer = (GiraffePlayer) videoView.getPlayer();
        PlayerManager.getInstance().setMediaControllerGenerator(new PlayerManager.MediaControllerGenerator() {
            @Override
            public tcking.github.com.giraffeplayer2.MediaController create(Context context, VideoInfo videoInfo) {
                return new JakeController(MainActivity.this, new TimeCallback() {
                    @Override
                    public void onTimeCallBack(int a) {
                        Toast.makeText(MainActivity.this, ""+a, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }/*new JakeController(this, new TimeCallback() {
            @Override
            public void onTimeCallBack(int a) {
                Toast.makeText(MainActivity.this, ""+a, Toast.LENGTH_SHORT).show();
            }
        })*/);
        //videoView.setPlayerListener(new ProxyPlayerListene)
        videoView.setPlayerListener(new tcking.github.com.giraffeplayer2.MediaController() {
            @Override
            public void bind(VideoView videoView) {
            }

            @Override
            public void onPrepared(GiraffePlayer giraffePlayer) {
                currentDuration = giraffePlayer.getDuration();
                myAsync = new MyAsync();
                myAsync.setDuration(currentDuration);
                myAsync.execute();
            }

            @Override
            public void onBufferingUpdate(GiraffePlayer giraffePlayer, int percent) {

               // Log.e(TAG, "current pick  " + giraffePlayer.getCurrentPosition());
            }

            @Override
            public boolean onInfo(GiraffePlayer giraffePlayer, int what, int extra) {
                return false;
            }

            @Override
            public void onCompletion(GiraffePlayer giraffePlayer) {
                if (myAsync != null) {
                    myAsync.cancel(true);
                }
            }

            @Override
            public void onSeekComplete(GiraffePlayer giraffePlayer) {

            }

            @Override
            public boolean onError(GiraffePlayer giraffePlayer, int what, int extra) {
                return false;
            }

            @Override
            public void onPause(GiraffePlayer giraffePlayer) {

            }

            @Override
            public void onRelease(GiraffePlayer giraffePlayer) {

            }

            @Override
            public void onStart(GiraffePlayer giraffePlayer) {
                if (currentDuration > 0) {
                    myAsync = new MyAsync();
                    myAsync.setDuration(currentDuration);
                    myAsync.execute();
                }
            }


            @Override
            public void onTargetStateChange(int oldState, int newState) {

            }

            @Override
            public void onCurrentStateChange(int oldState, int newState) {

            }

            @Override
            public void onDisplayModelChange(int oldModel, int newModel) {

            }

            @Override
            public void onPreparing(GiraffePlayer giraffePlayer) {
                Log.e(TAG, "onPreparing: " +giraffePlayer.getCurrentPosition() );
            }

            @Override
            public void onTimedText(GiraffePlayer giraffePlayer, IjkTimedText text) {

            }

            @Override
            public void onLazyLoadProgress(GiraffePlayer giraffePlayer, int progress) {

            }

            @Override
            public void onLazyLoadError(GiraffePlayer giraffePlayer, String message) {

            }
        });



        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseVideo();
            }
        });
    }


    private class MyAsync extends AsyncTask<Void, Integer, Void> {

        int current;
        int duration;

        public MyAsync() {
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        @Override
        protected Void doInBackground(Void... params) {

            do {
                current = mediaPlayer.getCurrentPosition();
                try {
                    publishProgress((int) (current));
                } catch (Exception e) {
                }

                try {
                    Thread.sleep(400);
                } catch (Exception e) {
                }
            }
            while (!isCancelled());
            return null;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.d(TAG, "onProgressUpdate: " + values[0]);
            Log.d(TAG, "onProgressUpdate: current " + mediaPlayer.getCurrentPosition());
            if (current < duration) {
                onCancelled();
            }
        }
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
                     /*   videoView.setVideoURI(uri);
                        videoView.setVisibility(View.VISIBLE);
                        videoView.start();*/
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
