package grace.datastore

import groovy.util.logging.Slf4j

/**
 * 验证器
 * 验证约束规则
 */
@Slf4j
class Validator {
    static validators = ['blank', 'size']

    static boolean validate(Object value, Map.Entry validator) {
        if (!(validator.key in validators)) {
            log.error("不存在的约束 ：$validator")
            throw new Exception("约束 $validator 不存在，请检查拼写")
        }

        switch (validator.key) {
            case 'blank':
                return validateBlank(value,validator.value)
            case 'size':
                return validateSize(value, validator.value)
        }

        return false
    }

    static boolean validateBlank(String value, boolean blank) {
        !value ? blank : true
    }

    static boolean validateSize(String value, Range size) {
        return size.contains(value.size())
    }


}
