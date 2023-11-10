package net.hearnsoft.gensokyoradio.trd.service;

import net.hearnsoft.gensokyoradio.trd.beans.NowPlayingBean;

public interface WsServiceInterface {
    void beanReceived(NowPlayingBean bean);
}
