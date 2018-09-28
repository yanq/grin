package grace.servlet

import grace.app.GraceApp
import grace.controller.traits.WebRequest
import grace.controller.route.Route
import grace.controller.route.Routes
import grace.util.ClassUtil
import grace.util.RegexUtil
import groovy.util.logging.Slf4j
import javax.servlet.GenericServlet
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j
class GraceServlet extends GenericServlet {
    @Override
    void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req
        HttpServletResponse response = (HttpServletResponse) res
        String requestURI = request.requestURI

        //等待刷新，如果系统在刷新中
        GraceApp.instance.waitingForRefresh()

        String clearedURI = RegexUtil.toURI(requestURI,request.getContextPath())
        Route route = Routes.routes.find { it.matches(clearedURI) }
        if (route) {
            //设置默认编码
            request.setCharacterEncoding('utf-8')
            response.setCharacterEncoding('utf-8')
            response.setContentType('text/html;charset=UTF-8')

            //初始化闭包
            WebRequest webRequest = new WebRequest(request: request, response: response)
            Closure closure = route.closure.clone()
            closure.delegate = webRequest
            closure.setResolveStrategy(Closure.DELEGATE_ONLY)
            webRequest.controllerName = ClassUtil.propertyName(closure.owner.class)

            //路径参数
            def pathParas = route.getPathParams(clearedURI)
            if (pathParas) webRequest.params.putAll(pathParas)

            //处理
            long start = System.nanoTime()
            Object result
            use(GraceCategory.class) {
                //before
                boolean pass = before(clearedURI, webRequest)
                if (!pass) return //结束了
                //process
                result = closure()
                //after
                after(clearedURI, webRequest) //似乎返回结果也没啥意义
            }

            log.info("$clearedURI ($route.path) , ${(System.nanoTime() - start) / 1000000} ms")
        } else {
            response.setStatus(404)
            res.writer.write("No page found for ${request.requestURI}")
        }
    }

    private boolean before(String uri, WebRequest webRequest) {
        Routes.beforeInterceptors.each {
            if (it.matches(uri)) {
                Closure i = it.closure.clone()
                i.delegate = webRequest
                i.setResolveStrategy(Closure.DELEGATE_ONLY)
                boolean r = i()
                if (r == false) return false
                return true
            }
        }
    }

    private boolean after(String uri, WebRequest webRequest) {
        Routes.afterInterceptors.each {
            if (it.matches(uri)) {
                Closure i = it.closure.clone()
                i.delegate = webRequest
                i.setResolveStrategy(Closure.DELEGATE_ONLY)
                return i()
            }
        }
    }
}
