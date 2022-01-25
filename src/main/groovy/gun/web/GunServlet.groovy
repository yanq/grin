package gun.web

import groovy.util.logging.Slf4j
import gun.app.GunApp

import javax.servlet.GenericServlet
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.reflect.Method

@Slf4j
class GunServlet extends GenericServlet {
    GunApp app = GunApp.instance

    @Override
    void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        long startAt = System.nanoTime()

        //设置默认编码
        req.setCharacterEncoding('utf-8')
        res.setCharacterEncoding('utf-8')
        res.setContentType('text/html;charset=UTF-8')

        HttpServletRequest request = (HttpServletRequest) req
        HttpServletResponse response = (HttpServletResponse) res

        String clearedURI = toURI(request.requestURI, request.getContextPath())
        List cai = splitURI(clearedURI)
        String controllerName = cai[0], actionName = cai[1], id = cai[2]

        use(GunCategory.class) {
            try {
                Controller controller
                Method method
                if (app.isDev()) {
                    app.controllers.load(app.controllersDir)
                    if (app.controllers.controllerMap.get(controllerName)) {
                        controller = app.scriptEngine.loadScriptByName(app.controllers.controllerMap.get(controllerName).replaceAll('\\.', '/') + ".groovy").newInstance()
                        method = controller.class.getDeclaredMethod(actionName)
                    }
                } else {
                    method = app.controllers.methodMap.get("${controllerName}-${actionName}")
                    controller = method?.declaringClass?.newInstance()
                }
                if (method) {
                    FlashScope.next(request.getSession(false)?.getId())
                    if (!app.controllers.interceptor.before(request, response, controllerName, actionName, id)) return
                    controller.init(request, response, controllerName, actionName, id)
                    method.invoke(controller)
                    app.controllers.interceptor.after(request, response, controllerName, actionName, id)
                } else {
                    log.warn("页面不存在 ${controllerName}.${actionName}")
                    Controller instance = getErrorController()
                    instance.init(request, response, controllerName, actionName, id)
                    instance.notFound()
                }
            } catch (Exception e) {
                e.printStackTrace()
                Controller instance = getErrorController()
                instance.init(request, response, controllerName, actionName, id)
                instance.error(e)
            }
        }

        def ip = request.getHeader("X-Real-Ip") ?: request.getRemoteAddr()
        log.info("${response.status} ${ip} ${clearedURI}(${cai[0]}.${cai[1]}) time ${(System.nanoTime() - startAt) / 1000000}ms")
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

    /**
     * 从 url 解析出来控制器，操作，id 等内容。
     * @param uri 处理后的 uri
     */
    static splitURI(String uri) {
        String controllerName = 'home' //默认用 home 路径，现在还没有地方定义首页
        String actionName = 'index' // 默认 action
        String id = null
        if (uri) {
            def l = uri.substring(1).split('/')
            if (l.size() > 0 && l[0]) controllerName = l[0]
            if (l.size() > 1 && l[1]) actionName = l[1]
            if (l.size() > 2) id = l[2..-1].join('/')
        }
        return [controllerName, actionName, id]
    }
}
