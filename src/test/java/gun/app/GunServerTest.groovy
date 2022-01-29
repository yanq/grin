package gun.app

class GunServerTest extends GroovyTestCase {
    void testBuildSSL() {
        new GunServer().buildSSL()
    }
}
