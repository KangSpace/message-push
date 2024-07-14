package org.kangspace.messagepush.ws.gateway;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kangspace.messagepush.ws.gateway.filter.session.SessionProxyHolder;
import org.kangspace.messagepush.ws.gateway.filter.session.WebSocketUserSessionManager;

/**
 * WebSocketUserSessionManager 大数据量测试
 *
 * @author kango2gler@gmail.com
 * @since 2021/10/28
 */
@RunWith(JUnit4.class)
public class WebSocketUserSessionManagerTest {
    private WebSocketUserSessionManager webSocketUserSessionManager = new WebSocketUserSessionManager();

    /**
     * memory size: 6M
     */
    @Test
    public void add20WTest() {
        int dataSize = 20_0000;
        for (int i = 0; i < dataSize; i++) {
            webSocketUserSessionManager.addSession("uid:" + i, new SessionProxyHolder());
        }
        System.out.println(webSocketUserSessionManager.getUserSessionsMap().size());
        while (true) {
            try {
                Thread.sleep(3_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void initTest() {
    }
}
