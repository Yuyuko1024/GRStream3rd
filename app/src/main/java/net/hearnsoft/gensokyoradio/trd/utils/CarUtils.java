package net.hearnsoft.gensokyoradio.trd.utils;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;

public class CarUtils {

    public static boolean isAutomotiveOS(Context context) {
        UiModeManager uiModeManager =
                (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        return uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_CAR;
    }



}
