package com.appproteam.sangha.wegoapp.ConfigController;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.appproteam.sangha.wegoapp.R;

import tcking.github.com.giraffeplayer2.DefaultMediaController;
import tcking.github.com.giraffeplayer2.GiraffePlayer;
import tcking.github.com.giraffeplayer2.MediaController;
import tcking.github.com.giraffeplayer2.PlayerManager;
import tcking.github.com.giraffeplayer2.VideoInfo;

import static tcking.github.com.giraffeplayer2.GiraffePlayer.TAG;

public class JakeController extends DefaultMediaController  {
    public TimeCallback timeCallback;
    public JakeController(Context context, TimeCallback timeCallback) {
        super(context);
        this.timeCallback = timeCallback;
    }
    private String generateTime(long time) {
        int totalSeconds = (int) (time / 1000);
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        return hours > 0 ? String.format("%02d:%02d:%02d", hours, minutes, seconds) : String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void initView(View view) {
        super.initView(view);
        super.seekBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "onClick: " );
            }
        });
    }
    protected final SeekBar.OnSeekBarChangeListener seekListenerr = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser)
                return;
            if (!videoView.isCurrentActivePlayer()) {
                return;
            }
            $.id(R.id.app_video_status).gone();//移动时隐藏掉状态image
            GiraffePlayer player = videoView.getPlayer();
            int newPosition = (int) (player.getDuration() * (progress * 1.0 / 1000));
            String time = generateTime(newPosition);
            if (instantSeeking) {
                player.seekTo(newPosition);

            }
            $.id(R.id.app_video_currentTime).text(time);
            Log.e("sangha123", "progess" + progress );
            if (timeCallback!=null) {
                timeCallback.onTimeCallBack(progress);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isDragging = true;
            show(3600000);
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            if (instantSeeking) {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (!videoView.isCurrentActivePlayer()) {
                return;
            }
            GiraffePlayer player = videoView.getPlayer();
            if (!instantSeeking) {
                player.seekTo((int) (player.getDuration() * (seekBar.getProgress() * 1.0 / 1000)));
            }
            show(defaultTimeout);
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            isDragging = false;
            handler.sendEmptyMessageDelayed(MESSAGE_SHOW_PROGRESS, 1000);
        }
    };


}
