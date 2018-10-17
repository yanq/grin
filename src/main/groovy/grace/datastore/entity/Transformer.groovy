package grace.datastore.entity

import groovy.util.logging.Slf4j

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 转换器
 * 将数据转换成特定类型
 */
@Slf4j
class Transformer {
    static List<String> dateFormats = ['yyyy-MM-dd', 'yyyyMMdd']
    static List<String> dateTimeFormats = ['EEE MMM dd HH:mm:ss z yyyy',
                                           "yyyy-MM-dd'T'HH:mm:ss.S", "yyyy-MM-dd HH:mm:ss.S",
                                           "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss",
                                           "yyyy-MM-dd'T'HH:mm", "yyyy-MM-dd HH:mm"]
    static List<String> allFormats = dateTimeFormats + dateFormats

    /**
     * 类属性转换
     * @param aClass
     * @param propName
     * @param propValue
     * @param formats
     * @return
     */
    static toType(Class aClass, String propName, Object propValue, List<String> formats = []) {
        if (!aClass || !propName || !propValue) return null
        try {
            Class propClass = aClass.getDeclaredField(propName)?.type
            return toType(propClass, propValue, formats)
        } catch (Exception e) {
            log.debug("Exception :${e.getMessage()}")
            // e.printStackTrace()
        }
        return propValue
    }

    /**
     * 转换
     * @param propClass
     * @param propValue
     * @param formats
     * @return
     */
    static toType(Class propClass, Object propValue, List<String> formats = []) {
        switch (propClass) {
            case int:
                return propValue.toString().toInteger()
            case Integer:
                return propValue.toString().toInteger()
            case long:
                return propValue.toString().toLong()
            case Long:
                return propValue.toString().toLong()
            case float:
                return propValue.toString().toFloat()
            case Float:
                return propValue.toString().toFloat()
            case double:
                return propValue.toString().toDouble()
            case Double:
                return propValue.toString().toDouble()
            case Date:
                return toDate(propValue, formats)
            case LocalDate:
                return toLoaclDate(propValue, formats)
            case LocalDateTime:
                return toLocalDateTime(propValue, formats)
        }
        //如果不是上述情况，返回原值
        return propValue
    }

    /**
     * Date 转换
     * @param propValue
     * @param formats
     * @return
     */
    static toDate(Object propValue, List formats) {
        if (propValue instanceof Date) return propValue

        List<String> list = formats ?: allFormats
        Date date = null
        for (int i = 0; i < list.size(); i++) {
            try {
                date = new SimpleDateFormat(list[i]).parse(propValue)
            } catch (Exception e) {
                log.debug("Exception :${e.getMessage()}")
                // e.printStackTrace()
            }
            if (date) return date
        }

        return propValue
    }

    /**
     * LocalDate 转换
     * @param propValue
     * @param formats
     * @return
     */
    static toLoaclDate(Object propValue, List formats) {
        if (propValue instanceof LocalDate) return propValue

        List<String> list = formats ?: dateFormats
        LocalDate date = null
        for (int i = 0; i < list.size(); i++) {
            try {
                date = LocalDate.parse(propValue.toString(), DateTimeFormatter.ofPattern(list[i]))
            } catch (Exception e) {
                log.debug("Exception :${e.getMessage()}")
                // e.printStackTrace()
            }
            if (date) return date
        }

        return propValue
    }

    /**
     * LocalDateTime 转换
     * @param propValue
     * @param formats
     * @return
     */
    static toLocalDateTime(Object propValue, List formats) {
        if (propValue instanceof LocalDateTime) return propValue

        List<String> list = formats ?: dateTimeFormats
        LocalDateTime date = null
        for (int i = 0; i < list.size(); i++) {
            try {
                date = LocalDateTime.parse(propValue.toString(), DateTimeFormatter.ofPattern(list[i]))
            } catch (Exception e) {
                log.debug("Exception :${e.getMessage()}")
                // e.printStackTrace()
            }
            if (date) return date
        }

        return propValue
    }
}
