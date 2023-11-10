package net.hearnsoft.gensokyoradio.trd.beans;

import net.hearnsoft.gensokyoradio.trd.beans.misc.Misc;
import net.hearnsoft.gensokyoradio.trd.beans.misc.ServerInfo;
import net.hearnsoft.gensokyoradio.trd.beans.misc.SongData;
import net.hearnsoft.gensokyoradio.trd.beans.misc.SongInfo;
import net.hearnsoft.gensokyoradio.trd.beans.misc.SongTimes;

public class SongDataBean {

    private ServerInfo SERVERINFO;

    private SongInfo SONGINFO;

    private SongTimes SONGTIMES;

    private SongData SONGDATA;

    private Misc MISC;
    public void setServerInfo(ServerInfo serverinfo) {
         this.SERVERINFO = serverinfo;
     }
     public ServerInfo getServerInfo() {
         return SERVERINFO;
     }

    public void setSongInfo(SongInfo songinfo) {
         this.SONGINFO = songinfo;
     }
     public SongInfo getSongInfo() {
         return SONGINFO;
     }

    public void setSongTimes(SongTimes songtimes) {
         this.SONGTIMES = songtimes;
     }
     public SongTimes getSongTimes() {
         return SONGTIMES;
     }

    public void setSongData(SongData songdata) {
         this.SONGDATA = songdata;
     }
     public SongData getSongData() {
         return SONGDATA;
     }

    public void setMisc(Misc misc) {
         this.MISC = misc;
     }
     public Misc getMisc() {
         return MISC;
     }

}