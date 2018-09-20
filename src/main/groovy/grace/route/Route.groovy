package grace.route

import grace.util.RegexUtil
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * 路由
 */
class Route {
    String method
    String path
    Closure closure
    Pattern pattern

    /**
     * 匹配否
     * @param requestURI
     * @return
     */
    boolean matches(String uri) {
        if (!pattern) pattern = RegexUtil.toPattern(path)
        return uri.matches(pattern)
    }

    /**
     * 路径参数
     * @param requestURI
     * @return
     */
    Map getPathParams(String uri) {
        def result = [:]

        if (!pattern) RegexUtil.toPattern(path)

        List<String> vars = path.findAll(/@\w+/)
        if (!vars) return result

        Matcher matcher = pattern.matcher(uri)
        if (matcher.matches()) {
            vars.each {
                result[it.substring(1)] = [matcher[0][result.size() + 1]]
            }
        }

        return result
    }
}
