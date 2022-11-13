package gun.datastore.validate

import gun.datastore.Entity

class Min extends Validator {
    Number value

    @Override
    boolean validate(String fieldName, Object fieldValue, Entity<?> entity) {
        fieldValue >= value
    }
}
