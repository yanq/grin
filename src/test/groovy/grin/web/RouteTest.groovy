package grin.web

class RouteTest extends GroovyTestCase {
    Map<String, String> urlMapping = [
            '/'                                   : 'home',
            '/*'                                  : 'home',
            '/**'                                 : 'home',
            '/files/@actionName?/@id?'            : 'files',
            '/files/@actionName?/@id*?'           : 'files',
            '/files/@actionName?/@id**?'          : 'files',
            '/@controllerName/?@actionName?/?@id?': '',
    ]

    void testMatches() {
        urlMapping.each {
            println(new Route(it.key, it.value))
        }
    }
}
