package grace.common

import groovy.transform.CompileStatic
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 参数
 */
@CompileStatic
class Params extends HashMap<String, Object> {
    List<String> dateFormats = ['yyyy-MM-dd', 'yyyyMMdd']
    List<String> dateTimeFormats = ['EEE MMM dd HH:mm:ss z yyyy', "yyyy-MM-dd HH:mm:ss.SSS", "yyyy-MM-dd HH:mm"]
    Locale local = Locale.ENGLISH

    /**
     * 增加更多格式
     * @param format
     */
    void addDateFormat(String format) { dateFormats << format }

    void addDateTimeFormat(String format) { dateTimeFormats << format }

    /**
     * 解析 Date
     * @param key
     * @param format
     * @return
     */
    Date date(String key, String format = null) {
        def value = super.get(key)
        if (value instanceof Date) return value
        if (value.toString().contains('月')) local = Locale.SIMPLIFIED_CHINESE

        if (format) {
            return new SimpleDateFormat(format, local).parse(value.toString())
        }

        Date date = null
        List<String> allFormats = dateTimeFormats + dateFormats
        for (int i = 0; i < allFormats.size(); i++) {
            try {
                def dateFormat = new SimpleDateFormat(allFormats[i], local)
                date = dateFormat.parse(value.toString())
            } catch (Exception e) {
                // e.printStackTrace()
            }
            if (date) return date
        }

        return date
    }

    /**
     * 解析 LocalDate
     * @param key
     * @param format
     * @return
     */
    LocalDate localDate(String key, String format = null) {
        def value = super.get(key)
        if (value instanceof LocalDate) return value

        if (format) {
            return LocalDate.parse(value as CharSequence, DateTimeFormatter.ofPattern(format, local))
        }

        LocalDate date = null
        for (int i = 0; i < dateFormats.size(); i++) {
            try {
                date = LocalDate.parse(value as CharSequence, DateTimeFormatter.ofPattern(dateFormats[i], local))
            } catch (Exception e) {
                // e.printStackTrace()
            }
        }

        if (date) {
            return date
        } else {
            throw new Exception("no format for local date : $value")
        }

    }

    /**
     * 解析 LocalDateTime
     * @param key
     * @param format
     * @return
     */
    LocalDateTime localDateTime(String key, String format = null) {
        def value = super.get(key)
        if (value instanceof LocalDateTime) return value
        if (value.toString().contains('月')) local = Locale.SIMPLIFIED_CHINESE

        if (format) {
            return LocalDateTime.parse(value.toString(), DateTimeFormatter.ofPattern(format, local))
        }

        LocalDateTime date = null
        for (int i = 0; i < dateTimeFormats.size(); i++) {
            try {
                date = LocalDateTime.parse(value.toString(), DateTimeFormatter.ofPattern(dateTimeFormats[i], local))
            } catch (Exception e) {
                // e.printStackTrace()
            }
        }

        if (date) {
            return date
        } else {
            throw new Exception("no format for local date time : $value")
        }
    }
}