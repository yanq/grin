package grace.servlet.request

import grace.app.GraceApp
import org.thymeleaf.context.Context

/**
 * render
 * 支持字符串，object，thymeleaf 模板等
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
     * 用以托底，render 任何对象
     * @param o
     */
    void render(Object o) {
        response.getWriter().write(o.toString())
    }
}