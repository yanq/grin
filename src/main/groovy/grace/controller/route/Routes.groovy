package grace.controller.route

import grace.controller.request.WebRequest
import grace.controller.route.Interceptor
import grace.controller.route.Route
import grace.util.ClassUtil
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
    static List<Interceptor> beforeInterceptors = [] //前置拦截器
    static List<Interceptor> afterInterceptors = [] //后置拦截器

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

    //interceptors
    static before(@DelegatesTo(WebRequest) Closure<Boolean> closure) { before('/' + ClassUtil.propertyName(closure.owner.class) + '/**', closure) }

    static before(String path, @DelegatesTo(WebRequest) Closure<Boolean> closure) { before(path, Interceptor.ORDER_NORMAL, closure) }

    static before(String path, int order, @DelegatesTo(WebRequest) Closure<Boolean> closure) { addInterceptor(path, order, closure, true) }

    static after(@DelegatesTo(WebRequest) Closure<Boolean> closure) { after('/' + ClassUtil.propertyName(closure.owner.class) + '/**', closure) }

    static after(String path, @DelegatesTo(WebRequest) Closure<Boolean> closure) { after(path, Interceptor.ORDER_NORMAL, closure) }

    static after(String path, int order, @DelegatesTo(WebRequest) Closure<Boolean> closure) { addInterceptor(path, order, closure, false) }

    //asset
    static assets(String path){get("$path/@file"){asset()}}

    /**
     * 添加到路由表
     * 如果重复，会异常爆出，方便随时发现问题
     * @param path
     * @param closure
     * @return
     */
    private static addRoute(String method, String path, Closure closure) {
        Class ownerClass = closure.owner.class
        if (!path.startsWith('/')) path = "/${ClassUtil.propertyName(ownerClass)}/$path"

        if (routes.find { it.path == path }) {
            log.error("route path {$path} already exists !")
            throw new Exception("route path {$path} already exists !")
        } else {
            log.info("add a route @ ${ownerClass.simpleName} $method $path")
            routes.add(new Route(method: method, path: path, closure: closure))
        }
    }

    /**
     * 添加拦截器
     * @param path
     * @param order
     * @param closure
     * @param before
     * @return
     */
    private static addInterceptor(String path, int order, Closure closure, boolean before) {
        Class ownerClass = closure.owner.class
        if (!path.startsWith('/')) path = "/${ClassUtil.propertyName(ownerClass)}${path ? '/' + path : '/**'}"

        List<Interceptor> target = before ? beforeInterceptors : afterInterceptors

        if (target.find { it.path == path }) {
            log.error("interceptor path {$path} already exists !")
            throw new Exception("interceptor path {$path} already exists !")
        } else {
            target.add(new Interceptor(path: path, order: order, closure: closure))
            log.info("add a interceptor @ ${ownerClass.simpleName} $path")
        }
    }

    /**
     * 清空
     * @return
     */
    static clear() {
        routes.clear()
        beforeInterceptors.clear()
        afterInterceptors.clear()
    }

    /**
     * 排序
     */
    static sort() {
        //简单排序，确切的靠前
        routes.sort {
            String path = it.path
            if (path.count('*')) return 'zz' * path.count('*') + path
            if (path.count('@')) return 'z' * path.count('@') + path
            return path
        }
        beforeInterceptors.sort { it.order }
        afterInterceptors.sort { it.order }
    }
}
