package grace.servlet

import grace.app.GraceApp
import grace.controller.route.Processor
import grace.servlet.request.WebRequest
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
        //等待刷新，如果系统在刷新中
        GraceApp.instance.waitingForRefresh()

        //设置默认编码
        req.setCharacterEncoding('utf-8')
        res.setCharacterEncoding('utf-8')
        res.setContentType('text/html;charset=UTF-8')

        HttpServletRequest request = (HttpServletRequest) req
        HttpServletResponse response = (HttpServletResponse) res

        String clearedURI = RegexUtil.toURI(request.requestURI, request.getContextPath())
        WebRequest webRequest = new WebRequest(request: request, response: response)

        use(GraceCategory.class) {
            try {
                Processor.processRequest(clearedURI, webRequest)
            } catch (Exception e) {
                e.printStackTrace()
                if (app.config.views.error) {
                    response.status = 500
                    webRequest.render(app.config.views.error, [exception: e])
                } else {
                    webRequest.error(e)
                }
            }
        }
    }
}
