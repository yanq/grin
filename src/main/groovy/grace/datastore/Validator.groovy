package grace.datastore

import groovy.util.logging.Slf4j

/**
 * 验证器
 * 验证约束规则
 */
@Slf4j
class Validator {
    static constraintKeys = ['size']

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
        }

        return false
    }

    /**
     * 验证 size
     * @param value
     * @param size
     * @return
     */
    static boolean validateSize(String value, Range size) {
        return size.contains(value.size())
    }
}
