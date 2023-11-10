package net.hearnsoft.gensokyoradio.trd.beans.misc;

public class StreamLevel {

    private int BITRATE;

    private int LISTENERS;
    public void setBitRate(int bitrate) {
         this.BITRATE = bitrate;
     }
     public int getBitRate() {
         return BITRATE;
     }

    public void setListeners(int listeners) {
         this.LISTENERS = listeners;
     }
     public int getListeners() {
         return LISTENERS;
     }

}