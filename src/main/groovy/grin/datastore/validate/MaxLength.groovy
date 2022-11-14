package grin.datastore.validate

class MaxLength extends Validator {
    Integer value

    @Override
    boolean validate(String fieldName, Object fieldValue, grin.datastore.Entity<?> entity) {
        fieldValue.toString().length() <= value
    }
}
