package grace.util

class ClassUtilTest extends GroovyTestCase {
    void testPropertyName() {
        println ClassUtil.simpleName(String)
        println ClassUtil.propertyName(String)
        println ClassUtil.packageName(String)
        println ClassUtil.packagePath(String)
        println ClassUtil.classPath(String)

        println ClassUtil.simpleName("Hello")
        println ClassUtil.propertyName("Hello")
        println ClassUtil.packageName("Hello")
        println ClassUtil.packagePath("Hello")
        println ClassUtil.classPath("Hello")
    }
}
