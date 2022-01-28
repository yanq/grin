package gun.datastore

import groovy.util.logging.Slf4j

import java.util.regex.Pattern

/**
 * 验证器
 * 验证约束规则
 */
@Slf4j
class Validator {
    static constraintKeys = ['max', 'min', 'range', 'matches', 'size', 'inList']

    /**
     * 验证约束
     * @param value
     * @param constraint
     * @return
     */
    static boolean validate(Object value, Map.Entry constraint) {
        if (!(constraint.key in constraintKeys)) {
            log.error("不存在的约束 ：$constraint")
            throw new Exception("约束 $constraint 不存在，请检查拼写")
        }

        switch (constraint.key) {
            case 'max':
                return validateMax(value, constraint.value)
            case 'min':
                return validateMin(value, constraint.value)
            case 'range':
                return validateRange(value, constraint.value)
            case 'matches':
                return validateMatches(value, constraint.value)
            case 'size':
                return validateSize(value, constraint.value)
            case 'inList':
                return validateInList(value, constraint.value)
        }

        return false
    }

    /**
     * max
     * @param value
     * @param max
     * @return
     */
    static boolean validateMax(Object value, Object max) {
        return value <= max
    }

    /**
     * min
     * @param value
     * @param min
     * @return
     */
    static boolean validateMin(Object value, Object min) {
        return value >= min
    }

    /**
     * range
     * @param value
     * @param range
     * @return
     */
    static boolean validateRange(Object value, Range range) {
        return range.containsWithinBounds(value)
    }

    /**
     * matches
     * @param value
     * @param regular
     * @return
     */
    static boolean validateMatches(Object value, Object regular) {
        if (regular instanceof String) return Pattern.matches(regular, value)
        if (regular instanceof Pattern) return regular.matcher(value).matches()
        throw new Exception('matches value must a String or a Pattern')
    }

    /**
     * in list
     * @param value
     * @param list
     * @return
     */
    static boolean validateInList(Object value, List list) {
        list.contains(value)
    }

    /**
     * 验证 size
     * @param value
     * @param range
     * @return
     */
    static boolean validateSize(String value, Range range) {
        return range.containsWithinBounds(value.size())
    }

    static boolean validateSize(String value, Integer size) {
        return size == value.length()
    }
}
