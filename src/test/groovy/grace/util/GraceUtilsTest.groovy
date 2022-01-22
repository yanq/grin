package grace.util

class GraceUtilsTest extends GroovyTestCase {
    void testSplitURI() {
        def p1 = GraceUtils.splitURI('/')
        def p2 = GraceUtils.splitURI('/a')
        def p3 = GraceUtils.splitURI('/b/c')
        def p4 = GraceUtils.splitURI('/b/c/d')
        def p5 = GraceUtils.splitURI('/b/c/d/e/f.g')

        def p6 = GraceUtils.splitURI('')
        def p7 = GraceUtils.splitURI('/b/')

            println()
    }
}
