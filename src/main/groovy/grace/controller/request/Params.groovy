package grace.controller.request

import groovy.transform.CompileStatic
import java.text.SimpleDateFormat

/**
 * 参数
 */
@CompileStatic
class Params extends HashMap<String, Object> {
    List<String> dateFormats = ['EEE MMM dd HH:mm:ss z yyyy', 'yyyy-MM-dd', "yyyy-MM-dd HH:mm"]
    Locale l = Locale.ENGLISH

    void addDateFormat(String format) { dateFormats << format }

    Date date(String key, String format = null) {
        def value = super.get(key)
        if (value instanceof Date) return value
        if (value.toString().contains('月')) l = Locale.SIMPLIFIED_CHINESE

        if (format) {
            return new SimpleDateFormat(format, l).parse(value.toString())
        }

        Date date = null
        for (int i = 0; i < dateFormats.size(); i++) {
            try {
                def dateFormat = new SimpleDateFormat(dateFormats[i],l)
                date = dateFormat.parse(value.toString())
            } catch (Exception e) {
                e.printStackTrace()
            }
            if (date) return date
        }

        return date
    }
}