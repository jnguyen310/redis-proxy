package com.rescale.redisproxy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.*;

import static org.mockito.Mockito.*;

public class LRUCacheTest {
    private Jedis jedis;
    private JedisPool pool;
    private static final long CACHE_CAPACITY = 3;
    private static final long CACHE_EXPIRY = 300;
    private static final int CACHE_CONCURRENT_THREADS = 3;

    @Before
    public void setup() {
        jedis = mock(Jedis.class);
        pool = mock(JedisPool.class);
        when(pool.getResource()).thenReturn(jedis);

    }

    @Test
    public void testGetValueFromJedisServer() {
        LRUCache cache = new LRUCache(CACHE_CAPACITY, CACHE_EXPIRY, CACHE_CONCURRENT_THREADS, pool);
        when(jedis.get("1")).thenReturn("a");
        when(jedis.get("2")).thenReturn("b");
        when(jedis.get("3")).thenReturn("c");
        Assert.assertEquals("a", cache.get("1"));
        Assert.assertEquals("b", cache.get("2"));
        Assert.assertEquals("c", cache.get("3"));
        Assert.assertEquals(3, cache.getCacheSize());
    }

    @Test
    public void testInvalidKey() {
        LRUCache cache = new LRUCache(CACHE_CAPACITY, CACHE_EXPIRY, CACHE_CONCURRENT_THREADS, pool);
        Assert.assertNull(cache.get("1"));
    }


    @Test
    public void testCacheEvictionWhenFull() {
        LRUCache cache = new LRUCache(CACHE_CAPACITY, CACHE_EXPIRY, CACHE_CONCURRENT_THREADS, pool);
        when(jedis.get("1")).thenReturn("a");
        when(jedis.get("2")).thenReturn("b");
        when(jedis.get("3")).thenReturn("c");
        when(jedis.get("4")).thenReturn("d");
        cache.get("1");
        cache.get("2");
        cache.get("3");
        cache.get("4"); // This GET should evict key "1"
        Assert.assertEquals(3, cache.getCacheSize());
        Assert.assertEquals(false, cache.contains("1"));

    }

    @Test
    public void testCacheExpiryEviction() throws InterruptedException {
        LRUCache cache = new LRUCache(CACHE_CAPACITY, 1, CACHE_CONCURRENT_THREADS, pool);
        when(jedis.get("1")).thenReturn("a");
        cache.get("1");
        Thread.sleep(2000);
        cache.get("1");
        Mockito.verify(jedis, times(2)).get("1");
    }

    @Test
    public void testConcurrentGETs() {
        when(jedis.get("1")).thenReturn("a");
        when(jedis.get("2")).thenReturn("b");
        when(jedis.get("3")).thenReturn("c");
        when(jedis.get("4")).thenReturn("d");
        when(jedis.get("5")).thenReturn("e");

        ExecutorService service = Executors.newFixedThreadPool(10);
        LRUCache cache = new LRUCache(CACHE_CAPACITY, CACHE_EXPIRY, CACHE_CONCURRENT_THREADS, pool);

        for (int i = 0; i < 100; i++) {
            service.execute(() -> Assert.assertEquals("a", cache.get("1")));
            service.execute(() -> Assert.assertEquals("b", cache.get("2")));
            service.execute(() -> Assert.assertEquals("c", cache.get("3")));
            service.execute(() -> Assert.assertEquals("d", cache.get("4")));
            service.execute(() -> Assert.assertEquals("e", cache.get("5")));
        }
        service.shutdown();

        try {
            if(!service.awaitTermination(60, TimeUnit.SECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
