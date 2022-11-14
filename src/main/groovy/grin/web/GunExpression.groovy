package grin.web

import grin.app.GunApp
import grin.generate.Generator

import javax.servlet.http.HttpServletRequest

/**
 * gun 表达式
 * 提供一些方法，处理一些东西。如 asset,link。
 */
class GunExpression {
    def app = GunApp.instance
    HttpServletRequest request
    def assetsPath = '/files/assets'
    /**
     * 处理 application.js
     * 解析文件中的指令，并产生 js 链接
     * todo 缓存以优化
     * @param js
     */
    def assetJs(String js) {
        if (app.isDev()) {
            File jsDir = new File(app.assetDir, 'javascripts')
            File jsFile = new File(jsDir, js)
            if (!jsFile.exists()) return ''

            String jsPath = jsFile.canonicalPath
            List<String> required = []
            jsFile.eachLine {
                if (it.startsWith('//= require ')) {
                    required.add(new File(jsDir, it.substring(11).trim() + '.js').canonicalPath)
                }
                if (it.startsWith('//= require_tree ')) {
                    def dir = it.substring(16).trim()
                    File tree = new File(jsDir, dir)
                    if (tree.isDirectory()) {
                        tree.eachFileRecurse {
                            String path = it.canonicalPath
                            if (path.endsWith('.js') && !required.contains(path) && path != jsPath) required.add(path)
                            //注意不包含本尊
                        }
                    }
                }
                if (it.startsWith('//= require_self')) { //得放到此时此地，如果前面有了，要去掉
                    if (required.contains(jsPath)) required.remove(jsPath)
                    required.add(jsPath)
                }
            }

            String result = ""
            required.each {
                String uri = it.substring(app.assetDir.canonicalPath.size() + 1).replaceAll("\\\\", "/")
                result += "<script type=\"text/javascript\" src=\"${assetsPath}/${uri}\" ></script>\n        "
            }

            return result.trim()
        } else {
            return "<script type=\"text/javascript\" src=\"${assetsPath}/${js}\" ></script>"
        }
    }

    /**
     * 解析 application.css
     * @param css
     * @return
     */
    def assetCss(String css) {
        if (app.isDev()) {
            File cssDir = new File(app.assetDir, 'stylesheets')
            File cssFile = new File(cssDir, css)
            if (!cssFile.exists()) return ''

            String cssPath = cssFile.canonicalPath
            List<String> required = []
            cssFile.eachLine {
                if (it.startsWith('*= require ')) {
                    required.add(new File(cssDir, it.substring(11).trim() + '.css').canonicalPath)
                }
                if (it.startsWith('*= require_tree ')) {
                    def dir = it.substring(16).trim()
                    File tree = new File(cssDir, dir)
                    if (tree.isDirectory()) {
                        tree.eachFileRecurse {
                            String path = it.canonicalPath
                            if (path.endsWith('.css') && !required.contains(path) && path != cssPath) required.add(path)
                            //注意不包含本尊
                        }
                    }
                }
                if (it.startsWith('*= require_self')) { //得放到此时此地，如果前面有了，要去掉
                    if (required.contains(cssPath)) required.remove(cssPath)
                    required.add(cssPath)
                }
            }

            String result = ""
            required.each {
                String uri = it.substring(app.assetDir.canonicalPath.size() + 1).replaceAll("\\\\", "/")
                result += "<link rel=\"stylesheet\" href=\"${assetsPath}/${uri}\"/>\n        "
            }

            return result.trim()
        } else {
            return "<link rel=\"stylesheet\" href=\"${assetsPath}/${css}\"/>"
        }
    }

    /**
     * assetImg
     * @param f
     * @return
     */
    def assetImg(String f) {
        if (app.isDev()) {
            "${assetsPath}/images/$f"
        } else {
            "${assetsPath}/$f"
        }
    }

    /**
     * 文件链接处理 位于 static 目录下
     * @param f
     * @return
     */
    def 'static'(String f) {
        "/files/static/$f"
    }

    /**
     * 拼接字符串
     */
    def string(Object... list) {
        list.collect { it ? it.toString() : '' }.join()
    }

    /**
     * 分页
     * 这里多个参数，是因为 tl 没法传递 map 数据为参数。
     * 一个 tf 的好处是，参数它会处理，变成相应的类型
     * @param offset
     * @param limit
     * @param total
     * @param params
     * @return
     */
    def pagination(int offset, int limit, int total, String params = '') {
        if (total == 0) return ''
        int current = (offset / limit as int) + 1
        int pageCount = (total % limit == 0) ? (total / limit as int) : (total / limit as int) + 1
        def pre, next
        if (current != 1) {
            pre = [title: current - 1, link: link('', [offset: offset - limit, limit: limit]) + "${params ? '&' + params : ''}"]
        }
        if (current != pageCount) {
            next = [title: current + 1, link: link('', [offset: offset + limit, limit: limit]) + "${params ? '&' + params : ''}"]
        }
        Generator.generate('components/pagination.html', [current: current, pageCount: pageCount, pre: pre, next: next])
    }

    /**
     * 链接生成
     * @param uri
     * @param params
     * @return
     */
    def link(String uri, Map params = [:]) {
        def paramsString = params.collect { return "${it.key}=${URLEncoder.encode(it.value.toString(), "utf-8")}" }.join('&')
        "${uri}${params ? '?' + paramsString : ''}"
    }
}
