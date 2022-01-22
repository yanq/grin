package grace.servlet

import grace.app.GraceApp
import grace.common.WebRequest
import grace.controller.Controller
import grace.route.Processor
import grace.util.GraceUtils
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
        long startAt = System.nanoTime()

        //等待刷新，如果系统在刷新中
        GraceApp app = GraceApp.instance
        app.waitingForRefresh()

        //设置默认编码
        req.setCharacterEncoding('utf-8')
        res.setCharacterEncoding('utf-8')
        res.setContentType('text/html;charset=UTF-8')

        HttpServletRequest request = (HttpServletRequest) req
        HttpServletResponse response = (HttpServletResponse) res

        String clearedURI = GraceUtils.toURI(request.requestURI, request.getContextPath())
        List params = GraceUtils.splitURI(clearedURI)

        use(GraceCategory.class) {
            try {
                app.controllers.executeAction(request, response, params[0], params[1], params[2])
            } catch (Exception e) {
                new Controller(request, response).error(e)
                e.printStackTrace()
            }
        }

        log.info("time ${(System.nanoTime() - startAt) / 1000000}ms")
    }
}
