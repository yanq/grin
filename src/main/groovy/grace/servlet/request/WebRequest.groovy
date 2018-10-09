package grace.servlet.request

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

/**
 * web 请求
 * 包装请求，提供方便变量和方法使用数据
 */
@Slf4j
@CompileStatic
class WebRequest implements JSON, HTML, Render, FileRender, ThymeleafRender {}
