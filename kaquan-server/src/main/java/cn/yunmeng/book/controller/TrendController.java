package cn.yunmeng.book.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;
import java.util.*;

/** 价格走势：从订单表统计真实数据 */
@RestController
@RequestMapping("/trend")
public class TrendController {

    @Resource private JdbcTemplate jdbc;

    /** 获取各商品近 N 天的日均价格走势 */
    @GetMapping("/price")
    public Map<String, Object> price(@RequestParam(defaultValue = "30") int days) {
        // 查询最近 N 天有订单的商品最近价格
        String sql = """
            SELECT DATE(o.create_time) AS dt, g.goods_name AS name, AVG(o.order_price) AS avg_price
            FROM orders o JOIN goods g ON o.goods_id = g.id
            WHERE o.create_time >= DATE_SUB(CURDATE(), INTERVAL ? DAY) AND o.status != 'refunded'
            GROUP BY dt, g.goods_name ORDER BY dt, name
            """;
        List<Map<String, Object>> rows = jdbc.queryForList(sql, days);

        // 组织为 { dates: [...], series: [{ name: '腾讯视频', data: [...] }, ...] }
        Set<String> nameSet = new LinkedHashSet<>();
        Map<String, Map<String, Double>> dataMap = new LinkedHashMap<>(); // date → {name → price}
        for (Map<String, Object> row : rows) {
            String dt = row.get("dt").toString().substring(5); // MM-DD
            String name = (String) row.get("name");
            double price = ((Number) row.get("avg_price")).doubleValue();
            nameSet.add(name);
            dataMap.computeIfAbsent(dt, k -> new LinkedHashMap<>()).put(name, price);
        }

        List<String> dates = new ArrayList<>(dataMap.keySet());
        List<Map<String, Object>> series = new ArrayList<>();
        for (String name : nameSet) {
            List<Double> prices = new ArrayList<>();
            for (String dt : dates) {
                Double p = dataMap.getOrDefault(dt, Collections.emptyMap()).get(name);
                prices.add(p != null ? Math.round(p * 100.0) / 100.0 : null);
            }
            series.add(Map.of("name", name, "data", prices));
        }

        return Map.of("dates", dates, "series", series);
    }
}
