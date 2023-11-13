package grin.web

import grin.app.App
import groovy.json.JsonSlurper
import groovy.json.StreamingJsonBuilder
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.context.WebContext
import org.thymeleaf.templateresolver.FileTemplateResolver

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
    static TemplateEngine templateEngine // 静态全局变量，thymeleaf 模板引擎
    // servlet
    HttpServletRequest request
    HttpServletResponse response
    Map<String, Object> pathParams
    Params params
    // app
    App app = App.instance
    // html json
    MarkupBuilder html
    StreamingJsonBuilder json

    // 控制器三大要素
    String controllerName
    String actionName

    /**
     * 初始化数据
     */
    void init(HttpServletRequest request, HttpServletResponse response, String controllerName, String actionName, Map<String, Object> pathParams) {
        this.request = request
        this.response = response
        this.controllerName = controllerName
        this.actionName = actionName
        this.pathParams = pathParams
    }

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
     * 获取参数，延时加载
     * @return
     */
    Params getParams() {
        if (params) return params

        params = new Params();
        if (pathParams) params.putAll(pathParams)
        for (Enumeration names = request.getParameterNames(); names.hasMoreElements();) {
            String name = (String) names.nextElement();
            String[] values = request.getParameterValues(name);
            if (values.length == 1) {
                params.put(name, values[0]);
            } else {
                params.put(name, values);
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

        return params;
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
        WebContext ctx = new WebContext(request, response, context, request.getLocale())
        Map map = [app    : app, controllerName: controllerName, actionName: actionName,
                   context: context, request: request, response: response, session: session,  params: params]
        map.putAll(model)
        ctx.setVariables(map)
        String path = view.startsWith('/') ? view : "/${controllerName}/${view}"
        templateEngine().process(path, ctx, response.getWriter())
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
    StreamingJsonBuilder getJson() {
        response.setHeader("Content-Type", "application/json;charset=UTF-8")
        if (json) return json
        json = new StreamingJsonBuilder(response.getWriter(), app.jsonGenerator)
        return json
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
     * @return
     */
    MarkupBuilder getHtml() {
        if (html) return html
        html = new MarkupBuilder(response.getWriter())
        return html
    }

    /**
     * 不存在页面
     */
    void notFound(String message = "请求的内容不存在") {
        response.status = 404

        if (app.config.views.notFound) {
            render(app.config.views.notFound, [message: message])
        } else {
            response.writer.write(message)
        }
    }

    /**
     * 错误页面处理
     * @param e
     */
    void error(Exception e) {
        response.status = 500

        if (app.config.views.error) {
            render(app.config.views.error, [exception: e])
        } else {
            response.writer.write("Error: ${e.getMessage()}")
        }
    }

    /**
     * template engine by thymeleaf
     * 默认是缓存的。
     * @return
     */
    TemplateEngine templateEngine() {
        if (templateEngine) return templateEngine
        templateEngine = new TemplateEngine()
        FileTemplateResolver resolver = new FileTemplateResolver()
        resolver.setPrefix(app.viewsDir.canonicalPath)
        resolver.setSuffix('.html')
        resolver.setCharacterEncoding('utf-8')
        resolver.setCacheable(!app.isDev())
        templateEngine.setTemplateResolver(resolver)
        return templateEngine
    }
}
