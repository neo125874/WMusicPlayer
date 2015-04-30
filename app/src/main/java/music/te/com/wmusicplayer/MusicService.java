package music.te.com.wmusicplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by tw4585 on 2015/4/23.
 */
public class MusicService extends Service
        implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener
{
    //media player
    private MediaPlayer player;
    //song list
    private ArrayList<Song> songs;
    //current position
    private int songPosn;

    private final IBinder musicBind = new MusicBinder();

    private String songTitle="";
    private static final int NOTIFY_ID=1;

    private boolean shuffle=false;
    private Random rand;

    private boolean repeat = false;

    public void onCreate(){
        //create the service
        //create the service
        super.onCreate();
        //initialize position
        /** init with -1: first time to the listview and need not to play **/
        songPosn=-1;
        //create player
        player = new MediaPlayer();

        initMusicPlayer();

        rand=new Random();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    public void setShuffle(){
        if(shuffle)
            shuffle=false;
        else
            shuffle=true;
    }

    public void setRepeat()
    {
        if (repeat)
            repeat = false;
        else
            repeat = true;
    }

    public void playPrev(){
        songPosn--;
        if(songPosn<0)
            songPosn=songs.size()-1;
        playSong();
    }

    //repeat
    public void playRepeat()
    {
        //keep the song position
        playSong();
    }

    //skip to next
    public void playNext(){
        /*songPosn++;
        if(songPosn>=songs.size())
            songPosn=0;
        playSong();*/

        /** if position = -1 means the first time to load the list in **/
        if(songPosn >= 0)
        {
            if(shuffle){
                int newSong = songPosn;
                while(newSong==songPosn){
                    newSong=rand.nextInt(songs.size());
                }
                songPosn=newSong;
            }
            else{
                songPosn++;
                if(songPosn>=songs.size())
                    songPosn=0;
            }
            playSong();
        }

    }
    /***
     * You could enhance this functionality by using a queue of songs and preventing any song from being repeated until all songs have been played. *
     ***/

    public int getPosn(){
        return player.getCurrentPosition();
    }

    public int getDur(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    public void setSong(int songIndex){
        songPosn=songIndex;
    }

    public void playSong(){
        //play a song
        player.reset();
        //get song
        Song playSong = songs.get(songPosn);

        songTitle=playSong.getTitle();

        //get id
        String currSong = playSong.getID();
        //set uri
        Uri trackUri = Uri.parse(currSong);

        try{
            player.setDataSource(getApplicationContext(), trackUri);
        }
        catch(Exception e){
            Log.e("MUSIC SERVICE", "Error setting data source", e);
        }

        player.prepareAsync();
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Song> theSongs){
        songs=theSongs;
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if(player.getCurrentPosition()>0){
            mp.reset();

            if(repeat)
            {
                playRepeat();
            }
            else
            {
                playNext();
            }

        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {

        // Broadcast intent to activity to let it know the media player has been prepared
        Intent onPreparedIntent = new Intent("MEDIA_PLAYER_PREPARED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(onPreparedIntent);

        //start playback
        mp.start();

        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendInt = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //Notification.Builder builder = new Notification.Builder(this);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.quaver2)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);

        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);

    }
}
