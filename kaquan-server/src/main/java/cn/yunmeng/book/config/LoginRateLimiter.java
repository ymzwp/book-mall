package cn.yunmeng.book.config;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录限流器：同 IP 1分钟内最多尝试 5 次
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;      // 最大尝试次数
    private static final long WINDOW_MS = 60_000;   // 时间窗口 1 分钟

    private final Map<String, Window> cache = new ConcurrentHashMap<>();

    /** 检查是否允许尝试，返回 true 表示放行 */
    public boolean allow(String ip) {
        long now = System.currentTimeMillis();
        Window w = cache.compute(ip, (k, v) -> {
            if (v == null || now - v.start > WINDOW_MS) {
                return new Window(now, 1);
            }
            v.count++;
            return v;
        });
        return w.count <= MAX_ATTEMPTS;
    }

    /** 登录成功后清除计数 */
    public void clear(String ip) {
        cache.remove(ip);
    }

    private static class Window {
        long start;
        int count;
        Window(long start, int count) { this.start = start; this.count = count; }
    }
}
