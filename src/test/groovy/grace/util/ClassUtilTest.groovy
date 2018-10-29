package grace.util

import grace.datastore.entity.Transformer

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

    void testDate() {
        println Transformer.toLocalDateTime("2018-10-22T15:53:48.860")
        println Transformer.toLocalDateTime("2018-10-22T15:53:48.860698")
    }
}
