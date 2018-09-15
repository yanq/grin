package grace.servlet

import com.sun.deploy.net.HttpRequest
import grace.controller.ControllerScript
import grace.route.Route

import javax.servlet.GenericServlet
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class GraceServlet extends GenericServlet {
    @Override
    void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest)req
        ControllerScript controllerScript = new ControllerScript()
        controllerScript.get("/ab"){
            res.writer.println("Hello,Grace!")
        }
        //res.writer.println("Hello,Grace!")
        //res.writer.println(new File('.').absolutePath)

        Route route =controllerScript.routes.find {it.path==request.requestURI}
        if (route){
            route.closure()
        }else {
            res.writer.write("No route fond")
        }

    }
}
