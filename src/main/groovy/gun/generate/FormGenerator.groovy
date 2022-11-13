package gun.generate

import gun.datastore.Entity
import gun.datastore.EntityImpl

/**
 * 产生表单
 */
class FormGenerator extends Generator {
    static types = [(String.name): 'text', (Long.name): 'number', (Integer.name): 'number', (Date.name): 'datetime']

    static String generateForm(Class entityClass) {
        List<String> props = EntityImpl.findPropertiesToPersist(entityClass) - 'id'
        Map constraints = entityClass.constraints

        List<String> result = []
        props.each {
            Class propClass = entityClass.getDeclaredField(it)?.type
            if (propClass in [List,Map]) return //有些类型明确处理不了的，略过
            String type = types[propClass.name] ?: 'text'
            if (propClass) {
                result << generateItem(entityClass, it, type, constraints[it]) ?: ''
            }
        }
        return result.join('\n')
    }

    static String generateItem(Class entityClass, String propName, String propType, Map constraints) {
        if (entityClass.getDeclaredField(propName).type.interfaces.contains(Entity)) {// entity
            entity(entityClass, propName, propType, constraints)
        } else if (constraints?.find { it.class.simpleName == 'InList' }) { // select
            select(entityClass, propName, propType, constraints)
        } else {// text
            input(entityClass, propName, propType, constraints)
        }
    }

    static input(Class entityClass, String propName, String propType, Map constraints) {
        generate('form/input.html', [entityClass: entityClass, propName: propName, propType: propType, constraints: constraints])
    }

    static select(Class entityClass, String propName, String propType, Map constraints) {
        generate('form/select.html', [entityClass: entityClass, propName: propName, propType: propType, constraints: constraints])
    }

    static entity(Class entityClass, String propName, String propType, Map constraints) {
        generate('form/entity.html', [entityClass: entityClass, propName: propName, propType: propType, constraints: constraints])
    }
}
