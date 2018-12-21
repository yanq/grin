package grace.servlet

import grace.app.GraceApp
import grace.common.FlashScope
import grace.common.GraceExpression
import grace.common.Params
import grace.common.WebRequest
import grace.util.FileUtil
import groovy.json.StreamingJsonBuilder
import groovy.util.logging.Slf4j
import groovy.xml.MarkupBuilder
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.templateresolver.FileTemplateResolver
import javax.servlet.RequestDispatcher
import javax.servlet.ServletContext
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

/**
 * web 请求
 * 包装请求，提供方便变量和方法使用数据
 */
@Slf4j
class GraceServletRequest extends WebRequest {
    static int ONE_DAY = 24 * 60 * 60 //second
    static TemplateEngine templateEngine //静态全局变量，thymeleaf 模板引擎
    //servlet
    HttpServletRequest request
    HttpServletResponse response
    Params params
    Map<String, String> headers
    //app
    GraceApp app = GraceApp.instance
    @Lazy
    GraceExpression g = new GraceExpression()
    //html json
    MarkupBuilder html
    StreamingJsonBuilder json
    //some share value
    String controllerName = '' //当前控制器

    /**
     * 构造函数，初始化
     * @param request
     * @param response
     */
    GraceServletRequest(HttpServletRequest request, HttpServletResponse response) {
        this.request = request
        this.response = response
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
     * flash
     * @return
     */
    @Override
    FlashScope.Flash getFlash() {
        FlashScope.getFlash(getSession().id)
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
     * 转成 Map ，方便其他地方注入使用，如注入到模板绑定中
     * @return
     */
    Map toMap() {
        return [app: app, request: request, response: response, session: session, flash: flash, context: context, params: params, headers: headers, g: g]
    }

    /**
     * 获取客户端 IP 地址
     * @return
     */
    @Override
    def remoteIP() {
        request.getHeader("X-Real-Ip") ?: request.getRemoteAddr()
    }

    /**
     * 获取响应状态码
     * @return
     */
    @Override
    int status() {
        response.status
    }

    @Override
    void sendMessage(int status, String message) {
        response.setStatus(status)
        render(message)
    }
/**
 * 不存在页面
 */
    void notFound() {
        response.status = 404

        if (app.config.views.notFound) {
            render(app.config.views.notFound, [:])
        } else {
            response.writer.write("No page found for ${request.requestURI}")
        }
    }

    /**
     * 错误页面处理
     * @param e
     */
    @Override
    void error(Exception e) {
        response.status = 500

        if (app.config.views.error) {
            render(app.config.views.error, [exception: e])
        } else {
            response.writer.write("Error: ${e.getMessage()}")
        }
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
        Context ctx = new Context()
        model.putAll(toMap())
        ctx.setVariables(model)

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
        FileUtil.serveFile(request, response, file, cacheTime)
    }

    /**
     * asset
     * 需要路由定义配合 /asset/@file
     */
    void asset() {
        File assetFile
        if (app.isDev()) {
            assetFile = new File(app.assetDir, params.file)
        } else {
            assetFile = new File(app.assetBuildDir, params.file)
        }

        render(assetFile)
    }

    /**
     * 文件上传
     * @return
     */
    List upload() {
        List fileNames = []
        request.parts.each {
            String fileName = FileUtil.fileUUIDName(it.submittedFileName)
            it.write(fileName)
            fileNames << fileName
        }
        fileNames.collect { "${app.config.fileUpload.download ?: ''}/$it" }
    }

    /**
     * 文件下载
     */
    void download() {
        if (!params.file) {
            notFound()
        } else {
            render(new File("${app.config.fileUpload.location ?: ''}", params.file))
        }
    }

    /**
     * 站点静态文件
     */
    void files() {
        if (!params.file) {
            notFound()
        } else {
            render(new File(app.staticDir, params.file))
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

    /**
     * json builder
     * @return
     */
    StreamingJsonBuilder getJson() {
        response.setHeader("Content-Type", "application/json;charset=UTF-8")
        if (json) return json
        json = new StreamingJsonBuilder(response.getWriter())
        return json
    }

    /**
     * html builder
     * @return
     */
    MarkupBuilder getHtml() {
        if (html) return html
        html = new MarkupBuilder(response.getWriter())
        return html
    }
}
