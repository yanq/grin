package grace.controller.traits

import grace.app.GraceApp
import org.thymeleaf.context.Context

/**
 * thymeleaf
 */
trait Render extends RequestBase {
    String controllerName = '' //当前控制器

    /**
     * 返回string
     * @param string
     */
    void render(String string) {
        response.getWriter().write(string)
    }

    /**
     * view and model
     * 默认 thymeleaf 渲染
     * @param view
     * @param model
     */
    void render(String view, Map model) {
        Context ctx = new Context()
        model.putAll(toMap())
        ctx.setVariables(model)

        String path = view.startsWith('/') ? view : "/${controllerName}/${view}"

        GraceApp.instance.templateEngine.process(path, ctx, response.getWriter())
    }
}