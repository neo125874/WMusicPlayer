package music.te.com.wmusicplayer;

/**
 * Created by tw4585 on 2015/4/23.
 */
public class Song {
    private String id;
    private String title;
    private String artist;

    public Song(String songID, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }

    public String getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
}
