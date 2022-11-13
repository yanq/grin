package gun.datastore.validate

import gun.datastore.Entity

class InList extends Validator {
    List value

    @Override
    boolean validate(String fieldName, Object fieldValue, Entity<?> entity) {
        fieldValue in value
    }
}
