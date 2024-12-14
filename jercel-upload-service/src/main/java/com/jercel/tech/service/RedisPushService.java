package com.jercel.tech.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

@Service
@Slf4j
public class RedisPushService {

    @Value("${redis.queue.name}")
    private String redisQueueName;

    private final Jedis jedis;

    public RedisPushService(@Value("${redis.host.name}") String redisHostName, @Value("${redis.port}") int redisPort) {
        jedis = new Jedis(redisHostName, redisPort);
    }

    public boolean enqueueTask(String message) {
        log.info("Inside enqueueTask");
        try {
            jedis.lpush(redisQueueName, message);
            return true;
        } catch (Exception e) {
            log.info("Exception in enqueueTask", e);
        }
        return false;
    }

    public void insertDeploymentStatus(String folderName) {
        jedis.set(folderName, "uploaded");
    }

    public String getValue(String key){
        return jedis.get(key);
    }

}
