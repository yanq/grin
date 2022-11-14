package grin.datastore.validate

class Min extends Validator {
    Number value

    @Override
    boolean validate(String fieldName, Object fieldValue, grin.datastore.Entity<?> entity) {
        fieldValue >= value
    }
}
