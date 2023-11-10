package net.hearnsoft.gensokyoradio.trd.beans.misc;

public class ServerInfo {

    private int LASTUPDATE;

    private int SERVERS;

    private String STATUS;

    private int LISTENERS;

    private Streams STREAMS;

    private String MODE;
    public void setLastUpdate(int lastupdate) {
         this.LASTUPDATE = lastupdate;
     }
     public int getLastUpdate() {
         return LASTUPDATE;
     }

    public void setServers(int servers) {
         this.SERVERS = servers;
     }
     public int getServers() {
         return SERVERS;
     }

    public void setStatus(String status) {
         this.STATUS = status;
     }
     public String getStatus() {
         return STATUS;
     }

    public void setListeners(int listeners) {
         this.LISTENERS = listeners;
     }
     public int getListeners() {
         return LISTENERS;
     }

    public void setStreams(Streams streams) {
         this.STREAMS = streams;
     }
     public Streams getStreams() {
         return STREAMS;
     }

    public void setMode(String mode) {
         this.MODE = mode;
     }
     public String getMode() {
         return MODE;
     }

}