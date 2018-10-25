package grace.simple

import com.sun.net.httpserver.HttpExchange
import grace.common.Params
import grace.common.WebRequest
import groovy.json.StreamingJsonBuilder
import groovy.xml.MarkupBuilder

/**
 * simple request
 * for http server 's handler
 */
class SimpleRequest extends WebRequest {
    boolean processed = false //自行处理
    HttpExchange exchange
    StreamingJsonBuilder json
    MarkupBuilder html
    Writer writer

    /**
     * 构造函数
     * @param exchange
     */
    SimpleRequest(HttpExchange exchange) {
        this.exchange = exchange
    }

    /**
     * 获取 exchange，自行处理
     * @return
     */
    HttpExchange getExchange() {
        processed = true
        return exchange
    }

    /**
     * writer 延时初始化
     * @return
     */
    Writer getWriter() {
        if (writer) return writer
        writer = new StringWriter()
        return writer
    }

    /**
     * json builder
     * @return
     */
    @Override
    StreamingJsonBuilder getJson() {
        if (json) return json
        json = new StreamingJsonBuilder(getWriter())
        return json
    }

    /**
     * html
     * @return
     */
    @Override
    MarkupBuilder getHtml() {
        if (html) return html
        html = new MarkupBuilder(getWriter())
        return html
    }

    /**
     * params
     * todo 初始化 params
     * @return
     */
    Params getParams() {
        if (params) return params
        params = new Params()
        return params
    }

    /**
     * 404
     */
    @Override
    void notFound() {
        byte[] bytes = "No page found for ${exchange.requestURI.path}".getBytes('utf-8')
        getExchange().sendResponseHeaders(404, bytes.length)
        getExchange().getResponseBody().write(bytes)
        getExchange().getResponseBody().close()
    }

    /**
     * error
     * @param e
     */
    @Override
    void error(Exception e) {
        byte[] bytes = "Error: ${e.getMessage()}".getBytes('utf-8')
        getExchange().sendResponseHeaders(500, bytes.length)
        getExchange().getResponseBody().write(bytes)
        getExchange().getResponseBody().close()
    }

    /**
     * render anything
     * @param object
     */
    @Override
    void render(Object object) {
        getWriter().write(object.toString())
    }

    /**
     * render string
     * @param string
     */
    @Override
    void render(String string) {
        getWriter().write(string)
    }

    /**
     * 获取状态码
     * @return
     */
    @Override
    int status() {
        return exchange.getResponseCode()
    }

    @Override
    def remoteIP() {
        return ''
    }

    //------------------------------------------------------------------------------------------------

    @Override
    void render(String view, Map model) { throw new Exception("没有实现的方法") }

    @Override
    Map toMap() { throw new Exception("没有实现的方法") }

    @Override
    void forward(String path) { throw new Exception("没有实现的方法") }

    @Override
    void redirect(String location) { throw new Exception("没有实现的方法") }

    @Override
    void render(byte[] bytes) { throw new Exception("没有实现的方法") }

    @Override
    void render(File file) { throw new Exception("没有实现的方法") }

    @Override
    void render(File file, int cacheTime) { throw new Exception("没有实现的方法") }

    @Override
    void asset() { throw new Exception("没有实现的方法") }

    @Override
    void upload() { throw new Exception("没有实现的方法") }

    @Override
    void download() { throw new Exception("没有实现的方法") }

    @Override
    void files() { throw new Exception("没有实现的方法") }
}
