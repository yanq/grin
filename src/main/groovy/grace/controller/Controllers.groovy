package grace.controller


import grace.util.ClassUtil
import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.reflect.Method

/**
 * 控制器工具类
 * 加载控制器，以及处理等。
 */
@Slf4j
class Controllers {

    Map<String, String> controllerMap = [:]
    Map<String, Method> methodMap = [:]

    /**
     * 加载所有的控制器
     * @param dir
     */
    void loadControllers(File dir) {
        log.info('start load controllers')

        controllerMap.clear()
        methodMap.clear()

        dir.eachFileRecurse {
            if (it.name.matches('.+Controller.groovy')) {
                def name = ClassUtil.reduce(it.name.split('\\.')[0].uncapitalize())
                def className = ClassUtil.pathToClassName(it.canonicalPath.replace(dir.canonicalPath, '').substring(1))
                if (controllerMap.containsKey(name)) throw new Exception("控制器 ${name} 已经存在: ${controllerMap.get(name)},新的 ${className}")
                controllerMap.put(name, className)
            }
        }

        controllerMap.each { name, className ->

            Class.forName(className).getDeclaredMethods()
                    .findAll {
                        def methodName = it.name
                        !['$', 'super$'].find { methodName.startsWith(it) }
                    }
                    .each {
                        methodMap.put("${name}-${it.name.uncapitalize()}", it)
                    }
        }

        log.info("loaded, controllers ${controllerMap.keySet()}")
    }

    /**
     * 执行 action
     * @param controllerName
     * @param actionName
     * @param id
     */
    void executeAction(HttpServletRequest request, HttpServletResponse response, String controllerName, String actionName, String id) {

    }

}
