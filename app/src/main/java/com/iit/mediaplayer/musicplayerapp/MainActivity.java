package com.iit.mediaplayer.musicplayerapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String MEDIA_PLAYER_APP_MESSENGER_KEY = "app_messenger";

    private Button mPlayButton;
    private Button mStopButton;


    private AppHandler mHandler;
    private Messenger mAppMessenger;
    private MediaPlayerServiceConnection mConnection = new MediaPlayerServiceConnection();
    private Messenger messengerToService;

    private boolean isServiceConnected = false;

    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayButton = (Button) findViewById(R.id.play_button);
        mPlayButton.setOnClickListener(this);

        mStopButton = (Button) findViewById(R.id.stop_button);
        mStopButton.setOnClickListener(this);

        mHandler = new AppHandler(this);
        mAppMessenger = new Messenger(mHandler);



        Intent serviceIntent = new Intent(this,
                AudioPlayerService.class);
        serviceIntent.putExtra(MEDIA_PLAYER_APP_MESSENGER_KEY, mAppMessenger);
        startService(serviceIntent);

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_button:
                if (!isPlaying) {
                    playAudio();
                } else {
                    pauseAudio();
                }
                break;
            case R.id.stop_button:
                stopAudio();
                break;
        }
    }


    private void playAudio() {
        if (messengerToService != null) {
            try {
                Message message = Message.obtain();
                message.what = AudioPlayerService.MEDIA_PLAYER_CONTROL_START;
                messengerToService.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    private void pauseAudio() {
        if (messengerToService != null) {
            try {
                Message message = Message.obtain();
                message.what = AudioPlayerService.MEDIA_PLAYER_CONTROL_PAUSE;
                messengerToService.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }

    private void stopAudio() {
        if (messengerToService != null) {
            try {
                Message message = Message.obtain();
                message.what = AudioPlayerService.MEDIA_PLAYER_CONTROL_STOP;
                messengerToService.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

    }


    private void doBind() {
        Log.v("log_iit", "request service bind in activity");
        bindService(
                new Intent(this, AudioPlayerService.class),
                mConnection, Context.BIND_AUTO_CREATE);

    }

    private void doUnbindService() {
        if (messengerToService != null) {
            try {
                Message message = Message.obtain();
                message.what = AudioPlayerService.MEDIA_PLAYER_SERVICE_CLIENT_UNBOUND;
                messengerToService.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    private void updatePlayButton() {
        isPlaying = true;
        mPlayButton.setText("Pause");

    }

    private void updatePauseButton() {
        isPlaying = false;
        mPlayButton.setText("Play");
    }

    private void stopPerformed() {
        isPlaying = false;
        mPlayButton.setText("Play");
    }


    /***********************************************************/
    /***************** private classes *************************/
    /**
     * *******************************************************
     */

    private class MediaPlayerServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {

            isServiceConnected = true;
            messengerToService = new Messenger(binder);

            Log.v("log_iit","service connected");

            //try {
            //Message message = Message.obtain();
            //message.what = MediaPlayerService.MEDIA_PLAYER_GET_PODCASTS;
            //messengerToService.send(message);
            //} catch (RemoteException e1) {
            //  e1.printStackTrace();
            //}
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            messengerToService = null;
        }
    }

    private static class AppHandler extends Handler {

        private final WeakReference<MainActivity> mTarget;

        private AppHandler(MainActivity target) {
            mTarget = new WeakReference<MainActivity>(target);
        }

        @Override
        public void handleMessage(Message message) {

            MainActivity target = mTarget.get();
            Bundle bundle;
            switch (message.what) {
                case AudioPlayerService.MEDIA_PLAYER_SERVICE_STARTED:
                    target.doBind();
                    break;
                case AudioPlayerService.MEDIA_PLAYER_CONTROL_START:
                    target.updatePlayButton();
                    break;
                case AudioPlayerService.MEDIA_PLAYER_CONTROL_PAUSE:
                    target.updatePauseButton();
                    break;
                case AudioPlayerService.MEDIA_PLAYER_CONTROL_STOP:
                    target.stopPerformed();
                    break;

            }
        }
    }


}
