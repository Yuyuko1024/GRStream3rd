package net.hearnsoft.gensokyoradio.trd.utils;

import android.content.Context;
import android.media.AudioManager;

public class AudioSessionManager {


    private Integer audioSessionId;

    private static AudioSessionManager audioSessionManager;

    public static AudioSessionManager getInstance() {
        if (audioSessionManager == null) {
            audioSessionManager = new AudioSessionManager();
        }
        return audioSessionManager;
    }

    public AudioSessionManager() {}

    public void generateAudioSessionId(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioSessionId = manager.generateAudioSessionId();
    }

    public int getAudioSessionId() {
        return audioSessionId;
    }

}
