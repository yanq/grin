package grin.datastore.validate

class Blank extends Validator {
    Boolean value = true

    @Override
    boolean validate(String fieldName, Object fieldValue, grin.datastore.Entity<?> entity) {
        value ? true : !!fieldValue.toString().trim()
    }
}
