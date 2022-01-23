package gun.web


import gun.app.GraceApp;

public class LinkUtil {

    /**
     * 获取文件的绝对路径
     * @param file
     * @return
     */
    static String absolute(String file) {
        if (file && !(file.startsWith('http://') || file.startsWith('https://'))) {
            return GraceApp.instance.config.serverURL + file
        } else {
            return file
        }
    }
}
