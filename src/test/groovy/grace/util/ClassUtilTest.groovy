package grace.util

class ClassUtilTest extends GroovyTestCase {
    void testPropertyName() {
        println ClassUtil.simpleName(String)
        println ClassUtil.propertyName(String)
        println ClassUtil.packageName(String)
        println ClassUtil.packagePath(String)
        println ClassUtil.classPath(String)


        println ClassUtil.propertyName("HelloController")
        println ClassUtil.propertyName("HController")
        println ClassUtil.propertyName("Controller")
    }
}
