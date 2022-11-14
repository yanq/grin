package grin.datastore.validate

class InList extends Validator {
    List value

    @Override
    boolean validate(String fieldName, Object fieldValue, grin.datastore.Entity<?> entity) {
        fieldValue in value
    }
}
