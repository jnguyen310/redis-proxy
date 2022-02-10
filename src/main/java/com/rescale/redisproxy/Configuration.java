package com.rescale.redisproxy;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {
    private Properties properties;

    public Configuration(String propertyFileName) throws IOException {
        InputStream is = getClass().getClassLoader()
                .getResourceAsStream(propertyFileName);
        this.properties = new Properties();
        this.properties.load(is);
    }
    public String getProperty(String propertyName) {
        return this.properties.getProperty(propertyName);
    }

    public String getRedisServerAddress() {
        if( System.getenv("REDIS_SERVER_ADDR") == null) {
            System.out.println("Could not read config proerty");
            return properties.getProperty("REDIS_SERVER_ADDR");
        } else {
            return System.getenv("REDIS_SERVER_ADDR");
        }
    }

    public int getRedisServerPort() {
        return System.getenv("REDIS_SERVER_PORT") == null ?
                Integer.parseInt(properties.getProperty("REDIS_SERVER_PORT")) : Integer.parseInt(System.getenv("REDIS_SERVER_PORT"));
    }

    public long getCacheExpiryTime() {
        return System.getenv("CACHE_EXPIRY_TIME") == null ?
                Long.parseLong(properties.getProperty("CACHE_EXPIRY_TIME")) : Long.parseLong(System.getenv("CACHE_EXPIRY_TIME"));
    }

    public long getCacheCapacity() {
        return System.getenv("CACHE_CAPACITY") == null ?
                Long.parseLong(properties.getProperty("CACHE_CAPACITY")) : Long.parseLong(System.getenv("CACHE_CAPACITY"));
    }

    public int getConcurrencyLimit() {
        return System.getenv("CONCURRENCY_LIMIT") == null ?
                Integer.parseInt(properties.getProperty("CONCURRENCY_LIMIT")) : Integer.parseInt(System.getenv("CONCURRENCY_LIMIT"));
    }

    public String getProxyAddress() {
        return System.getenv("PROXY_ADDR") == null ? properties.getProperty("PROXY_ADDR") : System.getenv("PROXY_ADDR");
    }

    public int getProxyPort() {
        return System.getenv("PROXY_PORT") == null ?
                Integer.parseInt(properties.getProperty("PROXY_PORT")) : Integer.parseInt(System.getenv("PROXY_PORT"));
    }
}
