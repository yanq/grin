package grin.datastore.validate
/**
 * 验证器
 */
abstract class Validator {
    Object value
    String message

    abstract boolean validate(String fieldName, Object fieldValue, grin.datastore.Entity<?> entity)
}
