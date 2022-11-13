package gun.datastore.validate

import gun.datastore.Entity

class Nullable extends Validator {
    Boolean value = true

    @Override
    boolean validate(String fieldName, Object fieldValue, Entity<?> entity) {
        value ? true : fieldValue != null
    }
}
