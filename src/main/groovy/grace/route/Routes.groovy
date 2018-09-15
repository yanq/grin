package grace.route

import grace.controller.WebRequest
import groovy.util.logging.Slf4j

/**
 * 路由工具类
 * 提供方便的方法定义路由
 */
@Slf4j
class Routes {
    static final METHOD_ALL = '*'
    static final String METHOD_DELETE = "DELETE"
    static final String METHOD_HEAD = "HEAD"
    static final String METHOD_GET = "GET"
    static final String METHOD_OPTIONS = "OPTIONS"
    static final String METHOD_POST = "POST"
    static final String METHOD_PUT = "PUT"
    static final String METHOD_TRACE = "TRACE"

    static List<Route> routes = [] //路由列表

    //all method
    static req(String path, @DelegatesTo(WebRequest) Closure closure) {
        addRoute(METHOD_ALL, path, closure)
    }

    //get
    static get(String path, @DelegatesTo(WebRequest) Closure closure) {
        addRoute(METHOD_GET, path, closure)
    }

    //post
    static post(String path, @DelegatesTo(WebRequest) Closure closure) {
        addRoute(METHOD_POST, path, closure)
    }

    //put
    static put(String path, @DelegatesTo(WebRequest) Closure closure) {
        addRoute(METHOD_PUT, path, closure)
    }

    //delete
    static delete(String path, @DelegatesTo(WebRequest) Closure closure) {
        addRoute(METHOD_DELETE, path, closure)
    }

    /**
     * 添加到路由表
     * 如果重复，会异常爆出，方便随时发现问题
     * @param path
     * @param closure
     * @return
     */
    private static addRoute(String method, String path, Closure closure) {
        if (routes.find { it.path == path }) {
            log.error("path {$path} already exists !")
            throw new Exception("path {$path} already exists !")
        } else {
            log.info("add a route @ $method $path")
            routes.add(new Route(method: method, path: path, closure: closure))
        }
    }
}
