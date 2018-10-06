package grace.controller.request

import grace.app.GraceApp

import javax.servlet.RequestDispatcher
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import java.text.SimpleDateFormat

/**
 * 基本变量
 */
trait RequestBase {
    GraceApp app = GraceApp.instance
    HttpServletRequest request
    HttpServletResponse response
    Params params
    Map<String, String> headers
    @Lazy
    GraceEx g = new GraceEx()

    /**
     * forward
     */
    void forward(String path) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(path);
        dispatcher.forward(request, response);
    }

    /**
     * redirect
     */
    void redirect(String location) throws IOException {
        response.sendRedirect(location);
    }

    /**
     * session
     * @return
     */
    HttpSession getSession() {
        request.getSession(true)
    }

    /**
     * context
     * @return
     */
    ServletContext getContext() {
        request.getServletContext()
    }

    /**
     * headers 延时加载
     * @return
     */
    Map<String, String> getHeaders() {
        if (headers) return headers

        headers = new LinkedHashMap<String, String>();
        for (Enumeration names = request.getHeaderNames(); names.hasMoreElements();) {
            String headerName = (String) names.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.put(headerName, headerValue);
        }

        return headers
    }
    /**
     * 获取参数，延时加载
     * @return
     */
    Params getParams() {
        if (params) return params

        params = new Params();
        for (Enumeration names = request.getParameterNames(); names.hasMoreElements();) {
            String name = (String) names.nextElement();
            if (!params.containsKey(name)) {
                String[] values = request.getParameterValues(name);
                if (values.length == 1) {
                    params.put(name, values[0]);
                } else {
                    params.put(name, values);
                }
            }
        }

        return params;
    }

    /**
     * 参数类，并提供一些方便的转换方法。异常需要自己处理
     */
    static class Params extends HashMap<String, Object> {
        List<String> dateFormats = ['EEE MMM dd HH:mm:ss z yyyy', 'yyyy-MM-dd',"yyyy-MM-dd HH:mm"]
        Locale locale = Locale.ENGLISH

        void addDateFormat(String format) { dateFormats << format }

        Date date(String key, String format = null) {
            def value = super.get(key)
            if (value instanceof Date) return value
            if (value.toString().contains('月')) locale = Locale.SIMPLIFIED_CHINESE
            if (format) {
                return new SimpleDateFormat(format, locale).parse(value.toString())
            }

            def date = null
            dateFormats.each {
                try {
                    date = new SimpleDateFormat(it, locale).parse(value.toString())
                } catch (Exception e) {
                }
                if (date) return date
            }
            return date
        }
    }

    /**
     * 转成 Map ，方便其他地方注入使用，如注入到模板绑定中
     * @return
     */
    Map toMap() {
        return [request: request, response: response, session: session, context: context, params: params, headers: headers, g: g]
    }
}