package cool.yunlong.mall.activity.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yunlong
 * @since 2022/7/2 18:46
 */
public class CacheHelper {
    /**
     * 缓存容器
     */
    private final static Map<String, Object> cacheMap = new ConcurrentHashMap<>();

    /**
     * 加入缓存
     *
     * @param key         键
     * @param cacheObject 值
     */
    public static void put(String key, Object cacheObject) {
        cacheMap.put(key, cacheObject);
    }

    /**
     * 获取缓存
     *
     * @param key 键
     * @return 值
     */
    public static Object get(String key) {
        return cacheMap.get(key);
    }

    /**
     * 清除缓存
     *
     * @param key 键
     * @return 值
     */
    public static void remove(String key) {
        cacheMap.remove(key);
    }

    public static synchronized void removeAll() {
        cacheMap.clear();
    }
}
