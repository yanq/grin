package grace.websocket

import grace.app.GraceApp
import grace.util.ClassUtil
import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j
import javax.websocket.Session

/**
 * WebSocketEntry 信息处理
 */
@Slf4j
class WebSocketHandler {
    static Map<String, Closure> handlers = [:]
    static Closure beforeInterceptor

    /**
     * 定义消息处理
     * @param messageType
     * @param closure
     * @return
     */
    static message(String messageType, @DelegatesTo(WSMessage) Closure closure) {
        if (handlers.containsKey(messageType)) {
            throw new Exception("${messageType} already exist!")
        } else {
            log.info("add a message handler @ ${closure.owner.class.simpleName} ${messageType}")
            handlers.put(messageType, closure)
        }
    }

    /**
     * 定义前置拦截器
     * 这里是唯一的，因为貌似不需要太多
     * @param closure
     * @return
     */
    static before(@DelegatesTo(WSMessage) Closure closure) {
        if (beforeInterceptor) throw new Exception("before already exist!")
        log.info("set message before interceptor @ ${closure.owner.class.simpleName}")
        beforeInterceptor = closure
    }

    /**
     * 处理消息过程
     * @param message
     * @param session
     * @return
     */
    static handleMessage(String message, Session session) {
        def start = System.nanoTime()

        WSMessage msg = new WSMessage(message: message, session: session)
        Closure closure = handlers.get(msg.params.type)

        if (closure) {
            msg.controllerName = ClassUtil.propertyName(closure.owner.class)

            if (beforeInterceptor) {
                Closure c = beforeInterceptor.clone()
                c.delegate = msg
                c.setResolveStrategy(Closure.DELEGATE_FIRST)
                def result = c()
                if (result == false) return 'accessFail'
                if (result instanceof String) return result
            }

            Closure c = closure.clone()
            c.delegate = msg
            c.setResolveStrategy(Closure.DELEGATE_FIRST)
            def result = c()
            log.info("${msg.params.type} , ${(System.nanoTime() - start) / 1000000}ms ")
            return result
        } else {
            log.warn("No message handler found for ${msg.params.type}")
        }
    }

    /**
     * 清理
     * @return
     */
    static clear() {
        handlers.clear()
        beforeInterceptor = null
    }

    /**
     * 代理消息类
     * 并做消息处理
     */
    static class WSMessage {
        String message
        Session session
        String controllerName //控制器简称，方便权限控制时，批量设置
        private Object _data //作缓存用

        Object getParams() {
            if (_data) return _data

            try {
                _data = new JsonSlurper().parseText(message)
            } catch (Exception e) {
                _data = [:]
                log.warn("parse json fail，message : $message")
            }

            return _data
        }

        String toJson(Object obj) {
            GraceApp.instance.jsonGenerator.toJson(obj)
        }
    }
}