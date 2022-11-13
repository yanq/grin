package gun.datastore.validate

import gun.datastore.Entity

import java.util.regex.Pattern

class Matcher extends Validator {
    Pattern value

    @Override
    boolean validate(String fieldName, Object fieldValue, Entity<?> entity) {
        value.matcher(fieldValue).matches()
    }
}
