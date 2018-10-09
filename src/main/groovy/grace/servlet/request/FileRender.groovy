package grace.servlet.request

import grace.util.FileUtil

/**
 * 文件处理
 * 文件上传，下载，包括 asset 处理
 */
trait FileRender extends Render {
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
     * 文件上传和下载
     * @return
     */
    void upload() {
        try {
            List fileNames = []
            request.parts.each {
                String fileName = FileUtil.fileUUIDName(it.submittedFileName)
                it.write(fileName)
                fileNames << fileName
            }
            render(fileNames.collect { "${app.config.fileUpload.download ?: ''}/$it" })
        } catch (Exception e) {
            render([])
        }
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
}