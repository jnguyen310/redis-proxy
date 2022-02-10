package integration;

import com.rescale.redisproxy.Configuration;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JedisProxyServerIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(JedisProxyServerIT.class);

    private static final String KEY_PARAM = "?key=";
    private static Configuration config;

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @BeforeClass
    public static void setup() throws IOException {
        config = new Configuration("settings.properties");
        JedisPool pool = new JedisPool(config.getRedisServerAddress(), config.getRedisServerPort());
        Jedis jedis = pool.getResource();
        jedis.set("1", "a");
        jedis.set("2", "b");
        jedis.set("3", "c");
        jedis.set("4", "d");
    }

    @Test
    public void testConcurrentGETRequests() {
        ExecutorService service = Executors.newFixedThreadPool(10);

        for (int i = 0; i < 100; i++) {
            service.execute(() -> Assert.assertEquals("a", sendGet("1")));
            service.execute(() -> Assert.assertEquals("b", sendGet("2")));
            service.execute(() -> Assert.assertEquals("c", sendGet("3")));
            service.execute(() -> Assert.assertEquals("d", sendGet("4")));
        }
    }

    @Test
    public void testSingleGETRequests() {
        Assert.assertEquals("a", sendGet("1"));
    }

    private String sendGet(String key) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://" + config.getProxyAddress() + ":" + config.getProxyPort() + KEY_PARAM + key))
                .build();
        CompletableFuture<HttpResponse<String>> responseFuture =
                httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());

        return responseFuture.join().body();
    }


}
