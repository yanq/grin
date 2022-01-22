package grace.controller

class ControllersTest extends GroovyTestCase {
    void testLoadControllers() {
        println(new File('.').absolutePath)
        def c = new Controllers()
        c.reload(new File("/Users/yan/projects/grace-dev/grace/src/main/groovy"))
    }
}
