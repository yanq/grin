package gun.datastore.validate

import gun.datastore.Entity

class Max extends Validator {
    Number value

    @Override
    boolean validate(String fieldName, Object fieldValue, Entity<?> entity) {
        fieldValue <= value
    }
}
