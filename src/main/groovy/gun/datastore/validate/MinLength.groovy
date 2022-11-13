package gun.datastore.validate

import gun.datastore.Entity

/**
 * 字符串最小长度
 */
class MinLength extends Validator {
    Integer value

    @Override
    boolean validate(String fieldName, Object fieldValue, Entity<?> entity) {
        fieldValue.toString().length() >= value
    }
}
