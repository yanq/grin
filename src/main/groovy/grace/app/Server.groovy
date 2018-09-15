package grace.app

import grace.servlet.GraceServlet
import io.undertow.Undertow
import io.undertow.servlet.Servlets
import io.undertow.servlet.api.DeploymentInfo
import io.undertow.servlet.api.DeploymentManager

class Server {
    void start() {
        DeploymentInfo servletBuilder = Servlets.deployment()
                .setClassLoader(Server.class.getClassLoader())
                .setContextPath("/")
                .setDeploymentName("grace.war")
                .addServlets(Servlets.servlet("GraceServlet", GraceServlet.class).addMapping("/*"))

        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy()

        Undertow server = Undertow.builder()
                .addHttpListener(8080, "localhost")
                .setHandler(manager.start())
                .build()
        server.start()
    }

    public static void main(String[] args) {
        new Server().start()
    }
}
