package gun.app

import groovy.util.logging.Slf4j
import gun.web.GunServlet
import io.undertow.Undertow
import io.undertow.servlet.Servlets
import io.undertow.servlet.api.DeploymentInfo
import io.undertow.servlet.api.DeploymentManager

import javax.servlet.MultipartConfigElement

/**
 * Server
 * 启动服务器，使用 undertow servlet 容器。
 */
@Slf4j
class GunServer {
    String host = 'localhost'
    String context = '/'
    int port = 8080
    //fileUpload,默认大小不限制，磁盘存储
    String location = ''
    long maxFileSize = -1L
    long maxRequestSize = -1L
    int fileSizeThreshold = 0
    // 规模
    int ioThreads = 2
    int workerThreads = 5

    /**
     * 启动 Server
     */
    void start() {
        // WebSocketDeploymentInfo webSockets = new WebSocketDeploymentInfo()
        // webSockets.addEndpoint(WebSocketEntry)
        DeploymentInfo deploymentInfo = Servlets.deployment()
                .setClassLoader(GunServer.class.getClassLoader())
                .setDefaultMultipartConfig(new MultipartConfigElement(location, maxFileSize, maxRequestSize, fileSizeThreshold))
                .setTempDir(File.createTempDir()) //这里上传文件的时候，如果 location 空，会用到。但设置了 location，这里就必须设置。
                .setContextPath(context)
                .setDeploymentName("gun.war")
                .addServlets(Servlets.servlet("GunServlet", GunServlet.class).addMapping("/*"))
        // .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSockets)
        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        manager.deploy()

        Undertow server = Undertow.builder()
                .setIoThreads(ioThreads).setWorkerThreads(workerThreads)
                .addHttpListener(port, host)
                .setHandler(manager.start())
                .build()
        server.start()

        log.info("start server @ http://${host}:${port}${context}")
    }
}
