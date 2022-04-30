package gun.web

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 拦截器
 */
class Interceptor {

    boolean before(HttpServletRequest request, HttpServletResponse response, String controllerName, String actionName) {
        return true
    }

    boolean after(HttpServletRequest request, HttpServletResponse response, String controllerName, String actionName) {
        return true
    }

}