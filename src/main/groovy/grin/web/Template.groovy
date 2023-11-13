package grin.web

import grin.app.App
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.WebContext
import org.thymeleaf.templateresolver.FileTemplateResolver

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 模板
 */
class Template {
    TemplateEngine templateEngine

    Template(App app) {
        templateEngine = new TemplateEngine()
        FileTemplateResolver resolver = new FileTemplateResolver()
        resolver.setPrefix(app.viewsDir.canonicalPath)
        resolver.setSuffix('.html')
        resolver.setCharacterEncoding('utf-8')
        resolver.setCacheable(!app.isDev())
        templateEngine.setTemplateResolver(resolver)
    }

    /**
     * 控制器外渲染
     * @param request
     * @param path
     * @param modal
     */
    void render(HttpServletRequest request, HttpServletResponse response, String path, Map model) {
        WebContext ctx = new WebContext(request, response, request.getServletContext(), request.getLocale())
        ctx.setVariables(model)
        templateEngine.process(path, ctx, response.getWriter())
    }

    /**
     * 控制器内渲染
     * @param controller
     * @param path
     * @param modal
     */
    void render(Controller controller, String view, Map model) {
        WebContext ctx = new WebContext(controller.request, controller.response, controller.context, controller.request.getLocale())
        Map map = [app    : controller.app, controllerName: controller.controllerName, actionName: controller.actionName,
                   context: controller.context, request: controller.request, response: controller.response, session: controller.session, params: controller.params]
        map.putAll(model)
        ctx.setVariables(map)
        String path = view.startsWith('/') ? view : "/${controller.controllerName}/${view}"
        templateEngine.process(path, ctx, controller.response.getWriter())
    }
}
