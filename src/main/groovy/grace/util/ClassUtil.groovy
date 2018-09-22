package grace.util

class ClassUtil {
    /**
     * 获取 class 的默认变量名称
     * @param c
     * @return
     */
    static String propertyName(Class c){return c.simpleName.uncapitalize()}

    /**
     * 获取包名
     * @param aClass
     * @return
     */
    static String packageName(Class aClass){
        aClass.name.substring(0,aClass.name.lastIndexOf('.'))
    }

    /**
     * 获取包路径
     * @param aClass
     * @return
     */
    static String packagePath(Class aClass){
        packageName(aClass).split('\\.').join(File.separator)
    }

    /**
     * 获取类路径
     * @param aClass
     * @return
     */
    static String classPath(Class aClass){
        aClass.name.split('\\.').join(File.separator)
    }
}
