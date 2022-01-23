package gun.web


import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class FilesController extends Controller {
    /**
     * 构造函数，初始化
     * @param request
     * @param response
     */
    FilesController(HttpServletRequest request, HttpServletResponse response) {
        super(request, response)
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
            if (it.submittedFileName) {
                String fileName = FileUtil.fileUUIDName(it.submittedFileName)
                it.write(fileName)
                fileNames << fileName
            }
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
}
