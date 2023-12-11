package grin.datastore

/**
 * 提供实体类的基本功能
 */
trait Entity<D> {
    static List<String> transients = []
    static Map<String, Object> mapping = [:]
    static Map<String, List> constraints = [:]

    Map<String, String> errors = [:]

    /**
     * get
     * this 返回的是实现的类，这是 trait 的特性。
     * @param id
     * @return
     */
    static D get(Serializable id, List<String> selects = []) {
        if (!id) return null
        where([id: id]).get(selects)
    }


    /**
     * get all
     * 获取多条
     * @param ids
     * @return
     */
    static List<D> getAll(List<Serializable> ids, List<String> selects = []) {
        if (!ids) return []
        where([id: ids]).list([:], selects)
    }

    /**
     * 列表
     * @param params
     * @return
     */
    static List<D> list(Map params = null, List<String> selects = []) {
        where('').list(params, selects)
    }

    /**
     * 计数
     * @return
     */
    static int count(List<String> selects = []) {
        where('').count(selects)
    }

    static int countDistinct(List<String> selects = []) {
        where('').countDistinct(selects)
    }

    /**
     * 保存，更新或者插入
     * @return
     */
    D save(boolean validate = false) {
        if (validate) this.validate()
        if (this.errors) throw new Exception("entity save fail,errors: ${this.errors}")
        EntityImpl.save(this)
    }

    /**
     * 删除
     * @return
     */
    boolean delete() {
        EntityImpl.delete(this)
    }

    /**
     * 刷新
     * @return
     */
    boolean refresh() {
        EntityImpl.refresh(this)
    }

    /**
     * where 自定义条件查询
     * @param sql
     */
    static EntityImpl.Where<D> where(String sql, List params = []) {
        new EntityImpl.Where(whereSql: sql, params: params ?: [], entityClass: this)
    }

    static EntityImpl.Where<D> where(String sql, Object... params) {
        where(sql, params.toList())
    }

    static EntityImpl.Where<D> where(Map kvs) {
        def params = []
        def sql = kvs.keySet()
                .collect {
                    def k = it
                    def v = kvs[it]
                    if (v instanceof List) {
                        params.addAll(v.collect { Transformer.toType(this, k, it) })
                        return "${Utils.findColumnName(this, it)} in (${v.collect { '?' }.join(',')})"
                    } else {
                        params.add(Transformer.toType(this, k, v))
                        return "${Utils.findColumnName(this, it)} = ?"
                    }
                }
                .join(' and ')
        where(sql, params)
    }

    /**
     * 验证约束
     * @return
     */
    boolean validate() {
        if (errors) return false // 已经有错误了，可能来自数据绑定时的类型转换错误
        EntityImpl.validate(this)
    }

    /**
     * 属性列表
     * @return
     */
    static List<String> getProps() {
        Utils.findPropertiesToPersist(this)
    }

    /**
     * 转换成 Map
     * @return
     */
    Map toMap(List<String> includes = []) {
        Utils.toMap(includes, this)
    }

    /**
     * 绑定参数,实例
     * @param params
     * @return
     */
    D bind(Map params) {
        EntityImpl.bind(this, params)
    }

    /**
     * 从参数产生实例
     * @param params
     * @return
     */
    static from(Map params) {
        EntityImpl.bind(this, params)
    }

    /**
     * 加载关联
     * @param names
     * @return
     */
    def fetch(String... names) {
        EntityFetch.fetch(this, names)
    }
}