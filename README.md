#Overview
This is a Redis proxy that accepts concurrent HTTP GET requests for requesting key-value pairs in a Redis datastore.
The proxy provides a LRU cache with a configurable cache capacity, concurrent client limit, and expiry time for values.


#Code Implementation
The specification requires an HTTP proxy server that handles concurrent GET requests for retrieving values from a Redis database 
server, storing it in an LRU cache that is configurable for number of concurrent reads, cache expiry time, and cache capacity.

In this implementation, I used the built-in HTTPServer library as it already supports asynchronous requests. For caching,
Google's Guava library provides all the features required by the specification (cache sizing, cache expiry, concurrency).
Lastly, for interfacing with Redis, I used Jedis as it is the most popular client for Java integration. Using these existing 
libraries that supports the specifications reduces maintenance overheads of in-house implementations.

###Below is the list of the Java classes and their features:
- Configuration.java - Retrieves the configuration settings for the application. The settings can either be set in the 
settings.properties files before building, or set as environment variables in a docker compose yml. The supported configuration parameters are:
    - REDIS_SERVER_ADDR
    - REDIS_SERVER_PORT
    - CACHE_EXPIRY_TIME (in seconds)
    - CACHE_CAPACITY
    - CONCURRENCY_LIMIT
    - PROXY_ADDR
    - PROXY_PORT
- LRUCache.java - Uses Guava to handle caching of values based on a key. The cache handles a specified cache size, expiry,  
and concurrently level. The cache configuration can be set using CACHE_EXPIRY_TIME, CACHE_CAPACITY and CONCURRENCY_LIMIT.
- ProxyServer.java - HTTP server that is able to accept asynchronous requests. The address and port of the server can be 
configured using PROXY_ADDR and PROXY_PORT.
- RequestHandler.java - This class is a handler for all requests to the proxy server. Currently it only supports GET requests
but additional requests can be implemented as needed.




#Algorithmic complexity of caching operations
Guava uses a ConcurrentHashMap so operations would have an average time complexity of O(1).

#Running proxy and tests
> make test

#Time spent
- Reading specification, researching libraries, setting up environment - 1.5h
- Implementing server - 0.75h
- Implementing cache - 1h
- Implementing configuration class - 0.5h
- Creating and building via Docker - 2h
- Writing and testing test cases and debugging - 2.5h

#List of requirements met
All core requirements and bonus requirements were met.