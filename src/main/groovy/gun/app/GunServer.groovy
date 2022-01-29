package gun.app

import groovy.util.logging.Slf4j
import gun.web.GunServlet
import io.undertow.Undertow
import io.undertow.servlet.Servlets
import io.undertow.servlet.api.DeploymentInfo
import io.undertow.servlet.api.DeploymentManager

import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.servlet.MultipartConfigElement
import java.security.KeyStore

/**
 * Server
 * 启动服务器，使用 undertow servlet 容器。
 */
@Slf4j
class GunServer {
    String host
    int port
    int httpsPort
    String jksPath
    String jksPwd
    String context
    String uploadLocation
    long maxFileSize
    long maxRequestSize
    int fileSizeThreshold
    int ioThreads
    int workerThreads

    /**
     * 启动 Server
     */
    void start() {
        // WebSocketDeploymentInfo webSockets = new WebSocketDeploymentInfo()
        // webSockets.addEndpoint(WebSocketEntry)
        DeploymentInfo deploymentInfo = Servlets.deployment()
                .setClassLoader(GunServer.class.getClassLoader())
                .setDefaultMultipartConfig(new MultipartConfigElement(uploadLocation, maxFileSize, maxRequestSize, fileSizeThreshold))
                .setTempDir(File.createTempDir()) //这里上传文件的时候，如果 location 空，会用到。但设置了 location，这里就必须设置。
                .setContextPath(context)
                .setDeploymentName("gun.war")
                .addServlets(Servlets.servlet("GunServlet", GunServlet.class).addMapping("/*"))
        // .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME, webSockets)
        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        manager.deploy()

        def buider = Undertow.builder()
        buider.setIoThreads(ioThreads).setWorkerThreads(workerThreads)
        if (port != -1) buider.addHttpListener(port, host)
        if (httpsPort != -1) buider.addHttpsListener(httpsPort, host, buildSSL(jksPath, jksPwd))
        buider.setHandler(manager.start())
        Undertow server = buider.build()
        server.start()

        if (port != -1) log.info("start server @ http://${host}:${port}${context}")
        if (httpsPort != -1) log.info("start server @ https://${host}:${httpsPort}${context}")
    }

    SSLContext buildSSL(String jks, String pwd) {
        KeyStore serverKeyStore = KeyStore.getInstance("JKS")
        serverKeyStore.load(new FileInputStream(jks), pwd.toCharArray())
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509")
        kmf.init(serverKeyStore, pwd.toCharArray());//加载密钥储存器
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(serverKeyStore)
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext
    }
}
