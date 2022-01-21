package grace.controller


import grace.util.ClassUtil
import groovy.util.logging.Slf4j

/**
 * 控制器工具类
 * 加载控制器，以及处理等。
 */
@Slf4j
class Controllers {

    Map<String, String> controllerMap = [:]

    void loadControllers(File dir) {
        controllerMap.clear()
        log.info('start load controllers')
        dir.eachFileRecurse {
            if (ClassUtil.isJavaClass(it.name)) {
                def name = ClassUtil.reduce(it.name.split('\\.')[0].uncapitalize())
                def className = ClassUtil.pathToClassName(it.canonicalPath.replace(dir.canonicalPath, '').substring(1))
                if (controllerMap.containsKey(name)) throw new Exception("控制器 ${name} 已经存在: ${controllerMap.get(name)},新的 ${className}")
                controllerMap.put(name, className)
            }
        }
        log.info("loaded, controllers ${controllerMap.keySet()}")
    }

}
