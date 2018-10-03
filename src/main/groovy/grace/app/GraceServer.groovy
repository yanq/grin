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
        startUndertowServer(buildDeploymentInfo())
    }

    /**
     * 启动 GraceApp
     * todo 对产品部署环境优化
     * 监控目录，即时编译，适用于开发阶段
     * 目录是一个完整的 grace app 结构。
     * @param root
     */
    void startApp(File root, String env = GraceApp.ENV_DEV) {
        GraceApp.setRootAndEnv(root, env)
        def app = GraceApp.instance
        log.info("start app @ ${app.root.absolutePath} ${app.environment}")
        if (app.isAppDir()) {
            app.startFileWatcher()
            app.refresh()
            //config
            if (app.config.server.port) this.port = app.config.server.port
            if (app.config.server.host) this.host = app.config.server.host
            if (app.config.server.context) this.context = app.config.server.context

            def d = buildDeploymentInfo()
            app.init(d) //内部处理
            startUndertowServer(d)
        } else {
            throw new Exception("It is not a grace app dir @ ${GraceApp.instance.root.absolutePath}")
        }
    }

    /**
     * 构建 deploy info
     * @return
     */
    private DeploymentInfo buildDeploymentInfo() {
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(GraceServer.class.getClassLoader())
                .setContextPath(context)
                .setDeploymentName("grace.war")
                .addServlets(Servlets.servlet("GraceServlet", GraceServlet.class).addMapping("/*"))
        return servletBuilder
    }

    /**
     * 启动 server
     * start undertow server
     */
    private void startUndertowServer(DeploymentInfo deploymentInfo) {

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
        manager.deploy()

        Undertow server = Undertow.builder()
                .setIoThreads(2).setWorkerThreads(5)
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
