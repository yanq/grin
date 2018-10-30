package grace.route

import grace.common.WebRequest
import grace.util.ClassUtil
import groovy.util.logging.Slf4j

/**
 * 处理 request
 */
@Slf4j
class Processor {
    /**
     * 处理请求
     * @param uri
     * @param webRequest
     * @return
     */
    static processRequest(String uri, WebRequest webRequest) {
        long start = System.nanoTime()

        Route route = Routes.routes.find { it.matches(uri) }
        if (route) {
            Closure closure = route.closure.clone()
            closure.delegate = webRequest
            closure.setResolveStrategy(Closure.DELEGATE_ONLY)
            webRequest.controllerName = ClassUtil.propertyName(closure.owner.class)

            //路径参数
            def pathParas = route.getPathParams(uri)
            if (pathParas) webRequest.params.putAll(pathParas)

            //before
            boolean pass = before(uri, webRequest)
            if (!pass) return //结束了
            //process
            Object result = closure()
            //after
            after(uri, webRequest) //似乎返回结果也没啥意义
        } else {
            webRequest.notFound()
        }

        log.info("${webRequest.status() != -1 ? webRequest.status() : 200} ${webRequest.remoteIP()} $uri (${route?.path ?: '-'}) , ${(System.nanoTime() - start) / 1000000}ms")
    }

    /**
     * 运行前置拦截器
     * @param uri
     * @param request
     * @return
     */
    private static boolean before(String uri, WebRequest request) {
        def fail = Routes.beforeInterceptors.find {
            if (it.matches(uri)) {
                Closure i = it.closure.clone()
                i.delegate = request
                i.setResolveStrategy(Closure.DELEGATE_ONLY)
                return !i()
            }
        }
       !fail
    }

    /**
     * 运行后置拦截器
     * @param uri
     * @param request
     * @return
     */
    private static boolean after(String uri, WebRequest request) {
        Routes.afterInterceptors.each {
            if (it.matches(uri)) {
                Closure i = it.closure.clone()
                i.delegate = request
                i.setResolveStrategy(Closure.DELEGATE_ONLY)
                i() //each 里面的 return 毫无意义
            }
        }
    }
}
