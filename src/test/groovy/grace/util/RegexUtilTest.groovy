package grace.util

class RegexUtilTest extends GroovyTestCase {
    void testTransformPathIntoRegex() {

        // println RegexUtil.transformPathIntoRegex('*')
        // println RegexUtil.transformPathIntoRegex('/*')
        // println RegexUtil.transformPathIntoRegex('/@a')
        // println RegexUtil.transformPathIntoRegex('/@a**')
        // println RegexUtil.transformPathIntoRegex('/\\.')
        // println RegexUtil.transformPathIntoRegex('/a\\.')
        println RegexUtil.transformPathIntoRegex('/blogs/@id')
        println RegexUtil.transformPathIntoRegex('/blogs/@id/**')
        println RegexUtil.transformPathIntoRegex('/blogs/@id**')
    }

    void testToPattern() {
        println '/blogs'.matches(RegexUtil.toPattern('/blogs'))
        println '/blogs/1'.matches(RegexUtil.toPattern('/blogs/@id'))
        println '/blogs/show/1'.matches(RegexUtil.toPattern('/blogs/@id'))
        println '/blogs/show/1'.matches(RegexUtil.toPattern('/blogs/@id*'))
        println '/blogs/1'.matches(RegexUtil.toPattern('/blogs/@id*'))
        println '/blogs/show/1'.matches(RegexUtil.toPattern('/blogs/@id**'))
    }
}
