package grin.datastore.validate


import java.util.regex.Pattern

class Matcher extends Validator {
    Pattern value

    @Override
    boolean validate(String fieldName, Object fieldValue, grin.datastore.Entity<?> entity) {
        value.matcher(fieldValue).matches()
    }
}
