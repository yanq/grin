package grin.generate

import grin.app.App
import groovy.text.StreamingTemplateEngine
import groovy.text.Template
import groovy.util.logging.Slf4j

/**
 * 生成器
 * Created by yan on 2017/2/25.
 */
@Slf4j
class Generator {
    static final templateEngine = new StreamingTemplateEngine()

    /**
     * 创建 领域类
     * @param className
     */
    static createDomain(String className) {
        File template = new File(templateDir, 'domain')
        File target = new File(App.instance.domainsDir, ClassUtils.classPath(className) + '.groovy')
        generate(template, target, ClassUtils.toMap(className))
    }

    /**
     * 创建新的控制器,简单示例
     * 如果存在，略过
     * @param templateFile
     * @param className
     * @return
     */
    static createController(String className) {
        File template = new File(templateDir, 'controller')
        File target = new File(controllersDir, ClassUtils.classPath(className) + 'Controller.groovy')
        generate(template, target, ClassUtils.toMap(className))
    }

    /**
     * 从领域类生成控制器和视图
     * @param className
     * @return
     */
    static generateAll(String className) {
        Class entityClass = Class.forName(className)
        String propName = ClassUtils.propertyName(className)

        //生成增删改查的控制器和视图
        File template = new File(templateDir, 'curdcontroller')
        File target = new File(controllersDir, ClassUtils.classPath(className) + 'Controller.groovy')
        generate(template, target, ClassUtils.toMap(entityClass))

        List files = ['index.html', 'show.html', 'create.html', 'edit.html']
        files.each {
            File viewTemplate = new File(templateDir, it)
            File viewTarget = new File(viewsDir, "${propName}/${it}")
            generate(viewTemplate, viewTarget, ClassUtils.toMap(entityClass))
        }
    }

    /**
     * 从领域类生成 Service
     * @param className
     * @return
     */
    static generateService(String className) {
        Class entityClass = Class.forName(className)
        String propName = ClassUtils.propertyName(className)
        File template = new File(templateDir, 'servicecontroller')
        File target = new File(controllersDir, ClassUtils.classPath(className) + 'ServiceController.groovy')
        generate(template, target, ClassUtils.toMap(entityClass))
    }

    /**
     * 生成文件
     * @param templateFile
     * @param targetFile
     * @param binding
     * @return
     */
    static generate(String templateFile, File targetFile, Map binding = [:]) {
        generate(getTemplateFile(templateFile), targetFile, binding)
    }

    static generate(File templateFile, File targetFile, Map binding = [:]) {
        if (!targetFile.parentFile.exists()) targetFile.parentFile.mkdirs()
        if (targetFile.exists()) {
            log.warn("file exists,do nothing! @ $targetFile.canonicalPath ")
        } else {
            targetFile << generate(templateFile, binding).getBytes('utf-8')
            log.info("generate file @ $targetFile.canonicalPath")
        }
    }

    static String generate(String templateFile, Map binding = [:]) {
        generate(getTemplateFile(templateFile), binding)
    }

    static String generate(File templateFile, Map binding = [:]) {
        Template template = templateEngine.createTemplate(templateFile)
        template.make(binding).toString()
    }

    /**
     * 获取模板目录
     * @return
     */
    static File getTemplateDir() {
        new File(App.instance.appDir, 'templates')
    }

    /**
     * 获取模板文件
     * @return
     */
    static File getTemplateFile(String fileName) {
        new File(templateDir, fileName)
    }

    /**
     * 获取视图目录
     * @return
     */
    static File getViewsDir() {
        App.instance.viewsDir
    }

    /**
     * 获取控制器目录
     * @return
     */
    static File getControllersDir() {
        App.instance.controllersDir
    }
}
