package grace.controller

import groovy.json.StreamingJsonBuilder
import groovy.transform.CompileStatic
import groovy.xml.MarkupBuilder
import javax.servlet.RequestDispatcher
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import java.text.SimpleDateFormat

/**
 * web 请求
 * 包装请求，提供方便变量和方法使用数据
 */
@CompileStatic
class WebRequest {
    HttpServletRequest request
    HttpServletResponse response
    Params params
    Map<String, String> headers
    MarkupBuilder html
    StreamingJsonBuilder json

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
     * 参数，并提供一些方便的转换方法。异常需要自己处理
     */
    class Params extends HashMap<String, Object> {
        Date date(String key, String format = 'yyyy-MM-dd') {
            def value = super.get(key)
            if (value instanceof Date) return value
            return new SimpleDateFormat(format).parse(value.toString())
        }
    }

    /**
     * html builder
     * @return
     */
    MarkupBuilder getHtml(){
        if (html) return html
        return new MarkupBuilder(response.getWriter())
    }

    /**
     * json builder
     * @return
     */
    StreamingJsonBuilder getJson(){
        if (json) return json
        return new StreamingJsonBuilder(response.getWriter())
    }

// todo do something
//    void json(Object object){
//        getJson()(object)
//    }

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
