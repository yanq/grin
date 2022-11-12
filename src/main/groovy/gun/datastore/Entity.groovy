package gun.datastore

/**
 * 提供实体类的基本功能
 */
trait Entity<D> {
    Map<String, String> errors = [:]

    /**
     * get
     * this 返回的是实现的类，这是 trait 的特性。
     * @param id
     * @return
     */
    static D get(Serializable id, String selects = '*') {
        if (!id) return null
        EntityImpl.get(this, id, selects)
    }

    /**
     * get all
     * 获取多条
     * @param ids
     * @return
     */
    static List<D> getAll(List<Serializable> ids, String selects = '*') {
        if (!ids) return []
        new EntityImpl.Where(whereSql: "id in (${ids.collect { '?' }.join(',')})", params: ids, entityClass: this).list([:], selects)
    }

    static List<D> getAll(Serializable... ids) {
        if (!ids) return []
        getAll(ids.toList())
    }

    /**
     * 列表
     * @param params
     * @return
     */
    static List<D> list(Map params = null, String selects = '*') {
        EntityImpl.list(this, params, selects)
    }

    /**
     * 计数
     * @return
     */
    static int count(String selects = '*') {
        EntityImpl.count(this, selects)
    }

    /**
     * 保存，更新或者插入
     * @return
     */
    D save(boolean validate = false) {
        if (validate) this.validate()
        if (this.errors) return null
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
        where(kvs.keySet().collect { "${EntityImpl.toDbName(it)}=?" }.join(' and '), kvs.values().toList())
    }

    /**
     * 约束 Map
     * @return
     */
    static Map getConstraintMap() {
        EntityImpl.getConstraintMap(this)
    }

    /**
     * 验证约束
     * @return
     */
    boolean validate() {
        EntityImpl.validate(this)
    }

    /**
     * 属性列表
     * @return
     */
    static List<String> getProps() {
        EntityImpl.findPropertiesToPersist(this)
    }

    /**
     * 转换成 Map
     * @return
     */
    Map toMap(List<String> excludes) {
        EntityImpl.toMap(excludes, this)
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