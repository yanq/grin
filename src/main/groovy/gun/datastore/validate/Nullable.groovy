package gun.datastore.validate

import gun.datastore.Entity

class Nullable extends Validator {
    boolean canNull = true

    @Override
    boolean validate(String fieldName, Object fieldValue, Entity<?> entity) {
        canNull ? true : fieldValue != null
    }
}
