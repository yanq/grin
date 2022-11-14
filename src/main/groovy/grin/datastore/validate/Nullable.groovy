package grin.datastore.validate

class Nullable extends Validator {
    Boolean value = true

    @Override
    boolean validate(String fieldName, Object fieldValue, grin.datastore.Entity<?> entity) {
        value ? true : fieldValue != null
    }
}
