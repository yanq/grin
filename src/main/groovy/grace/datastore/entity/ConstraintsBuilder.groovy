package grace.datastore.entity
/**
 * 约束组装成 map，方便使用
 */
class ConstraintsBuilder {
    Class entityClass
    Map constraints = [:] // like : [title:[conditions:[blank:true, size:1..5], comment:字符串长度要处于 1 到 5 之间]]

    /**
     * 构建
     * @return
     */
    def build() {
        Closure c = entityClass[EntityApiImpl.CONSTRAINTS]
        if (c) {
            c = c.clone()
            c.delegate = this
            c.setResolveStrategy(Closure.DELEGATE_ONLY)
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
        [comment: { String comment -> constraints << [(name): conditions << [comment: comment]] }]
    }

    /**
     * 缺失方法处理，获取约束
     * @param name
     * @param args
     * @return
     */
    def methodMissing(String name, Object args) {
        buildConstraint(name, args[0])
    }

    /**
     * 静态方法，方便使用
     * @param entityClass
     * @return
     */
    static Map<String,Map> buildFromEntityClass(Class entityClass){
        ConstraintsBuilder builder = new ConstraintsBuilder(entityClass: entityClass)
        builder.build()
        return builder.constraints
    }
}
