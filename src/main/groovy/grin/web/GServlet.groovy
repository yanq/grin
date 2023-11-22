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
    App app

    void init() {
        app = App.instance
        app.initializeDB()
        app.initializeWeb()
    }

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
        String clearedURI = clearURI(request.requestURI, request.getContextPath())
        Route route = app.routes.find { it.matches(clearedURI) }
        if (!route) {
            log.warn("找不到匹配的路由：${clearedURI}")
            throw new HttpException(404, "请求的地址不存在 ${clearedURI}")
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
                    controller.init(request, response, controllerName, actionName, route.id, pathParams)
                    method.invoke(controller)
                    app.interceptor.after(request, response, controllerName, actionName)
                } else {
                    log.warn("页面不存在 ${clearedURI}(${controllerName}.${actionName})")
                    throw new HttpException(404, "请求的地址不存在 ${clearedURI}")
                }
            } catch (Exception e) {
                app.interceptor.dealException(req, res, e)
            }
        }

        def ip = request.getHeader("X-Forwarded-For") ?: request.getRemoteAddr()
        log.info("${response.status} ${ip} ${clearedURI}(${controllerName}.${actionName}${route.id ? '.' + route.id : ''}) time ${(System.nanoTime() - startAt) / 1000000}ms")
    }

    /**
     * 转换成内部的 uri
     * @param requestURI
     * @param context
     * @return
     */
    static String clearURI(String requestURI, String context = '') {
        if (context.size() > 1 && requestURI.startsWith(context)) requestURI = requestURI.substring(context.size())
        return requestURI
    }
}
