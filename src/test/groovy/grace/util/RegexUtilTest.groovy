package grace.util

import gun.util.RegexUtil

class RegexUtilTest extends GroovyTestCase {
    void testTransformPathIntoRegex() {
        'ab'
        // println RegexUtil.transformPathIntoRegex('*')
        // println RegexUtil.transformPathIntoRegex('/*')
        // println RegexUtil.transformPathIntoRegex('/@a')
        // println RegexUtil.transformPathIntoRegex('/@a**')
        // println RegexUtil.transformPathIntoRegex('/\\.')
        // println RegexUtil.transformPathIntoRegex('/a\\.')
        // println RegexUtil.transformPathIntoRegex('/blogs/@id')
        // println RegexUtil.transformPathIntoRegex('/blogs/@id/**')
        // println RegexUtil.transformPathIntoRegex('/blogs/@id**')
        println RegexUtil.transformPathIntoRegex('/blogs/@a/@b?')
    }

    void testToPattern() {
        println '/blogs'.matches(RegexUtil.toPattern('/blogs'))
        println '/blogs/'.matches(RegexUtil.toPattern('/blogs/@id?'))
        println '/blogs/1'.matches(RegexUtil.toPattern('/blogs/@id'))
        println '/blogs/show/1'.matches(RegexUtil.toPattern('/blogs/@id'))
        println '/blogs/show/1'.matches(RegexUtil.toPattern('/blogs/@id*'))
        println '/blogs/1'.matches(RegexUtil.toPattern('/blogs/@id*'))
        println '/blogs/show/1'.matches(RegexUtil.toPattern('/blogs/@id**'))
    }
}
