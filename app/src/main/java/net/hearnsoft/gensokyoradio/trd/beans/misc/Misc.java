package net.hearnsoft.gensokyoradio.trd.beans.misc;

public class Misc {

    private String CIRCLELINK;

    private String ALBUMART;

    private String CIRCLEART;

    private String OFFSET;

    private int OFFSETTIME;
    public void setCircleLink(String circlelink) {
         this.CIRCLELINK = circlelink;
     }
     public String getCircleLink() {
         return CIRCLELINK;
     }

    public void setAlbumArt(String albumart) {
         this.ALBUMART = albumart;
     }
     public String getAlbumArt() {
         return ALBUMART;
     }

    public void setCircleArt(String circleart) {
         this.CIRCLEART = circleart;
     }
     public String getCircleArt() {
         return CIRCLEART;
     }

    public void setOffset(String offset) {
         this.OFFSET = offset;
     }
     public String getOffset() {
         return OFFSET;
     }

    public void setOffsetTime(int offsettime) {
         this.OFFSETTIME = offsettime;
     }
     public int getOffsetTime() {
         return OFFSETTIME;
     }

}