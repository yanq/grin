package grace.app

import groovy.util.logging.Slf4j

/**
 * Grace App
 * 定义规约目录等。
 */
@Slf4j
class GraceApp {
    public static final String APP_DIR = 'grace-app'
    public static final String APP_CONTROLLERS = 'controllers'
    public static final String APP_VIEWS = 'views'

    static initDirs(File root) {
        log.info("init grace app dirs @ ${root.absolutePath}")
        File appRoot = new File(root, APP_DIR)
        [appRoot, new File(appRoot, APP_CONTROLLERS), new File(appRoot, APP_VIEWS)].each {
            if (it.exists()) {
                log.info("${it.name} exists")
            } else {
                it.mkdirs()
                log.info("${it.name} mkdirs")
            }
        }
    }

    static boolean isController(String path) {
        return path.contains(APP_DIR) && path.contains(APP_CONTROLLERS)
    }
}
