package gun.util

import java.text.SimpleDateFormat

/**
 * Grace 扩展类的静态方法
 */
class GraceStaticExtension {
    /**
     * Date parseDate
     * @param date
     * @param dateString
     * @param dateFormat
     * @return
     */
    public static Date parseDate(Date date,String dateString,String dateFormat = 'yyyy-MM-dd HH:mm'){
        new SimpleDateFormat(dateFormat).parse(dateString)
    }
}
