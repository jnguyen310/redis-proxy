package com.rescale.redisproxy;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class ProxyServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyServer.class);

    public static void main(String[] args) throws IOException {
        Configuration config = new Configuration("settings.properties");

        JedisPool pool = new JedisPool(config.getRedisServerAddress(), config.getRedisServerPort());
        LRUCache cache = new LRUCache(config.getCacheCapacity(),
                config.getCacheExpiryTime(),
                config.getConcurrencyLimit(), pool);

        HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getByName(config.getProxyAddress()),
                config.getProxyPort()), 0);
        //Create the context for the server.
        server.createContext("/", new RequestHandler(cache));
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        LOGGER.info("Server is ready to accept requests at {}:{}", config.getProxyAddress(), config.getProxyPort());
    }
}
