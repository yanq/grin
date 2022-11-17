package grin.datastore

import groovy.json.JsonSlurper
import groovy.util.logging.Slf4j

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * 转换器
 * 将数据转换成特定类型
 */
@Slf4j
class Transformer {
    static List<String> dateFormats = ['yyyy-MM-dd', 'yyyyMMdd']
    static List<String> dateTimeFormats = ['EEE MMM dd HH:mm:ss z yyyy', "EEE MMM d HH:mm:ss 'CST' yyyy",
                                           "yyyy-MM-dd'T'HH:mm:ss.SSSSSS", "yyyy-MM-dd HH:mm:ss.SSSSSS",
                                           "yyyy-MM-dd'T'HH:mm:ss.SSS", "yyyy-MM-dd HH:mm:ss.SSS",
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
        if (!aClass || !propName || propValue == null) return null
        try {
            Class propClass = aClass.getDeclaredField(propName)?.type
            return valueToType(propClass, propValue, formats)
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
    static valueToType(Class propClass, Object propValue, List<String> formats = []) {
        switch (propClass) {
            case boolean:
                return propValue.toString().toBoolean()
            case Boolean:
                return propValue.toString().toBoolean()
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
            case BigDecimal:
                return propValue.toString().toBigDecimal()
            case Date:
                return toDate(propValue, formats)
            case LocalDate:
                return toLoaclDate(propValue, formats)
            case LocalTime:
                return LocalTime.parse(propValue)
            case LocalDateTime:
                return toLocalDateTime(propValue, formats)
            case List:
                return (propValue instanceof List) ? propValue : new JsonSlurper().parseText(propValue.toString() ?: '[]')
            case Map:
                return (propValue instanceof Map) ? propValue : new JsonSlurper().parseText(propValue.toString() ?: '{}')
        }
        // 如果不是上述情况，返回原值
        return propValue
    }

    /**
     * Date 转换
     * @param propValue
     * @param formats
     * @return
     */
    static toDate(Object propValue, List formats = []) {
        if (propValue instanceof Timestamp) return new Date(propValue.time)
        if (propValue instanceof Date) return propValue
        if (!propValue.toString().trim()) return null

        List<String> list = formats ?: allFormats
        Date date = null
        for (int i = 0; i < list.size(); i++) {
            try {
                // 这里有个诡异的问题。如果没有 locale，脚本测试都可以，但 web 下就不行了。web 下会默认当前的 locale，如含中文，常规格式解析不了了
                date = new SimpleDateFormat(list[i], Locale.ENGLISH).parse(propValue)
            } catch (Exception e) {
                log.debug("Exception :${e.getMessage()}，format：${list[i]}")
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
    static toLoaclDate(Object propValue, List formats = []) {
        if (propValue instanceof LocalDate) return propValue
        if (!propValue.toString().trim()) return null

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
    static toLocalDateTime(Object propValue, List formats = []) {
        if (propValue instanceof LocalDateTime) return propValue
        if (!propValue.toString().trim()) return null

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
