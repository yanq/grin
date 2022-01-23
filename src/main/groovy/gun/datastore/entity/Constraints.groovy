package gun.datastore.entity
/**
 * 约束组装成 map，方便使用
 */
class Constraints {
    Class entityClass
    Map<String, Map> constraints = [:] // like : [title:[conditions:[blank:true, size:1..5], comment:字符串长度要处于 1 到 5 之间]]

    /**
     * 构建
     * @return
     */
    def build() {
        Closure c = entityClass[EntityImpl.CONSTRAINTS]
        if (c) {
            c = c.clone()
            c.delegate = this
            c.setResolveStrategy(Closure.DELEGATE_FIRST)
            c.call()
        }
    }

    /**
     * 约束定义 dsl 的实现
     * @param name
     * @param conditions
     * @return
     */
    private buildConstraint(String name, Map conditions) {
        constraints << [(name): conditions]
        constraints[(name)].comment = { String comment -> constraints[(name)].comment = comment }
        return constraints[(name)]
    }

    /**
     * 缺失方法处理，获取约束
     * @param name
     * @param args
     * @return
     */
    def methodMissing(String name, Object args) {
        buildConstraint(name, args ? args[0] : [:])
    }

    /**
     * 静态方法，方便使用
     * @param entityClass
     * @return
     */
    static Map<String, Map> buildToMapFromEntityClass(Class entityClass) {
        Constraints builder = new Constraints(entityClass: entityClass)
        builder.build()
        builder.constraints.each {
            if (it.value.comment instanceof Closure) it.value.comment = ''
        }
        return builder.constraints
    }
}
