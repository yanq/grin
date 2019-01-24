package grace.websocket

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import javax.websocket.Session

/**
 * WebSocketEntry 信息处理
 */
@Slf4j
class WebSocketHandler {
    static Map<String, Closure> handlers = [:]

    static message(String messageType, @DelegatesTo(WSMessage) Closure closure) {
        if (handlers.containsKey(messageType)) {
            throw new Exception("${messageType} already exist!")
        } else {
            log.info("add a ws message hander for ${messageType}")
            handlers.put(messageType, closure)
        }
    }

    static handleMessage(String message, Session session) {
        def start = System.nanoTime()
        WSMessage msg = new WSMessage(message: message, session: session)
        Closure closure = handlers.get(msg.params.type)
        if (closure) {
            Closure c = closure.clone()
            c.delegate = msg
            c.setResolveStrategy(Closure.DELEGATE_ONLY)
            def result = c()
            log.info("${msg.params.type} , ${(System.nanoTime() - start) / 1000000}ms ")
            return result
        } else {
            log.warn("Not found handler for ${msg.params.type}")
        }
    }

    static clear() {
        handlers.clear()
    }

    static class WSMessage {
        String message
        Session session
        Object _data

        Object getParams() {
            if (_data) return _data
            _data = new JsonSlurper().parseText(message)
            return _data
        }
    }
}