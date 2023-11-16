package grin.web

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Route 路由
 * 从定义的路由里，解析出 controller，action，params 等。
 */
class Route {
    String pathReg
    String resource
    Pattern pattern
    String controllerName
    String actionName
    String id
    List<String> pathParamNames


    /**
     * 路由初始化
     * 检查是否符合规范
     * 如果 resource 为空，urlReg 里要有 @controllerName 和 @actionName
     * 如果 resource 不为空，格式为 controllerName[-actionName]
     * 如果 actionName 不存在，则 urlReg 里要有 actionName
     */
    Route(String pathReg, String resource) {
        this.pathReg = pathReg
        this.resource = resource
        this.pattern = toPattern(pathReg)
        this.pathParamNames = pathReg.findAll(/@\w+/).collect { it.substring(1) }

        if (resource) {
            def l = resource.split('-')
            if (l.length > 0) controllerName = l[0]
            if (l.length > 1) actionName = l[1]
            if (l.length > 2) id = l[2]
            if (l.length > 3) throw new Exception("路由定义，内部资源格式错误：${pathReg} -> ${resource}")
        }

        if (!controllerName && !pathReg.contains("@controllerName")) throw new Exception("路由定义，缺少 controllerName：${pathReg} -> ${resource}")
    }

    /**
     * 匹配否
     * @param requestURI
     * @return
     */
    boolean matches(String uri) {
        return uri.matches(pattern)
    }

    /**
     * 路径参数
     * @param requestURI
     * @return
     */
    Map<String, Object> getPathParams(String uri) {
        def result = [:]
        if (pathParamNames) {
            Matcher matcher = pattern.matcher(uri)
            if (matcher.matches()) {
                pathParamNames.each {
                    result[it] = matcher[0][result.size() + 1]
                }
            }
        }
        return result
    }

    /**
     * 转成正则表达式
     * copy from gaelyk
     */
    static String transformPathIntoRegex(String path) {
        if (path.matches(/\/\*\*\/\*\.(\w+)$/)) {
            return path.replace('**/*.', '.*\\.') + '$'
        }
        if (path.matches(/\/\*\*(\/\*\.\*)?$/)) {
            return '.*'
        }
        path.replaceAll('\\.', '\\\\.')
                .replaceAll('@\\w+\\*\\*', '(.+)')
                .replaceAll('\\*\\*', '(?:.+\\/?){0,}')
                .replaceAll('\\*', '[^\\/]+')
                .replaceAll('@\\w+', '([^/]+)')
    }

    /**
     * 转成正则模式
     * @param path
     * @return
     */
    static Pattern toPattern(String path) {
        Pattern.compile(transformPathIntoRegex(path))
    }

    @Override
    String toString() {
        return "${pathReg}(${pattern}) -> ${resource}"
    }
}
