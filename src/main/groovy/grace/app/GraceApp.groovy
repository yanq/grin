package grace.app

import groovy.util.logging.Slf4j
import org.codehaus.groovy.runtime.InvokerHelper
import java.nio.file.*
import static java.nio.file.StandardWatchEventKinds.*

/**
 * Grace App
 * 定义规约目录等。
 */
@Slf4j
class GraceApp {
    //目录结构
    public static final String APP_DIR = 'grace-app'
    public static final String APP_CONTROLLERS = 'controllers'
    public static final String APP_VIEWS = 'views'
    //instance
    private static GraceApp instance
    //other
    File root,appDir, controllersDir, viewsDir
    List<File> allDirs

    /**
     * 构造并初始化
     * @param appRoot
     */
    private GraceApp(File appRoot) {
        if (!appRoot) appRoot = new File('.')
        root = appRoot
        appDir = new File(appRoot, APP_DIR)
        controllersDir = new File(appDir, APP_CONTROLLERS)
        viewsDir = new File(appDir, APP_VIEWS)
        allDirs = [appDir, controllersDir, viewsDir]
    }

    /**
     * 设置根路径，并初始化。重复会有异常。
     * @param root
     * @return
     */
    static setRoot(File root) {
        if (instance) throw new Exception("Grace app has inited")
        instance = new GraceApp(root)
    }

    /**
     * 获取单例
     * @return
     */
    static getInstance() {
        if (instance) return instance
        instance = new GraceApp()
        return instance
    }

    /**
     * 初始化项目目录结构
     * @param root
     * @return
     */
    void initDirs() {
        log.info("init grace app dirs @ ${appRoot.absolutePath}")
        allDirs.each {
            if (it.exists()) {
                log.info("${it.name} exists")
            } else {
                it.mkdirs()
                log.info("${it.name} mkdirs")
            }
        }
    }

    /**
     * 检查是否是 grace app
     * @return
     */
    boolean isAppDir() {
        log.info("check grace app dirs @ ${root.absolutePath}")
        return !allDirs.find { !it.exists() }
    }

    /**
     * 判断是否是控制器
     * @param path
     * @return
     */
    boolean isController(String path) {
        return path.startsWith(controllersDir.absolutePath)
    }

    void startFileWatcher() {
        log.info("start watch ${appDir.absolutePath}")

        GroovyClassLoader loader = new GroovyClassLoader()
        WatchService watchService = FileSystems.getDefault().newWatchService()
        Paths.get(appDir.absolutePath).register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)

        Thread watcher = new Thread(new Runnable() {
            @Override
            void run() {
                while (true) {
                    WatchKey key = watchService.take()
                    try {
                        List<String> names = key.pollEvents()*.context().name
                        if (names.contains(APP_CONTROLLERS)) {
                            //refresh routes
                            controllersDir.eachFileRecurse {
                                if (it.name.endsWith('.groovy')) {
                                    Class<Script> s = loader.parseClass(it)
                                    InvokerHelper.runScript(s)
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.warn("file change deal fail")
                        e.printStackTrace()
                    } finally {
                        key.reset()
                        sleep(1000)
                    }
                }
            }
        })
        watcher.setDaemon(true)
        watcher.start()
    }
}
