package gun.datastore.validate

import gun.datastore.Entity

class MaxLength extends Validator {
    int length

    @Override
    boolean validate(String fieldName, Object fieldValue, Entity<?> entity) {
        fieldValue.toString().length() <= length
    }
}
