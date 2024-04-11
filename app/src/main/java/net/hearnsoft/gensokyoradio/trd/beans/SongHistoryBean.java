package net.hearnsoft.gensokyoradio.trd.beans;

public class SongHistoryBean {

    private int songId;
    private String title;
    private String artist;
    private String album;
    private String circle;
    private String coverUrl;
    private int albumId;
    private long timestamp;

    public SongHistoryBean(int songId, String title, String artist, String album, String circle, String coverUrl, int albumId, long timestamp) {
        this.songId = songId;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.circle = circle;
        this.coverUrl = coverUrl;
        this.albumId = albumId;
        this.timestamp = timestamp;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
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

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
