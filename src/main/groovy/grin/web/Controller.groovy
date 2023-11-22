package grin.web

import grin.app.App
import groovy.json.JsonSlurper
import groovy.json.StreamingJsonBuilder
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder

import javax.servlet.RequestDispatcher
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

/**
 * 控制器基类
 * 包装请求，提供方便变量和方法使用数据
 */
@Slf4j
class Controller {
    static int ONE_DAY = 24 * 60 * 60 // second
    // servlet
    HttpServletRequest request
    HttpServletResponse response
    Map<String, Object> pathParams
    Params params

    // app
    App app = App.instance
    // json
    private StreamingJsonBuilder _json

    // 控制器三大要素
    String controllerName
    String actionName
    String id

    /**
     * 初始化数据
     */
    void init(HttpServletRequest request, HttpServletResponse response, String controllerName, String actionName, String id, Map<String, Object> pathParams) {
        this.request = request
        this.response = response
        this.controllerName = controllerName
        this.actionName = actionName
        this.id = id
        this.pathParams = pathParams
    }

    /**
     * forward
     */
    void forward(String path) throws ServletException, IOException {
        RequestDispatcher dispatcher = request.getRequestDispatcher(path)
        dispatcher.forward(request, response)
    }

    /**
     * redirect
     */
    void redirect(String location) throws IOException {
        response.sendRedirect(location)
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
     * 获取参数，延时加载
     * @return
     */
    Params getParams() {
        if (params) return params

        params = new Params()
        if (id) params.put('id', id)
        if (pathParams) params.putAll(pathParams)
        for (Enumeration names = request.getParameterNames(); names.hasMoreElements();) {
            String name = (String) names.nextElement()
            String[] values = request.getParameterValues(name)
            if (values.length == 1) {
                params.put(name, values[0])
            } else {
                params.put(name, values)
            }
        }

        // json data
        String contentType = request.getHeader('content-type') ?: request.getHeader('Content-Type')
        if (contentType?.contains('application/json')) {
            try {
                if (!request.inputStream.isFinished()) {
                    String text = request.inputStream.text
                    if (text) {
                        def data = new JsonSlurper().parseText(text)
                        if (data instanceof Map) {
                            params.putAll(data)
                        } else {
                            params.put('json', data)
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("maybe json data,but throw an exception : ${e.getMessage()}")
                e.printStackTrace()
            }
        }

        return params
    }

    /**
     * headers
     * @return
     */
    Map getHeaders() {
        def headers = [:]
        request.headerNames.each {
            headers[it] = request.getHeader(it)
        }
        return headers
    }

    /**
     * 返回string
     * @param string
     */
    void render(String string) {
        response.getWriter().write(string)
    }

    /**
     * 用以托底，render 任何对象
     * @param o
     */
    void render(Object o) {
        response.getWriter().write(o.toString())
    }

    /**
     * view and model
     * 默认 thymeleaf 渲染
     * @param view
     * @param model
     */
    void render(String view, Map model) {
        app.template.render(this, view, model)
    }

    /**
     * bytes
     * 无缓存，直接返回
     * @param bytes
     */
    void render(byte[] bytes) {
        response.reset()
        response.getOutputStream().write(bytes)
    }

    /**
     * 文件处理
     * 开启了断点续传，缓存等。
     * @param file
     * @param cacheTime
     */
    void render(File file, int cacheTime = ONE_DAY) {
        response.reset()
        FileUtils.serveFile(request, response, file, cacheTime)
    }

    /**
     * json builder
     * @return
     */
    private StreamingJsonBuilder getJson() {
        response.reset()
        if (_json) throw new HttpException(500, "服务器内部错误，不能重复调用 json()")
        _json = app.getJson(response)
        return _json
    }

    // json相关的方法
    void json(Map m) { getJson()(m) }

    void json(String name) { getJson()(name) }

    void json(List l) { getJson()(l) }

    void json(Object... args) { getJson()(args) }

    void json(Iterable coll, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure c) { getJson()(coll, c) }

    void json(Collection coll, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure c) { getJson()(coll, c) }

    void json(@DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure c) { getJson()(c) }

    void json(String name, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure c) { getJson()(name, c) }

    void json(String name, Iterable coll, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure c) { getJson()(name, coll, c) }

    void json(String name, Collection coll, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure c) { getJson()(name, coll, c) }

    void json(String name, Map map, @DelegatesTo(StreamingJsonBuilder.StreamingJsonDelegate.class) Closure callable) { getJson()(name, map, callable) }

    void success(String message, Map data = null) { json(data ? [success: true, message: message, data: data] : [success: true, message: message]) }

    void fail(String message, Map errors = null) { json(errors ? [success: false, message: message, errors: errors] : [success: true, message: message]) }

    /**
     * html builder
     * 这里貌似也该限制只调用一次，但，第一次调用就向客户端提交了数据，抛出异常，无法正常显示异常信息。索性也就不限制了。
     * @return
     */
    MarkupBuilder getHtmlBuilder() {
        new MarkupBuilder(response.getWriter())
    }
}
