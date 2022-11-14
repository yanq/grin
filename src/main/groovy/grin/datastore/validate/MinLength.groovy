package grin.datastore.validate
/**
 * 字符串最小长度
 */
class MinLength extends Validator {
    Integer value

    @Override
    boolean validate(String fieldName, Object fieldValue, grin.datastore.Entity<?> entity) {
        fieldValue.toString().length() >= value
    }
}
