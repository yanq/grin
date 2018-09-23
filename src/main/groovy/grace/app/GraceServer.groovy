package grace.app

import grace.servlet.GraceServlet
import groovy.util.logging.Slf4j
import io.undertow.Undertow
import io.undertow.servlet.Servlets
import io.undertow.servlet.api.DeploymentInfo
import io.undertow.servlet.api.DeploymentManager

/**
 * Server
 * 启动服务器
 */
@Slf4j
class GraceServer {
    String host = 'localhost'
    String context = '/'
    int port = 8080

    /**
     * 最简化启动，只启动部署了 GraceServlet 的 Undertow server。
     * 用于但 controller 文件启动。
     */
    void start() {
        startUndertowServer()
    }

    /**
     * 启动 GraceApp ,dev
     * 监控目录，即时编译，适用于开发阶段
     * 目录是一个完整的 grace app 结构。
     * @param root
     */
    void startApp(File root) {
        GraceApp.setRoot(root)
        def app = GraceApp.instance
        log.info("start app @ ${app.root.absolutePath}")
        if (app.isAppDir()) {
            app.startFileWatcher()
            app.refresh()
            //config
            if (app.config.server.port) this.port = app.config.server.port
            if (app.config.server.host) this.host = app.config.server.host
            if (app.config.server.context) this.context = app.config.server.context
            startUndertowServer()
        } else {
            throw new Exception("It is not a grace app dir @ ${GraceApp.instance.root.absolutePath}")
        }
    }

    /**
     * 启动 GraceApp ，prod
     * 产品部署,优化的性能
     */
    void startDeploy() {
        //todo 实现
        print('coming soon'.center(30,'-'))
    }

    /**
     * start undertow server
     */
    private void startUndertowServer() {
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(GraceServer.class.getClassLoader())
                .setContextPath(context)
                .setDeploymentName("grace.war")
                .addServlets(Servlets.servlet("GraceServlet", GraceServlet.class).addMapping("/*"))

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy()

        Undertow server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(manager.start())
                .build()
        server.start()

        log.info("start server @ http://${host}:${port}${context}")
    }

    /**
     * main
     * @param args
     */
    public static void main(String[] args) {
        File root
        if (args) root = new File(args[0])

        new GraceServer().startApp(root)
    }
}
