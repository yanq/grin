package grace.generate


import grace.datastore.entity.EntityImpl

/**
 * 产生表单
 */
class FormGenerator extends Generator {
    static types = [(String.name): 'text', (Long.name): 'number', (Integer.name): 'number', (Date.name): 'datetime']

    static String generateForm(Class entityClass) {
        List<String> props = EntityImpl.findPropertiesToPersist(entityClass)
        Map constraints = EntityImpl.getConstraintMap(entityClass)

        String result = ""
        props.each {
            Class propClass = entityClass.getDeclaredField(it)?.type
            String type = types[propClass.name] ?: 'text'
            if (propClass) {
                result += generateItem(entityClass, it, type, constraints[it]) ?: ''
            }
        }
        return result
    }

    static String generateItem(Class entityClass, String propName, String propType, Map constraints) {
        if (constraints?.inList || constraints?.range) { //select
            select(entityClass, propName, propType, constraints)
        } else {
            input(entityClass, propName, propType, constraints)
        }
    }

    static input(Class entityClass, String propName, String propType, Map constraints) {
        generate('form/input.html', [entityClass: entityClass, propName: propName, propType: propType, constraints: constraints])
    }

    static select(Class entityClass, String propName, String propType, Map constraints) {
        generate('form/select.html', [entityClass: entityClass, propName: propName, propType: propType, constraints: constraints])
    }
}
