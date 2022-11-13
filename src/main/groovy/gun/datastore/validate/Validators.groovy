package gun.datastore.validate

import gun.datastore.Entity

import java.util.regex.Pattern

/**
 * 验证器工具类
 */
class Validators {
    static Validator nullable(boolean canNull = true, String message = '') {
        new Nullable(canNull: canNull, message: message ?: "${canNull ? '可以为空' : '不可以为空'}")
    }

    static Validator blank(boolean canBlank = true, String message = '') {
        new Blank(canBlank: canBlank, message: message ?: "${canBlank ? '可以为空字符串' : '不可以为空字符串'}")
    }

    static Validator minLength(int length, String message = "字符串长度不能小于 $length") {
        new MinLength(length: length, message: message)
    }

    static Validator maxLength(int length, String message = "字符串长度不能大于 $length") {
        new MaxLength(length: length, message: message)
    }

    static Validator matches(String pattern, String message = "字符串模式不匹配") {
        matches(Pattern.compile(pattern))
    }

    static Validator matches(Pattern pattern, String message = "字符串模式不匹配") {
        new Matcher(pattern: pattern, message: message)
    }

    static Validator min(Number number, String message = "不能小于 $number") {
        new Min(number: number, message: message)
    }

    static Validator max(Number number, String message = "不能大于 $number") {
        new Max(number: number, message: message)
    }

    static Validator inList(List list, String message = "请在 ${list} 中选择") {
        new InList(list: list, message: message)
    }

    static Validator validator(String message, Closure closure) {
        return new Validator() {
            @Override
            boolean validate(String fieldName, Object fieldValue, Entity<?> entity) {
                super.message = message // 这里若用 this 无法正确赋值
                closure.call(fieldName, fieldValue, entity)
            }
        }
    }
}
