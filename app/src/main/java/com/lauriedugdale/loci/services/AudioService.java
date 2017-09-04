package com.lauriedugdale.loci.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.audio.PlaybackStatus;
import com.lauriedugdale.loci.R;

import java.io.IOException;

/**
 * Code adapted from "A Step by Step Guide to Building an Android Audio Player" App written by Valdio Veliu
 */
public class AudioService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener{

    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_STOP = "ACTION_STOP";

    private static final int NOTIFICATION_ID = 101;

    private MediaPlayer mMediaPlayer;

    // Field variable for MediaSession
    private MediaSessionManager mMediaSessionManager;
    private MediaSessionCompat mMediaSession;
    private MediaControllerCompat.TransportControls mTransportControls;

    private int mPosition; // pauses and resumes MediaPlayer

    private AudioManager mAudioManager;

    // Used to bind the service
    private final IBinder mAudioBind = new AudioBinder();

    //Handle calls
    private boolean mOngoingCall = false;
    private PhoneStateListener mPhoneStateListener;
    private TelephonyManager mTelephonyManager;

    private GeoEntry mGeoEntry;

    public AudioService() {
    }

    public MediaPlayer getmMediaPlayer(){
        return mMediaPlayer;
    }


    /**
     * Service lifecycle methods
     */
    @Override
    public void onCreate() {
        super.onCreate();
        callStateListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.hasExtra("entry")) {
            mGeoEntry = intent.getParcelableExtra("entry");
        }

        // if we cannot have audio focus stop
        if (!requestAudioFocus()) {
            stopSelf();
        }

        // if the MediaSessionManager has not been created
        if (mMediaSessionManager == null) {
            try {
                startMediaSession();
                initMediaPlayer();
            } catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }

        handleIncomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mAudioBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mMediaSessionManager = null;
        mMediaSession.release();
        removeNotification();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            stopAudio();
            mMediaPlayer.release();
        }
        removeAudioFocus();
        //Disable the PhoneStateListener
        if (mPhoneStateListener != null) {
            mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();
        stopForeground(true);
    }

    /**
     * Service Binder
     */
    public class AudioBinder extends Binder {
        public AudioService getService() {
            // Return this instance of LocalService so clients can call public methods
            return AudioService.this;
        }
    }


    /**
     * MediaPlayer callback methods
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        stopAudio();
        removeNotification();
        //stop service
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        playAudio();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
    }

    @Override
    public void onAudioFocusChange(int focusState) {

        //Invoked when the audio focus of the system is updated.
        switch (focusState) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (mMediaPlayer == null) initMediaPlayer();
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }


    /**
     * AudioFocus
     */
    private boolean requestAudioFocus() {
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true;
        }
        //Could not gain focus
        return false;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.abandonAudioFocus(this);
    }


    /**
     * MediaPlayer actions
     */
    private void initMediaPlayer() {
        if (mMediaPlayer == null)
            mMediaPlayer = new MediaPlayer();//new MediaPlayer instance

        //Set up MediaPlayer event listeners
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnInfoListener(this);
        //Reset so that the MediaPlayer is not pointing to another data source
        mMediaPlayer.reset();


        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            // Set the data source to the mediaFile location
            mMediaPlayer.setDataSource(mGeoEntry.getFilePath());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        mMediaPlayer.prepareAsync();
    }

    public void playAudio() {
        if (!mMediaPlayer.isPlaying()) {
            buildNotification(PlaybackStatus.PLAYING);
            mMediaPlayer.start();
        }
    }

    public void stopAudio() {
        if (mMediaPlayer == null) return;
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
        }
    }

    public void pauseAudio() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mPosition = mMediaPlayer.getCurrentPosition();
        }
    }

    public void resumeAudio() {
        if (!mMediaPlayer.isPlaying()) {
            mMediaPlayer.seekTo(mPosition);
            mMediaPlayer.start();
        }
    }


    /**
     * Handle PhoneState changes
     */
    private void callStateListener() {
        // Get mTelephonyManager
        mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // listen for changes in the phone state
        mPhoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    // if there is a call or the phone is ringing pause the audio
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (mMediaPlayer != null) {
                            pauseAudio();
                            mOngoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // if the phone is idle resume the audio
                        if (mMediaPlayer != null) {
                            if (mOngoingCall) {
                                mOngoingCall = false;
                                resumeAudio();
                            }
                        }
                        break;
                }
            }
        };
        // listen for changes in phone call state.
        mTelephonyManager.listen(mPhoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * MediaSession and Notification actions
     */
    private void startMediaSession() throws RemoteException {
        if (mMediaSessionManager != null) return; //mediaSessionManager exists

        mMediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        // Create a new MediaSession
        mMediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        //Get MediaSessions transport controls
        mTransportControls = mMediaSession.getController().getTransportControls();
        //set MediaSession -> ready to receive media commands
        mMediaSession.setActive(true);
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Attach Callback to receive MediaSession updates
        mMediaSession.setCallback(new MediaSessionCompat.Callback() {
            // Implement callbacks
            @Override
            public void onPlay() {
                super.onPlay();
                resumeAudio();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();

                pauseAudio();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                //Stop the service
                stopSelf();
            }

            @Override
            public void onSeekTo(long position) {
                super.onSeekTo(position);
            }
        });
    }

    private void buildNotification(PlaybackStatus playbackStatus) {
        /**
         * Notification actions -> playbackAction()
         *  0 -> Play
         *  1 -> Pause
         */

        int notificationAction = android.R.drawable.ic_media_pause;//needs to be initialized
        PendingIntent play_pauseAction = null;

        //Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            //create the pause action
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            //create the play action
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_image); //replace with your own image

        // Create a new Notification
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                // Hide the timestamp
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(new NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mMediaSession.getSessionToken())
                        // Show our playback controls in the compat view
                        .setShowActionsInCompactView(1))
                // Set the Notification color
                .setColor(getResources().getColor(R.color.colorPrimary))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
//                .setContentText(activeAudio.getArtist())
                .setContentTitle(mGeoEntry.getTitle())
//                .setContentInfo(activeAudio.getTitle())
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }


    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, AudioService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void handleIncomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            mTransportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            mTransportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            mTransportControls.stop();
        }
    }
}
