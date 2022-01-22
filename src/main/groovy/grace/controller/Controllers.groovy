package grace.controller


import grace.util.ClassUtil
import groovy.util.logging.Slf4j

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.reflect.Method

/**
 * 控制器工具类
 * 加载控制器，全局的拦截器，执行操作等。
 */
@Slf4j
class Controllers {

    Map<String, String> controllerMap = [:]
    Map<String, Method> methodMap = [:]
    Interceptor interceptor

    /**
     * 加载所有的控制器
     * @param dir
     */
    void reload(File dir) {
        log.info('start load controllers')

        controllerMap.clear()
        methodMap.clear()

        dir.eachFileRecurse {
            def name = ClassUtil.reduce(it.name.split('\\.')[0].uncapitalize())
            def className = ClassUtil.pathToClassName(it.canonicalPath.replace(dir.canonicalPath, '').substring(1))
            if (it.name.matches('.+Controller.groovy')) {
                if (controllerMap.containsKey(name)) throw new Exception("控制器 ${name} 已经存在: ${controllerMap.get(name)},新的 ${className}")
                controllerMap.put(name, className)
            }
            if (it.name.matches('.+Interceptor.groovy')) {
                if (interceptor) throw new Exception("已经存在拦截器 ${interceptor.class.name}, 又有：${it.name}")
                interceptor = Class.forName(className).newInstance()
            }
        }

        if (!interceptor) interceptor = new Interceptor()

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
        Method method = methodMap.get("${controllerName}-${actionName}")
        if (method) {
            if (!interceptor.before(request, response, controllerName, actionName, id)) return
            def instance = method.class.newInstance(request, response)
            method.invoke(instance)
            interceptor.after(request, response, controllerName, actionName, id)
        } else {
            log.warn("不存在 ${controllerName}-${actionName}")
            new Controller(request, response).notFound()
        }
    }
}
