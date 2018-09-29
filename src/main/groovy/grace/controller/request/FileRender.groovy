package grace.controller.request

import grace.app.GraceApp
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
    void asset(){
        File asset = new File(GraceApp.instance.assetDir,params.file)
        render(asset)
    }

}