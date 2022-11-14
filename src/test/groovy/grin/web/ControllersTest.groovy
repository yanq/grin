package grin.web

class ControllersTest extends GroovyTestCase {
    void testLoadControllers() {
        println(new File('.').absolutePath)
        def c = new WebUtils()
        c.loadControllers(new File("/Users/yan/projects/grin-dev/grin/src/main/groovy"))
    }

    void testLoadSplit() {
        def s = "/Users/yan/projects/grin-dev/grin/src/main/groovy".split('/')
        def s1 = '/'.split('/')
        def s2 = ''.split('/')
        println('ok')
    }

    void testSplit() {
        print("a.bc.dd".split('.'))
        print("a.bc.dd".split('\\.'))
    }
}
