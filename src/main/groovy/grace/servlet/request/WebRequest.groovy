package grace.servlet.request

import grace.common.Request
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * web 请求
 * 包装请求，提供方便变量和方法使用数据
 */
@Slf4j
@CompileStatic
class WebRequest extends Request implements JSON, HTML, Render, FileRender, ThymeleafRender {
    @Override
    def remoteIP() {
        request.getHeader("X-Real-Ip") ?: request.getRemoteAddr()
    }

    @Override
    int status() {
        return response.status
    }

    @Override
    void error(Exception e) {
        response.status = 500
        response.writer.write("Error: ${e.getMessage()}")
    }
}
