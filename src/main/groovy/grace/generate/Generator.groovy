package grace.generate

import grace.app.GraceApp
import grace.util.ClassUtil
import groovy.text.StreamingTemplateEngine
import groovy.text.Template
import groovy.util.logging.Slf4j

/**
 * 生成器
 * Created by yan on 2017/2/25.
 */
@Slf4j
class Generator {
    static final TEMPLATE_DIR = 'templates'
    static final CONTROLLER_TEMPLATE = 'Controller.groovy'
    static final templateEngine = new StreamingTemplateEngine()

    /**
     * 创建 领域类
     * @param className
     */
    static createDomain(String className){
        File template = new File(GraceApp.instance.appDir,TEMPLATE_DIR+'/domain.groovy')
        File target = new File(GraceApp.instance.domainsDir,ClassUtil.classPath(className)+'.groovy')
        generate(template,target,[packageName:ClassUtil.packageName(className),simpleName:ClassUtil.simpleName(className)])
    }

    /**
     * 控制器处理
     * @param className
     * @return
     */
    static controller(String className) {
        Class aClass

        //find class
        try {
            aClass = Class.forName(className)
        } catch (Exception e) {
            log.info("class ${className} is not exist,to generate it")
        }

        //find template
        File appDir = GraceApp.instance.appDir
        File templateDir = new File(appDir, TEMPLATE_DIR)
        File controllerTemplate = new File(templateDir, CONTROLLER_TEMPLATE)
        if (!appDir.exists()) {
            log.error("GraceApp dir ${appDir.absolutePath} not exists ！")
            return
        }
        if (!templateDir.exists()) templateDir.mkdir()
        if (!controllerTemplate.exists()) {
            log.error("template ${controllerTemplate.absolutePath} not exists ！")
            return
        }

        //生成或者创建
        if (aClass) {
            generateController(controllerTemplate, aClass)
        } else {
            createController(controllerTemplate, className)
        }
    }

    /**
     * 创建新的控制器,简单示例
     * 如果存在，略过
     * @param templateFile
     * @param className
     * @return
     */
    static createController(File templateFile, String className) {
        File targetDir = new File(GraceApp.instance.controllersDir, ClassUtil.packagePath(className))
        if (!targetDir.exists()) targetDir.mkdirs()
        File target = new File(GraceApp.instance.controllersDir, ClassUtil.classPath(className) + '.groovy')
        generate(templateEngine, target)
    }

    /**
     * 根据领域类生成控制器，含增删改查操作
     * @param temFile
     * @param aClass
     */
    static generateController(File temFile, Class aClass) {

    }

    /**
     * 生成文件
     * @param templateFile
     * @param targetFile
     * @param binding
     * @return
     */
    static generate(File templateFile, File targetFile, Map binding = [:]) {
        if (!targetFile.parentFile.exists()) targetFile.parentFile.mkdirs()
        if (targetFile.exists()) {
            log.warn("file exists,do nothing! @ $targetFile.absolutePath ")
        } else {
            Template template = templateEngine.createTemplate(templateFile)
            targetFile << template.make(binding).toString().getBytes('utf-8')
            log.info("generate file @ $targetFile.absolutePath")
        }
    }
}
