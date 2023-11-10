package net.hearnsoft.gensokyoradio.trd.beans;

public class NowPlayingBean {
    private int songid;
    private String title;
    private String artist;
    private String album;
    private String circle;
    private int duration;
    private String albumart;
    private String year;
    private int played;
    private int remaining;

    public int getSongId() {
        return songid;
    }

    public void setSongId(int songid) {
        this.songid = songid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getCircle() {
        return circle;
    }

    public void setCircle(String circle) {
        this.circle = circle;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getAlbumArt() {
        return albumart;
    }

    public void setAlbumArt(String albumart) {
        this.albumart = albumart;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }
}
