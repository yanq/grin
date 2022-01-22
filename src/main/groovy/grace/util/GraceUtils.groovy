package grace.util

class GraceUtils {

    /**
     * 整理URI后面的内容
     * @param requestURI
     * @return
     */
    static String clearRequestURI(String requestURI) {
        if (requestURI.indexOf(';') > 0) return requestURI.substring(0, requestURI.indexOf(';'))
        if (requestURI.indexOf('#') > 0) return requestURI.substring(0, requestURI.indexOf('#'))
        if (requestURI.indexOf('?') > 0) return requestURI.substring(0, requestURI.indexOf('?'))
        if (requestURI.endsWith('/')) return requestURI.substring(0, requestURI.length() - 1)
        return requestURI
    }

    /**
     * 转换成内部的 uri
     * @param requestURI
     * @param context
     * @return
     */
    static String toURI(String requestURI, String context = '') {
        if (context.size() > 1 && requestURI.startsWith(context)) requestURI = requestURI.substring(context.size())
        return clearRequestURI(requestURI) ?: '/'
    }

    /**
     * 从 url 解析出来控制器，操作，id 等内容。
     * @param uri 处理后的 uri
     */
    static splitURI(String uri) {
        String controllerName = 'home' //默认用 home 路径，现在还没有地方定义首页
        String actionName = 'index' // 默认 action
        String id = null
        if (uri) {
            def l = uri.substring(1).split('/')
            if (l.size() > 0 && l[0]) controllerName = l[0]
            if (l.size() > 1 && l[1]) actionName = l[1]
            if (l.size() > 2) id = l[2..-1].join('/')
        }
        return [controllerName, actionName, id]
    }
}
