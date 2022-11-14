package grin.datastore.validate

class Max extends Validator {
    Number value

    @Override
    boolean validate(String fieldName, Object fieldValue, grin.datastore.Entity<?> entity) {
        fieldValue <= value
    }
}
