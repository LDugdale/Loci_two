package com.lauriedugdale.loci.ui.activity.entry;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lauriedugdale.loci.services.AudioService;
import com.lauriedugdale.loci.utils.AudioUtils;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.fragment.EntryFragment;
import com.lauriedugdale.loci.utils.InterfaceUtils;
/**
 * called for entries with an audio file
 *
 * @author Laurie Dugdale
 */
public class AudioEntryActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener, EntryFragment.OnFragmentInteractionListener {

    // audio player controls
    private ImageButton mPlay;
    private ImageButton mForward;
    private ImageButton mBackward;
    private SeekBar mProgress;
    private TextView mDuration;

    private MediaPlayer mMediaPlayer;

    private Handler mHandler = new Handler();
    // time is in milliseconds
    private final int SEEK_TIME = 5000;

    private GeoEntry mGeoEntry; // the GeoEntry for the current activity

    private AudioService mAudioService;
    private boolean mServiceBound = false;
    // bind the service
    private ServiceConnection audioConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioBinder binder = (AudioService.AudioBinder)service;
            //get service
            mAudioService = binder.getService();
            mMediaPlayer = mAudioService.getmMediaPlayer();
            mServiceBound = true;
            playAudio();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_entry);

        // get the GeoEntry to display info on this page
        mGeoEntry = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);

        // Fetch UI elements
        mPlay = (ImageButton) findViewById(R.id.btnPlay);
        mForward = (ImageButton) findViewById(R.id.btnForward);
        mBackward = (ImageButton) findViewById(R.id.btnBackward);
        mProgress = (SeekBar) findViewById(R.id.songProgressBar);
        mDuration = (TextView) findViewById(R.id.songTotalDurationLabel);

        Intent playerIntent = new Intent(this, AudioService.class);
        playerIntent.putExtra("entry", mGeoEntry);
        startService(playerIntent);
        bindService(playerIntent, audioConnection, Context.BIND_AUTO_CREATE);

        // media player controls
        playButton();
        forwardButton();
        backwardButton();

        // attach the fragment
        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            Fragment entryFragment = new EntryFragment();
            entryFragment.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, entryFragment).commit();
        }

        // setup the action bar
        InterfaceUtils.setUpToolbar(this, R.id.toolbar, "Entry");
    }

    /**
     * listener for mPlay. pauses and plays the current audio. and changes the image accordingly.
     */
    public void playButton(){

        mPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
            // check if playing
            if (mMediaPlayer.isPlaying()) {
                if (mMediaPlayer != null) {
                    mAudioService.pauseAudio();
                    // Change to play image
                    mPlay.setImageResource(R.drawable.play_button);
                }
            } else {
                // Resume song
                if (mMediaPlayer != null) {
                    mAudioService.playAudio();
                    // Change to pause image
                    mPlay.setImageResource(R.drawable.pause_button);
                }
            }
            }
        });
    }

    /**
     * mForward listener on click, we forward the audio by the seconds specified in the SEEK_TIME variable
     */
    public void forwardButton(){

        mForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // store the position in the audio
                int position = mMediaPlayer.getCurrentPosition();
                // if the position plus SEEK_TIME is less than the total duration of the audio
                if (position + SEEK_TIME <= mMediaPlayer.getDuration()) {
                    // move the song position to the current audio position plus SEEK_TIME
                    mMediaPlayer.seekTo(position + SEEK_TIME);
                // else if position plus SEEK_TIME is greater than audio duration go to end of audio
                } else {
                    mMediaPlayer.seekTo(mMediaPlayer.getDuration());
                }
            }
        });
    }

    /**
     * mBackward listener on click, we rewind the audio by the seconds specified in the SEEK_TIME variable
     */
    public void backwardButton(){

        mBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // store the position in the audio
                int position = mMediaPlayer.getCurrentPosition();
                // if the position plus SEEK_TIME is greater than zero
                if (position - SEEK_TIME >= 0) {
                    // move the song position to the current audio position minus SEEK_TIME
                    mMediaPlayer.seekTo(position - SEEK_TIME);
                // else if position minus SEEK_TIME is less than zero go to beginning of audio
                } else {
                    mMediaPlayer.seekTo(0);
                }
            }
        });
    }

    /**
     * on activity call back play the audio
     */
    @Override
    protected void onActivityResult(int requestCode,  int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        playAudio();
    }


    /**
     * Responsible for playing the audio specified in the activity
     */
    public void playAudio() {

        mAudioService.playAudio();
        // change image resource to the pause button
        mPlay.setImageResource(R.drawable.pause_button);
        // set the progress values
        mProgress.setProgress(0);
        mProgress.setMax(100);
        // launch the updateprogress bar method
        setProgressBar();
    }

    /**
     * change progress value
     */
    public void setProgressBar() {
        mHandler.postDelayed(mProgressRunnable, 100);
    }

    /**
     * Runnable keeps track of the progress
     */
    private Runnable mProgressRunnable = new Runnable() {
        public void run() {
            long audioPosition = mMediaPlayer.getCurrentPosition();
            // Displaying time completed playing
            mDuration.setText(String.valueOf( AudioUtils.milliSecondsToTimer(audioPosition)));
            // get the current progress
            mProgress.setProgress(AudioUtils.getProgressPercentage(audioPosition, mMediaPlayer.getDuration()));
            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    @Override
    /**
     * not used
     */
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mProgressRunnable);
    }

    @Override
    /**
     * When progress bar position is selected
     */
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mProgressRunnable);

        int position = AudioUtils.progressToTimer(seekBar.getProgress(),  mMediaPlayer.getDuration());
        // move to currently selected position
        mMediaPlayer.seekTo(position);

        setProgressBar();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mProgressRunnable);
        mAudioService.stopSelf();
        if (mServiceBound) {
            unbindService(audioConnection);
        }
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

