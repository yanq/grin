package grin.web

import grin.app.App
import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 拦截器
 */
@Slf4j
class Interceptor {
    def statusCodeMessages = [
            400: '请求有错误，请检查访问的地址是否正确',
            401: '需要登录后访问此页面',
            403: '此地址已被禁止访问',
            404: '页面不存在',
            405: '请求方法错误',
            422: '数据验证错误，请检查提交的数据',
            500: '服务器内部错误，请稍后再试'
    ]

    boolean before(HttpServletRequest request, HttpServletResponse response, String controllerName, String actionName) {
        return true
    }

    boolean after(HttpServletRequest request, HttpServletResponse response, String controllerName, String actionName) {
        return true
    }

    /**
     * 处理异常
     */
    void dealException(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        log.warn("Exception: ${exception.getMessage()}")
        int status = (exception instanceof HttpException) ? exception.status : 500
        String message = exception.message ?: statusCodeMessages[status] ?: '服务器出现错误，轻稍后再试'
        if (status >= 500) exception.printStackTrace()
        response.status = status
        def accept = request.getHeader('Accept')
        App app = App.instance
        if (accept.contains('json')) {
            app.getJson(response)([success: false, message: message])
        } else {
            String view = status == 404 ? app.config.views.notFound : app.config.views.error
            app.template.render(request, response, view, [exception: exception, message: message])
        }
    }
}