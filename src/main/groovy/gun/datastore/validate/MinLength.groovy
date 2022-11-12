package gun.datastore.validate

import gun.datastore.Entity

/**
 * 字符串最小长度
 */
class MinLength extends Validator {
    int length

    @Override
    boolean validate(String fieldName, Object fieldValue, Entity<?> entity) {
        fieldValue.toString().length() >= length
    }
}
