package net.hearnsoft.gensokyoradio.trd.utils;

public class Constants {

    public static final String WS_URL = "wss://gensokyoradio.net/wss";

    public static final String WS_SESSION_MSG = "{\"message\":\"grInitialConnection\"}";

    public static final String NOW_PLAYING_JSON = "https://gensokyoradio.net/api/station/playing/";

    public static final String GR_STREAM_URL_DEFAULT = "https://stream.gensokyoradio.net/1/";

    public static final String GR_STREAM_URL_MOBILE = "https://stream.gensokyoradio.net/2";
    public static final String GR_STREAM_URL_ENHANCED = "https://stream.gensokyoradio.net/3";

    public static final String GR_LOGIN_API_URL = "https://gensokyoradio.net/api/login/";
    public static final String GR_REGISTER_URL = "https://gensokyoradio.net/register";
    public static final String GR_FORGOT_PASSWORD_URL = "https://gensokyoradio.net/account/recover";

    public static final String PREF_GLOBAL_NAME = "gr_3rd";
    public static final String PREF_USERNAME_KEY = "username";
    public static final String PREF_USERID_KEY = "userid";
    public static final String PREF_APPSESSIONID_KEY = "appsessionid";
    public static final String PREF_API_KEY = "api";

    public static final String PREF_CLIENT_ID = "clientId";
    public static final String PREF_VISUALIZER = "visualizer";
    public static final String PREF_SERVER = "server";
    public static final String PREF_SHOWED_NOTICE_DIALOG = "showed_notice_dialog";
}
