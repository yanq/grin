package grace.websocket

import groovy.util.logging.Slf4j

import javax.websocket.*
import javax.websocket.server.ServerEndpoint

/**
 * WebSocketEntry
 * 入口，也作示例
 * 如果这个不适合，可以在项目初始化时，替换掉。
 */
@Slf4j
@ServerEndpoint("/ws")
class WebSocketEntry {
    @OnOpen
    public void onOpen(Session session) {
        log.info("Connected " + session.getId());
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        def result
        try {
            result = WebSocketHandler.handleMessage(message, session)
        } catch (Exception e) {
            e.printStackTrace()
            return 'serverError'
        }

        if (result instanceof String) return result
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        log.info(String.format("Session %s closed because of %s", session.getId(), closeReason));
    }
}
