package grace.controller.request

import grace.util.FileUtil

import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 文件处理
 * 包括 asset 处理
 */
trait FileRender extends RequestBase {
    static int ONE_DAY = 24 * 60 * 60 //second

    /**
     * bytes
     * 无缓存，直接返回
     * @param bytes
     */
    void render(byte[] bytes) {
        response.getOutputStream().write(bytes)
    }

    void render(File file, int cacheTime = ONE_DAY) {
        FileUtil.serveFile(request, response, file, cacheTime)
    }

}