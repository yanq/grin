package grace.util

import groovy.util.logging.Slf4j
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * 文件处理
 */
@Slf4j
class FileUtil {

    /**
     * 文件服务，调用入口
     * @param request
     * @param response
     * @param file
     * @param cacheTime
     */
    static void serveFile(HttpServletRequest request, HttpServletResponse response, File file, int cacheTime = 0) {
        if (!file.exists() || file.isDirectory()) {
            deal404(response)
            return
        }

        def etag = file.lastModified().toString()
        if (cached(request, etag)) {
            dealCache(response)
        } else {
            dealNoCachedFile(request, response, file, cacheTime, etag)
        }
    }

    /**
     * 是否修改
     * @param request
     * @param etag
     * @return
     */
    static private boolean cached(HttpServletRequest request, String etag) {
        def ifNoneMatchHeader = request.getHeader('If-None-Match')
        if (ifNoneMatchHeader && ifNoneMatchHeader == etag) {
            return true
        } else {
            return false
        }
    }

    /**
     * 处理未修改情况
     * @param response
     */
    static private void dealCache(HttpServletResponse response) {
        response.status = HttpServletResponse.SC_NOT_MODIFIED
        response.flushBuffer()
    }

    /**
     * 处理404
     * @param response
     */
    static private void deal404(HttpServletResponse response) {
        response.status = HttpServletResponse.SC_NOT_FOUND
        response.flushBuffer()
    }

    /**
     * 处理新文件
     * @param request
     * @param response
     * @param target
     * @param cacheTime
     * @param etag
     */
    static private void dealNoCachedFile(HttpServletRequest request, HttpServletResponse response, File target, int cacheTime, String etag) {

        response.setContentType(request.getServletContext().getMimeType(request.requestURI))
        response.setHeader("ETag", etag)
        response.setHeader('Cache-Control', 'public, max-age=' + cacheTime)
        response.setContentLength((int) target.length());
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Disposition", "attachment;filename=\" " + target.getName() + " \"");

        OutputStream out = response.getOutputStream()
        try {
            FileInputStream fis = new FileInputStream(target);

            long start = 0, end = 0;
            long fileLength = target.length();

            if (request.getHeader("Range") != null) {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);//206

                def ranges = request.getHeader("Range").replaceAll("bytes=", "").split('-')
                start = ranges[0].toLong()
                end = ranges.size() == 2 ? ranges[1].toLong() : 0

                response.setHeader("Content-Range", "bytes ${start}-${end > 0 ? end : fileLength - 1}/${fileLength}");
            }

            long contentLength = end > 0 ? end + 1 - start : fileLength - start
            response.setHeader("Content-Length", "${contentLength}");
            //log.info("file download ${start}-${end} , content length ${contentLength}")

            byte[] buffer = new byte[1024];
            int needSize = contentLength;
            fis.skip(start)
            while (needSize > 0) {
                int len = fis.read(buffer);
                if (needSize < buffer.length) {
                    out.write(buffer, 0, needSize);
                } else {
                    out.write(buffer, 0, len);
                    if (len < buffer.length) { //最后一波
                        break;
                    }
                }
                needSize -= buffer.length;
            }

            fis.close();
        } catch (Exception e) {
            log.warn "Exception when request: ${request.requestURI} , file: ${target.absolutePath},$e"
        } finally {
            out.close()
        }
    }

    /**
     * 产生一个 uuid 文件名，分析后缀
     * @param fileName
     * @return
     */
    static String fileUUIDName(String fileName){
        def s = fileName.split('\\.')
        def postFix = s.size()==2 ? s[1] : (s.size()==1 ? '' : s[1..-1].join('.'))
        def uuid = UUID.randomUUID().toString().replace('-','')
        "${uuid}.${postFix}"
    }
}
