package grace.util

import java.util.regex.Pattern

/**
 * 正则表达式工具
 */
class RegexUtil {
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
                .replaceAll('\\*\\*', '(?:.+\\/?){0,}')
                .replaceAll('\\*', '[^\\/]+')
                .replaceAll('@\\w+', '(.+)')
    }

    /**
     * 转成正则模式
     * @param path
     * @return
     */
    static Pattern toPattern(String path) {
        Pattern.compile(transformPathIntoRegex(path))
    }

    /**
     * 整理URI后面的内容
     * @param requestURI
     * @return
     */
    static String clearRequestURI(String requestURI) {
        if (requestURI.indexOf('?') > 0) return requestURI.substring(0, requestURI.indexOf('?'))
        if (requestURI.indexOf(';') > 0) return requestURI.substring(0, requestURI.indexOf(';'))
        if (requestURI.indexOf('#') > 0) return requestURI.substring(0, requestURI.indexOf('#'))
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
}
