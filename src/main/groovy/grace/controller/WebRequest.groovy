package grace.controller

import grace.controller.traits.HTMLSupport
import grace.controller.traits.JSONSupport
import grace.controller.traits.Render
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import javax.servlet.RequestDispatcher
import javax.servlet.ServletException

/**
 * web 请求
 * 包装请求，提供方便变量和方法使用数据
 */
@Slf4j
@CompileStatic
class WebRequest implements JSONSupport, HTMLSupport, Render {

    /**
     * forward
     */
    void forward(String path) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        dispatcher.forward(request, response);
    }

    /**
     * include
     */
    void include(String path) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        dispatcher.include(request, response);
    }

    /**
     * redirect
     */
    void redirect(String location) throws IOException {
        response.sendRedirect(location);
    }

    /**
     * respond string
     */
    void respond(String content) {
        response.getWriter().write(content)
    }

    /**
     * 修复某些方法，如 json(...)
     * todo 还不准确，具体怎么表现，稍后再考虑
     * @param name
     * @param args
     * @return
     */
//    def methodMissing(String name, Object args) {
//       if (name == 'json'){
//           getJson()(args)
//           return
//       }
//        throw new MissingMethodException()
//    }

}
