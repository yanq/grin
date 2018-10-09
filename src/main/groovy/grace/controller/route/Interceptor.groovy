package grace.controller.route

import grace.util.RegexUtil
import java.util.regex.Pattern

/**
 * 拦截器
 */
class Interceptor {
    static final int ORDER_HIGH = 0
    static final int ORDER_NORMAL = 50
    static final int ORDER_LOW = 100

    int order
    String path
    Closure closure
    Pattern pattern

    boolean matches(String uri){
        if (!pattern) pattern = RegexUtil.toPattern(path)
        return uri.matches(pattern)
    }
}
