package grace.controller.request

import grace.app.GraceApp

class GraceEx {
    /**
     * 解析文件中的指令，并产生 js 链接
     * todo 缓存以优化
     * @param js
     */
    def assetJs(String js) {
        def app = GraceApp.instance
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
                            if (!required.contains(path) && path != jsPath) required.add(path) //注意不包含本尊
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
                String uri = it.substring(app.assetDir.canonicalPath.size()+1).replaceAll("\\\\","/")
                result += "<script type=\"text/javascript\" src=\"/assets/${uri}\" ></script>"
            }

            return result
        }
    }
}
