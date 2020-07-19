package com.example.vmusicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Random;

import static com.example.vmusicplayer.AlbumDetailsAdapter.albumFiles;
import static com.example.vmusicplayer.ArtistDetailsAdapter.artistFiles;
import static com.example.vmusicplayer.MainActivity.musicFiles;
import static com.example.vmusicplayer.MainActivity.repeatBoolean;
import static com.example.vmusicplayer.MainActivity.shuffleBoolean;

//Created by Vimal_Pandey on 11/07/2020

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener {
    TextView song_name,artist_name,duration_played,duration_total;
    ImageView cover_art,nextBtn,prevBtn,backBtn,shuffleBtn,repeatBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar,seekVol;
    int position=-1;
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler=new Handler();
    AudioManager audioManager;
    static ArrayList<MusicFiles> listSongs=new ArrayList<>();
    private Thread playThread,prevThread,nextThread;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Real time reflect the SeekVolume bar change
        if(keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            int index=seekVol.getProgress();
            seekVol.setProgress(index+1);
            return true;
        }
        else if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
            int index=seekVol.getProgress();
            seekVol.setProgress(index-1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        //Animation in background
        RelativeLayout relativeLayout=findViewById(R.id.mContainer);
        AnimationDrawable animationDrawable=(AnimationDrawable)relativeLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();

           initViews();
           getIntentMethod();
           song_name.setText(listSongs.get(position).getTitle());
           artist_name.setText(listSongs.get(position).getArtists());
        //Volume SeekBar Activation
        audioManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int maxVol=audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int curVol=audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekVol.setMax(maxVol);
        seekVol.setProgress(curVol);
        seekVol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,progress,0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mediaPlayer.setOnCompletionListener(this);
           //Progress SeekBAr
           seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
               @Override
               public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                   if(mediaPlayer!=null&& fromUser){
                       mediaPlayer.seekTo(progress*1000);
                   }
               }

               @Override
               public void onStartTrackingTouch(SeekBar seekBar) {

               }

               @Override
               public void onStopTrackingTouch(SeekBar seekBar) {

               }
           });
           PlayerActivity.this.runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   if(mediaPlayer!=null){
                       int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                       seekBar.setProgress(mCurrentPosition);
                       duration_played.setText(formattedTime(mCurrentPosition));
                   }
                   handler.postDelayed(this,1000);


               }
           });
           shuffleBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if(shuffleBoolean){
                       shuffleBoolean=false;
                       shuffleBtn.setImageResource(R.drawable.ic_shuffle);
                   }
                   else{
                       shuffleBoolean=true;
                       shuffleBtn.setImageResource(R.drawable.ic_shuffle_off);
                   }
               }
           });
           repeatBtn.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   if(repeatBoolean){
                       repeatBoolean=false;
                       repeatBtn.setImageResource(R.drawable.ic_repeat_off);
                   }
                   else
                   {
                       repeatBoolean=true;
                       repeatBtn.setImageResource(R.drawable.ic_repeat_on);
                   }
               }
           });
    }

   @Override
   protected void onResume(){
        plaThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
   }

    private void prevThreadBtn() {
        prevThread=new Thread()
        {
            @Override
            public void run(){
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevBtnClicked();
                    }
                });

            }

        };
        prevThread.start();

    }

    private void prevBtnClicked() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean&&!repeatBoolean){
                position=getRandom(listSongs.size()-1);
            }
            else if(!shuffleBoolean&&!repeatBoolean){
                position=((position-1)<0?(listSongs.size()-1):(position-1));
            }
            uri=Uri.parse(listSongs.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            metaDta(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtists());
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();

        }
        else{
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean&&!repeatBoolean){
                position=getRandom(listSongs.size()-1);
            }
            else if(!shuffleBoolean&&!repeatBoolean){
                position=((position-1)<0?(listSongs.size()-1):(position-1));
            }
            uri=Uri.parse(listSongs.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            metaDta(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtists());
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play_arrow);

        }
    }


    private void nextThreadBtn() {
        nextThread=new Thread()
        {
            @Override
            public void run(){
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextBtnClicked();
                    }
                });

            }

        };
        nextThread.start();

    }

    private void nextBtnClicked() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean&&!repeatBoolean){
                position=getRandom(listSongs.size()-1);
            }
            else if(!shuffleBoolean&&!repeatBoolean){
                position=((position+1)%listSongs.size());
            }

            uri=Uri.parse(listSongs.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            metaDta(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtists());
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.ic_pause);
            mediaPlayer.start();

        }
        else{
            mediaPlayer.stop();
            mediaPlayer.release();
            if(shuffleBoolean&&!repeatBoolean){
                position=getRandom(listSongs.size()-1);
            }
            else if(!shuffleBoolean&&!repeatBoolean){
                position=((position+1)%listSongs.size());
            }
            uri=Uri.parse(listSongs.get(position).getPath());
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            metaDta(uri);
            song_name.setText(listSongs.get(position).getTitle());
            artist_name.setText(listSongs.get(position).getArtists());
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            playPauseBtn.setBackgroundResource(R.drawable.ic_play_arrow);

        }
    }

    private int getRandom(int i) {
        Random random=new Random();
        return random.nextInt(i+1);
    }

    private void plaThreadBtn() {
        playThread=new Thread()
        {
           @Override
            public void run(){
               super.run();
               playPauseBtn.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       playPauseBtnClicked();
                   }
               });

        }

        };
        playThread.start();

    }

    private void playPauseBtnClicked() {
        if(mediaPlayer.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.ic_play_arrow);
            mediaPlayer.pause();
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this,1000);
                }
            });
        }
        else{
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            mediaPlayer.start();
            seekBar.setMax(mediaPlayer.getDuration()/1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mediaPlayer!=null){
                        int mCurrentPosition=mediaPlayer.getCurrentPosition()/1000;
                        seekBar.setProgress(mCurrentPosition);

                    }
                    handler.postDelayed(this,1000);


                }
            });
        }

    }


    //Return musci Played and total time after changing into minute
    private String formattedTime(int mCurrentPosition) {
        String totalout="";
        String totslNow="";
        String seconds=String.valueOf(mCurrentPosition%60);
        String minutes=String.valueOf(mCurrentPosition/60);
        totalout=minutes+":"+seconds;
        totslNow=minutes+":"+"0"+seconds;
        if(seconds.length()==1){
            return totslNow;
        }
        else {
            return totalout;
        }

    }

    private void getIntentMethod() {
        position=getIntent().getIntExtra("position",-1);
        String sender=getIntent().getStringExtra("sender");
        String Senderartist=getIntent().getStringExtra("artistSender");
        if(Senderartist!=null&& Senderartist.equals("artistDetails")){
            listSongs=artistFiles;
        }
        else if (sender!=null && sender.equals("albumDetails")){
            listSongs=albumFiles;
        }
        else{
        listSongs=musicFiles;}
        if(listSongs!=null){
            playPauseBtn.setImageResource(R.drawable.ic_pause);
            uri=Uri.parse(listSongs.get(position).getPath());

        }
        if(mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        else{
            mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        seekBar.setMax(mediaPlayer.getDuration()/1000);
        metaDta(uri);


    }

    private void initViews() {
        //Initializing Global variables with Ids of xml
        song_name=findViewById(R.id.song_name);
        artist_name=findViewById(R.id.song_artist);
        duration_played=findViewById(R.id.durationPlayed);
        duration_total=findViewById(R.id.durationTotal);
        cover_art=findViewById(R.id.cover_art);
        nextBtn=findViewById(R.id.next);
        prevBtn=findViewById(R.id.id_prev);
        backBtn=findViewById(R.id.back_btn);
        shuffleBtn=findViewById(R.id.shuffle);
        repeatBtn=findViewById(R.id.repeat);
        playPauseBtn=findViewById(R.id.play_pause);
        seekBar=findViewById(R.id.seekBar1);
        seekVol=findViewById(R.id.seekBar);
    }


    private void metaDta(Uri uri){
        MediaMetadataRetriever retriever=new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal=Integer.parseInt(listSongs.get(position).getDuration())/1000;
        duration_total.setText(formattedTime(durationTotal));
        byte[] art=retriever.getEmbeddedPicture();
        if(art!=null){
            Glide.with(this).asBitmap().load(art).into(cover_art);
        }
        else{
            Glide.with(this).asBitmap().load(R.drawable.music).into(cover_art);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
       nextBtnClicked();
       if(mediaPlayer!=null){
           mediaPlayer=MediaPlayer.create(getApplicationContext(),uri);
           mediaPlayer.start();
           mediaPlayer.setOnCompletionListener(this);
       }
    }
}