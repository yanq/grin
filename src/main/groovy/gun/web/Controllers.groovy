package gun.web

import groovy.util.logging.Slf4j

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
    synchronized void load(File dir) {
        log.info('load interceptor and controllers ...')

        controllerMap.clear()
        methodMap.clear()
        interceptor = null

        dir.eachFileRecurse {
            if (!it.name.endsWith('.groovy')) return //忽略非目标文件
            def name = it.name.split('\\.')[0].uncapitalize()
                    .replaceAll("Controller", '')
                    .replaceAll('Interceptor', '')
            def className = it.canonicalPath.replace(dir.canonicalPath, '').substring(1)
            className = className.substring(0, className.lastIndexOf('.')).replace('/', '.')
            if (it.name.matches('.+Controller.groovy')) {
                if (controllerMap.containsKey(name)) throw new Exception("控制器 ${name} 已经存在: ${controllerMap.get(name)},新的 ${className}")
                controllerMap.put(name, className)
            }
            if (it.name.matches('.+Interceptor.groovy')) {
                if (interceptor) throw new Exception("已经存在拦截器 ${interceptor.class.name}, 又有：${it.name}")
                Class clazz = Class.forName(className)
                if (!Interceptor.isAssignableFrom(clazz)) throw new Exception("拦截器 ${clazz} 不是 ${Interceptor.class}")
                interceptor = clazz.newInstance()
            }
        }

        if (!interceptor) interceptor = new Interceptor()

        controllerMap.each { name, className ->
            Class clazz = Class.forName(className)
            if (!Controller.isAssignableFrom(clazz)) throw new Exception("控制器 ${clazz} 不是 ${Controller.class}")
            Class.forName(className).getDeclaredMethods()
                    .findAll {
                        def methodName = it.name
                        !['$', 'super$'].find { methodName.startsWith(it) }
                    }
                    .each {
                        methodMap.put("${name}-${it.name.uncapitalize()}", it)
                    }
        }

        log.info("loaded interceptor ${interceptor.class.simpleName} and controllers ${controllerMap.keySet()}")
    }
}
