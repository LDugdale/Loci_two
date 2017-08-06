package com.lauriedugdale.loci.ui.activity.entry;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.lauriedugdale.loci.services.AudioService;
import com.lauriedugdale.loci.audio.AudioUtilities;
import com.lauriedugdale.loci.data.dataobjects.GeoEntry;
import com.lauriedugdale.loci.R;
import com.lauriedugdale.loci.ui.fragment.EntryFragment;

public class AudioEntryActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener, EntryFragment.OnFragmentInteractionListener  {

    // audio player controls
    private ImageButton mBtnPlay;
    private ImageButton mBtnForward;
    private ImageButton mBtnBackward;
    private SeekBar mSongProgressBar;
    private TextView mSongCurrentDurationLabel;
    private TextView mSongTotalDurationLabel;

    // Media Player
    private MediaPlayer mMediaPlayer;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();
    private final int SEEK_BACKWARD_TIME = 5000; // 5000 milliseconds
    private final int SEEK_FORWARD_TIME = 5000; // 5000 milliseconds

    private GeoEntry mGeoEntry;

    private AudioService audioService;
    private boolean serviceBound=false;

    //connect to the service
    private ServiceConnection audioConnection = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.AudioBinder binder = (AudioService.AudioBinder)service;
            //get service
            audioService = binder.getService();
            mMediaPlayer = audioService.getMediaPlayer();
            serviceBound = true;
            playSong();

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            serviceBound = false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_entry);

        // get the GeoEntry to display info on this page
        mGeoEntry = getIntent().getParcelableExtra(Intent.ACTION_OPEN_DOCUMENT);

        // Fetch UI elements
        mBtnPlay = (ImageButton) findViewById(R.id.btnPlay);
        mBtnForward = (ImageButton) findViewById(R.id.btnForward);
        mBtnBackward = (ImageButton) findViewById(R.id.btnBackward);
        mSongProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        mSongCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        mSongTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

        Intent playerIntent = new Intent(this, AudioService.class);
        playerIntent.putExtra("entry", mGeoEntry);
        startService(playerIntent);
        bindService(playerIntent, audioConnection, Context.BIND_AUTO_CREATE);

        // media player controls
        playButton();
        forwardButton();
        backwardButton();

        if (findViewById(R.id.fragment_container) != null) {
            if (savedInstanceState != null) {
                return;
            }

            Fragment entryFragment = new EntryFragment();

            entryFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, entryFragment).commit();
        }

        // my_child_toolbar is defined in the layout file
        Toolbar myChildToolbar =
                (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myChildToolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.light_grey), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);

        ab.setTitle("Entry");

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Method for the play button
     * Controls play and pause, changes according to the current action
     * */
    public void playButton(){

        mBtnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check if playing
                if (mMediaPlayer.isPlaying()) {
                    if (mMediaPlayer != null) {
                        audioService.pauseMedia();
                        // Change to play image
                        mBtnPlay.setImageResource(R.drawable.btn_play);
                    }
                } else {
                    // Resume song
                    if (mMediaPlayer != null) {
                        audioService.playMedia();
                        // Change to pause image
                        mBtnPlay.setImageResource(R.drawable.btn_pause);
                    }
                }

            }
        });
    }

    /**
     * Forward button click event
     * Forwards song by the seconds specified in the field variable
     * */
    public void forwardButton(){

        mBtnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mMediaPlayer.getCurrentPosition();
                // if forward time is less than duration move foward
                if (currentPosition + SEEK_FORWARD_TIME <= mMediaPlayer.getDuration()) {
                    // forward song
                    mMediaPlayer.seekTo(currentPosition + SEEK_FORWARD_TIME);
                } else {
                    // else move to end of the track
                    mMediaPlayer.seekTo(mMediaPlayer.getDuration());
                }
            }
        });
    }

    /**
     * Backwards button click event
     * Rewinds the song by the seconds specified in the field variable
     * */
    public void backwardButton(){

        mBtnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // get current song position
                int currentPosition = mMediaPlayer.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if (currentPosition - SEEK_BACKWARD_TIME >= 0) {
                    // forward song
                    mMediaPlayer.seekTo(currentPosition - SEEK_BACKWARD_TIME);
                } else {
                    // backward to starting position
                    mMediaPlayer.seekTo(0);
                }

            }
        });
    }

    /**
     * Receiving song index from playlist view
     * and play the song
     * */
    @Override
    protected void onActivityResult(int requestCode,  int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        playSong();
    }


    /**
     * Function to play a song
     *
     */
    public void playSong() {
        // Play song
        try {
            audioService.playMedia();
            // Changing Button Image to pause image
            mBtnPlay.setImageResource(R.drawable.btn_pause);

            // set Progress bar values
            mSongProgressBar.setProgress(0);
            mSongProgressBar.setMax(100);
            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mMediaPlayer.getDuration();
            long currentDuration = mMediaPlayer.getCurrentPosition();

            // Displaying Total Duration time
            mSongTotalDurationLabel.setText("" + AudioUtilities.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            mSongTotalDurationLabel.setText("" + AudioUtilities.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int) (AudioUtilities.getProgressPercentage(currentDuration, totalDuration));
            //Log.d("Progress", ""+progress);
            mSongProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mMediaPlayer.getDuration();
        int currentPosition = AudioUtilities.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mMediaPlayer.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     */
    @Override
    public void onCompletion(MediaPlayer arg0) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateTimeTask);
        audioService.stopSelf();
        if (serviceBound) {
            unbindService(audioConnection);
        }
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}

