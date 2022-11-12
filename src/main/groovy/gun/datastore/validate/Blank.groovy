package gun.datastore.validate

import gun.datastore.Entity

class Blank extends Validator {
    boolean canBlank = true

    @Override
    boolean validate(String fieldName, Object fieldValue, Entity<?> entity) {
        canBlank ? true : !!fieldValue.toString().trim()
    }
}
