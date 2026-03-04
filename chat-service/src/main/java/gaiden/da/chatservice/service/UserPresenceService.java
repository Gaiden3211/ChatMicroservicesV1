package gaiden.da.chatservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserPresenceService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STATUS_PREFIX = "user:presence:";

    public void setOnline(String userId) {
        redisTemplate.opsForValue().set(STATUS_PREFIX + userId, "ONLINE", 24, TimeUnit.HOURS);
    }

    public void setOffline(String userId) {
        redisTemplate.delete(STATUS_PREFIX + userId);
    }

    public boolean isOnline(String userId) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().get(STATUS_PREFIX + userId));
    }


    public Map<String, String> getStatuses(List<String> userIds) {
        Map<String, String> statuses = new HashMap<>();

        for (String userId : userIds) {

            boolean isOnline = Boolean.TRUE.equals(redisTemplate.hasKey(STATUS_PREFIX + userId));
            statuses.put(userId, isOnline ? "ONLINE" : "OFFLINE");
        }

        return statuses;
    }
}
