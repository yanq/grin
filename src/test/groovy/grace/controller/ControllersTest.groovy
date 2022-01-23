package gun.controller

import gun.web.Controllers

class ControllersTest extends GroovyTestCase {
    void testLoadControllers() {
        println(new File('.').absolutePath)
        def c = new Controllers()
        c.load(new File("/Users/yan/projects/gun-dev/gun/src/main/groovy"))
    }

    void testLoadSplit() {
        def s = "/Users/yan/projects/gun-dev/gun/src/main/groovy".split('/')
        def s1 = '/'.split('/')
        def s2 = ''.split('/')
        println('ok')
    }
}
