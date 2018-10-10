package grace.controller.route

import grace.common.Request
import grace.util.ClassUtil
import groovy.util.logging.Slf4j

/**
 * 处理 request
 */
@Slf4j
class RouteUtil {
    /**
     * 处理请求
     * @param uri
     * @param request
     * @return
     */
    static processRequest(String uri, Request request) {
        long start = System.nanoTime()

        Route route = Routes.routes.find { it.matches(uri) }
        if (route) {
            Closure closure = route.closure.clone()
            closure.delegate = request
            closure.setResolveStrategy(Closure.DELEGATE_ONLY)
            request.controllerName = ClassUtil.propertyName(closure.owner.class)

            //路径参数
            def pathParas = route.getPathParams(uri)
            if (pathParas) request.params.putAll(pathParas)

            //before
            boolean pass = before(uri, request)
            if (!pass) return //结束了
            //process
            Object result = closure()
            //after
            after(uri, request) //似乎返回结果也没啥意义
        } else {
            request.notFound()
        }

        log.info("${request.status() != -1 ? request.status() : 200} $uri (${route?.path ?: '-'}) , ${(System.nanoTime() - start) / 1000000}ms")
    }

    /**
     * 运行前置拦截器
     * @param uri
     * @param request
     * @return
     */
    private static boolean before(String uri, Request request) {
        Routes.beforeInterceptors.each {
            if (it.matches(uri)) {
                Closure i = it.closure.clone()
                i.delegate = request
                i.setResolveStrategy(Closure.DELEGATE_ONLY)
                boolean r = i()
                if (r == false) return false
                return true
            }
        }
    }

    /**
     * 运行后置拦截器
     * @param uri
     * @param request
     * @return
     */
    private static boolean after(String uri, Request request) {
        Routes.afterInterceptors.each {
            if (it.matches(uri)) {
                Closure i = it.closure.clone()
                i.delegate = request
                i.setResolveStrategy(Closure.DELEGATE_ONLY)
                return i()
            }
        }
    }
}
