package grace.common

import java.util.concurrent.ConcurrentHashMap

/**
 * flash scope
 * 两次请求作用域，主要用于跳转后，保留信息
 */
class FlashScope {
    static Map<String, Flash> flashes = new ConcurrentHashMap<>()

    /**
     * 获取 flash
     * @param key
     * @return
     */
    static getFlash(String key) {
        Flash flash = flashes.get(key)
        if (flash) return flash

        flash = new Flash()
        flashes.put(key, flash)
        return flash
    }

    /**
     * 下一个页面
     * @param key
     */
    static void next(String key) {
        flashes.get(key)?.next()
    }

    /**
     * flash
     * flash 数据存储及切换
     */
    static class Flash {
        private List<FlashValue> valueList = new ArrayList<>()

        /**
         * 获取值
         * @param key
         * @return
         */
        Object getProperty(String key) {
            valueList.find { it.key == key }?.value
        }

        /**
         * 设置值
         * @param key
         * @param value
         */
        void setProperty(String key, Object value) {
            def fv = valueList.find { it.key == key }
            if (fv) {
                fv.count = 0
                fv.value = value
            } else {
                fv = new FlashValue(key, value)
                valueList << fv
            }
        }

        /**
         * 切换页面
         * 保留上个的，清理过期的
         */
        void next() {
            valueList = valueList.findAll { it.count == 0 } //保留上次的
            valueList.each { it.count = it.count + 1 } //然后加一
        }
    }

    /**
     * flash value
     */
    class FlashValue {
        String key
        Object value
        int count = 0 //第一次创建，0，跳转后页面，1

        FlashValue(String key, Object value) {
            this.key = key
            this.value = value
        }
    }
}
