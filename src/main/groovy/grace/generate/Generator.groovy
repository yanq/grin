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
    static final templateEngine = new StreamingTemplateEngine()

    /**
     * 创建 领域类
     * @param className
     */
    static createDomain(String className) {
        File template = new File(templateDir, 'domain')
        File target = new File(GraceApp.instance.domainsDir, ClassUtil.classPath(className) + '.groovy')
        generate(template, target, [className: className])
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
        File target = new File(GraceApp.instance.controllersDir, ClassUtil.classPath(className) + '.groovy')
        generate(template, target, [className: className])
    }

    /**
     * 从领域类生成控制器和视图
     * @param className
     * @return
     */
    static generateAll(String className) {
        Class aClass= Class.forName(className)


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
            log.warn("file exists,do nothing! @ $targetFile.canonicalPath ")
        } else {
            Template template = templateEngine.createTemplate(templateFile)
            targetFile << template.make(binding).toString().getBytes('utf-8')
            log.info("generate file @ $targetFile.canonicalPath")
        }
    }

    /**
     * 获取模板目录
     * @return
     */
    static File getTemplateDir() {
        new File(GraceApp.instance.appDir, 'templates')
    }
}
