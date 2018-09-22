package grace.util

class ClassUtilTest extends GroovyTestCase {
    void testPropertyName() {
        println ClassUtil.propertyName(String)
        println ClassUtil.propertyName(String.name)
        println ClassUtil.packageName(String)
        println ClassUtil.packagePath(String)
        println ClassUtil.classPath(String)
    }
}
