package gun.datastore.validate

import gun.datastore.Entity

class Blank extends Validator {
    Boolean value = true

    @Override
    boolean validate(String fieldName, Object fieldValue, Entity<?> entity) {
        value ? true : !!fieldValue.toString().trim()
    }
}
