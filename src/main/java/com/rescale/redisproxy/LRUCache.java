package com.rescale.redisproxy;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LRUCache {
    private static LoadingCache<String, String> CACHE;
    private static final Logger LOGGER = LoggerFactory.getLogger(LRUCache.class);

    public LRUCache(long cacheCapacity, long cacheExpiryTime, int concurrencyLevel, JedisPool pool) {
        CACHE = CacheBuilder.newBuilder()
                .maximumSize(cacheCapacity)
                .expireAfterWrite(cacheExpiryTime, TimeUnit.SECONDS)
                .concurrencyLevel(concurrencyLevel)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(String s) {
                        try (Jedis jedis = pool.getResource()) {
                            return jedis.get(s);
                        }
                    }
                });
    }

    public String get(String key) {
        try {
            String value =  CACHE.get(key);
            LOGGER.info("Requested key {}, received value {}", key, value);
            return value;
        } catch (CacheLoader.InvalidCacheLoadException | ExecutionException e) {
            LOGGER.warn("There was no value for the key '{}'", key);
            return null;
        }
    }

    public long getCacheSize() {
        return CACHE.size();
    }

    public boolean contains(String key) {
        return CACHE.getIfPresent(key) != null;
    }

}
