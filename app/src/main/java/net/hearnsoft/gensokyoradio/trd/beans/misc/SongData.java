package net.hearnsoft.gensokyoradio.trd.beans.misc;

public class SongData {

    private int SONGID;

    private int ALBUMID;

    private String RATING;

    private int TIMESRATED;

    public int getSongID() {
        return SONGID;
    }

    public void setSongID(int SONGID) {
        this.SONGID = SONGID;
    }

    public int getAlbumID() {
        return ALBUMID;
    }

    public void setAlbumID(int ALBUMID) {
        this.ALBUMID = ALBUMID;
    }

    public String getRating() {
        return RATING;
    }

    public void setRating(String RATING) {
        this.RATING = RATING;
    }

    public int getTimeSrated() {
        return TIMESRATED;
    }

    public void setTimeSrated(int TIMESRATED) {
        this.TIMESRATED = TIMESRATED;
    }
}