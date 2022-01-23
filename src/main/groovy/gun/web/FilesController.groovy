package gun.web

class FilesController extends Controller {

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
                String fileName = fileUUIDName(it.submittedFileName)
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

    /**
     * 产生一个 uuid 文件名，分析后缀
     * @param fileName
     * @return
     */
    static String fileUUIDName(String fileName) {
        def index = fileName.lastIndexOf('.')
        def postFix = ''
        if (index > 0) postFix = fileName.substring(index + 1)
        def uuid = UUID.randomUUID().toString().replace('-', '')
        return postFix ? "${uuid}.${postFix}" : uuid
    }
}
