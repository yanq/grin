package grin.web

import grin.app.App
import groovy.util.logging.Slf4j

import javax.servlet.GenericServlet
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.reflect.Method

@Slf4j
class GServlet extends GenericServlet {
    App app = App.instance

    @Override
    void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        long startAt = System.nanoTime()

        // 设置默认编码
        req.setCharacterEncoding('utf-8')
        res.setCharacterEncoding('utf-8')
        res.setContentType('text/html;charset=UTF-8')

        HttpServletRequest request = (HttpServletRequest) req
        HttpServletResponse response = (HttpServletResponse) res

        // 路由
        String controllerName, actionName
        String clearedURI = toURI(request.requestURI, request.getContextPath())
        Route route = app.routes.find { it.matches(clearedURI) }
        if (!route) {
            log.warn("找不到匹配的路由：${clearedURI}")
            Controller instance = getErrorController()
            instance.init(request, response, null, null, [:])
            instance.notFound()
            return
        }
        Map<String, Object> pathParams = route.getPathParams(clearedURI)
        controllerName = route.controllerName ?: pathParams.get('controllerName')
        actionName = route.actionName ?: pathParams.get('actionName') ?: 'index'


        use(Category.class) {
            try {
                Controller controller
                Method method
                if (app.isDev()) {
                    if (app.controllers.get(controllerName)) {
                        controller = (Controller) app.scriptEngine.loadScriptByName(app.controllers.get(controllerName).replaceAll('\\.', '/') + ".groovy").newInstance()
                        method = controller.class.getDeclaredMethods().find { it.name == actionName }
                    }
                } else {
                    method = app.actions.get("${controllerName}-${actionName}")
                    controller = method?.declaringClass?.newInstance() as Controller
                }
                if (method) {
                    if (!app.interceptor.before(request, response, controllerName, actionName)) return
                    controller.init(request, response, controllerName, actionName, pathParams)
                    method.invoke(controller)
                    app.interceptor.after(request, response, controllerName, actionName)
                } else {
                    log.warn("页面不存在 ${clearedURI}(${controllerName}.${actionName})")
                    Controller instance = getErrorController()
                    instance.init(request, response, controllerName, actionName, pathParams)
                    instance.notFound("请求的地址不存在 ${clearedURI}")
                }
            } catch (Exception e) {
                e.printStackTrace()
                Controller instance = getErrorController()
                instance.init(request, response, controllerName, actionName, pathParams)
                instance.error(e)
            }
        }

        def ip = request.getHeader("X-Real-Ip") ?: request.getRemoteAddr()
        log.info("${response.status} ${ip} ${clearedURI}(${controllerName}.${actionName}) time ${(System.nanoTime() - startAt) / 1000000}ms")
    }

    Controller getErrorController() {
        app.errorControllerClass.newInstance()
    }

    /**
     * 转换成内部的 uri
     * @param requestURI
     * @param context
     * @return
     */
    static String toURI(String requestURI, String context = '') {
        if (context.size() > 1 && requestURI.startsWith(context)) requestURI = requestURI.substring(context.size())
        return clearRequestURI(requestURI) ?: '/'
    }

    /**
     * 整理URI后面的内容
     * @param requestURI
     * @return
     */
    static String clearRequestURI(String requestURI) {
        if (requestURI.indexOf(';') > 0) return requestURI.substring(0, requestURI.indexOf(';'))
        if (requestURI.indexOf('#') > 0) return requestURI.substring(0, requestURI.indexOf('#'))
        if (requestURI.indexOf('?') > 0) return requestURI.substring(0, requestURI.indexOf('?'))
        if (requestURI.endsWith('/')) return requestURI.substring(0, requestURI.length() - 1)
        return requestURI
    }
}
