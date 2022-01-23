package gun.common

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
    static class Flash extends AbstractMap<String, Object> {
        private Set<FlashValue> flashValues = new HashSet<>()

        /**
         * 切换页面
         * 保留上个的，清理过期的
         */
        void next() {
            flashValues = flashValues.findAll { it.count == 0 } //保留上次的
            flashValues.each { it.count = it.count + 1 } //然后加一
        }

        /**
         * 获取值
         * @param key
         * @return
         */
        Object get(Object key) {
            flashValues.find { it.key == key }?.value
        }

        /**
         * 设置值
         * @param key
         * @param value
         */
        Object put(Object key, Object value) {
            def fv = flashValues.find { it.key == key }
            if (fv) {
                fv.count = 0
                fv.value = value
            } else {
                fv = new FlashValue(key, value)
                flashValues << fv
            }
        }

        /**
         * set
         * @return
         */
        @Override
        Set<Entry> entrySet() {
            return flashValues
        }

        /**
         * flash value
         */
        class FlashValue extends AbstractMap.SimpleEntry<String, Object> {
            int count = 0 //第一次创建，0，跳转后页面，1

            FlashValue(String key, Object value) {
                super(key, value)
            }
        }
    }
}
