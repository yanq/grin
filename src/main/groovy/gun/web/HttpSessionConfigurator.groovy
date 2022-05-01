package gun.web

import javax.servlet.http.HttpSession
import javax.websocket.HandshakeResponse
import javax.websocket.server.HandshakeRequest
import javax.websocket.server.ServerEndpointConfig

/**
 * 为 WebSocket 提供 http session，以维护统一的会话。
 */
class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        sec.getUserProperties().put(HttpSession.name, request.getHttpSession())
    }
}
