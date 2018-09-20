package grace.util

class GraceUtil {
    /**
     * 获取 class 的默认变量名称
     * @param c
     * @return
     */
    static String classToName(Class c){return c.simpleName.uncapitalize()}
}
