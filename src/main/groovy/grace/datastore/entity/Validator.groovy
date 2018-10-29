package grace.datastore.entity

import groovy.util.logging.Slf4j

/**
 * 验证器
 * 验证约束规则
 */
@Slf4j
class Validator {
    static constraintKeys = ['size','inList']

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
            case 'size':
                return validateSize(value, constraint.value)
            case 'inList':
                return validateInList(value, constraint.value)
        }

        return false
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
}
