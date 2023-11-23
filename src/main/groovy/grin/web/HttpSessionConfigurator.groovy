package grin.web


import javax.websocket.HandshakeResponse
import javax.websocket.server.HandshakeRequest
import javax.websocket.server.ServerEndpointConfig

/**
 * 为 WebSocket 提供 handshake request，方便获取信息。
 */
class HttpSessionConfigurator extends ServerEndpointConfig.Configurator {
    @Override
    void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        sec.getUserProperties().put(HandshakeRequest.name, request)
    }
}
