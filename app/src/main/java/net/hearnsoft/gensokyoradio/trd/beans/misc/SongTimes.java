package net.hearnsoft.gensokyoradio.trd.beans.misc;

public class SongTimes {

    private int DURATION;

    private int PLAYED;

    private int REMAINING;

    private int SONGSTART;

    private int SONGEND;
    public void setDuration(int duration) {
         this.DURATION = duration;
     }
     public int getDuration() {
         return DURATION;
     }

    public void setPlayed(int played) {
         this.PLAYED = played;
     }
     public int getPlayed() {
         return PLAYED;
     }

    public void setRemaining(int remaining) {
         this.REMAINING = remaining;
     }
     public int getRemaining() {
         return REMAINING;
     }

    public void setSongStart(int songstart) {
         this.SONGSTART = songstart;
     }
     public int getSongStart() {
         return SONGSTART;
     }

    public void setSongEnd(int songend) {
         this.SONGEND = songend;
     }
     public int getSongEnd() {
         return SONGEND;
     }

}