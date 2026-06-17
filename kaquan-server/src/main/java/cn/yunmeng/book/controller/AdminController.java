package cn.yunmeng.book.controller;
import cn.yunmeng.book.config.LoginRateLimiter;
import cn.yunmeng.book.mapper.AdminDao;
import cn.yunmeng.book.pojo.Admin;
import cn.yunmeng.book.pojo.Goods;
import cn.yunmeng.book.pojo.Orders;
import cn.yunmeng.book.pojo.User;
import cn.yunmeng.book.service.AdminService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource private AdminService adminService;
    @Resource private AdminDao adminDao;
    @Resource private JdbcTemplate jdbc;
    @Resource private LoginRateLimiter rateLimiter;

    private String clientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
            ip = req.getHeader("X-Real-IP");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip))
            ip = req.getRemoteAddr();
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }

    /** 校验管理员登录，未登录返回 403 */
    private boolean checkAdmin(HttpSession session) {
        return session.getAttribute("admin") != null;
    }

    // ===== 登录（限流） =====
    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody Admin admin, HttpSession session, HttpServletRequest req){
        Map<String,Object> map=new HashMap<>();
        String ip = clientIp(req);
        if (!rateLimiter.allow(ip)) {
            map.put("code", 429); map.put("msg", "登录过于频繁，请1分钟后再试"); return map;
        }
        Admin ad=adminService.login(admin.getUsername(),admin.getPassword());
        if(ad!=null){
            rateLimiter.clear(ip);
            session.setAttribute("admin",ad);
            map.put("code",200);
        } else map.put("code",500);
        return map;
    }

    @PostMapping("/check")
    public Map<String,Object> check(HttpSession session){
        return Map.of("code", checkAdmin(session)?200:500);
    }

    @PostMapping("/logout")
    public Map<String,Object> logout(HttpSession session){
        session.removeAttribute("admin");
        return Map.of("code",200);
    }

    // ===== 商品管理（需管理员） =====
    @GetMapping("/goods/list")
    public Object goodsList(HttpSession session) {
        if (!checkAdmin(session)) return Map.of("code",403,"msg","无权操作");
        return adminService.allGoods();
    }

    @PostMapping("/goods/add")
    public Map<String,Object> addGoods(@RequestBody Goods g, HttpSession session){
        if (!checkAdmin(session)) return Map.of("code",403,"msg","无权操作");
        return Map.of("code",adminService.addGoods(g)?200:500);
    }

    @PostMapping("/goods/update")
    public Map<String,Object> updateGoods(@RequestBody Goods g, HttpSession session){
        if (!checkAdmin(session)) return Map.of("code",403,"msg","无权操作");
        return Map.of("code",adminService.editGoods(g)?200:500);
    }

    @PostMapping("/goods/del")
    public Map<String,Object> delGoods(@RequestParam Integer id, HttpSession session){
        if (!checkAdmin(session)) return Map.of("code",403,"msg","无权操作");
        return Map.of("code",adminService.delGoods(id)?200:500);
    }

    // ===== 用户管理（需管理员） =====
    @GetMapping("/user/list")
    public Object userList(HttpSession session) {
        if (!checkAdmin(session)) return Map.of("code",403,"msg","无权操作");
        return adminService.allUser();
    }

    @PostMapping("/user/update")
    public Map<String,Object> updateUser(@RequestBody User u, HttpSession session){
        if (!checkAdmin(session)) return Map.of("code",403,"msg","无权操作");
        return Map.of("code",adminService.updateUser(u)?200:500);
    }

    @PostMapping("/user/del")
    public Map<String,Object> delUser(@RequestParam Integer id, HttpSession session){
        if (!checkAdmin(session)) return Map.of("code",403,"msg","无权操作");
        return Map.of("code",adminService.delUser(id)?200:500);
    }

    // ===== 订单管理（需管理员，支持分页） =====
    @GetMapping("/order/all")
    public Map<String, Object> allOrder(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int pageSize,
            HttpSession session) {
        if (!checkAdmin(session)) return Map.of("code",403,"msg","无权操作");
        int offset = (page - 1) * pageSize;
        return Map.of(
            "list", adminDao.selectOrderPage(offset, pageSize),
            "total", adminDao.countOrders(),
            "page", page,
            "pageSize", pageSize
        );
    }

    /** 热销排行：按真实订单销量，排除退款订单，取 TOP 3 */
    @GetMapping("/hotRank")
    public Object hotRank(HttpSession session) {
        if (!checkAdmin(session)) return Map.of("code",403,"msg","无权操作");
        return adminDao.hotRank();
    }

    /** 近30天趋势：订单数、交易额、注册数 */
    @GetMapping("/trend")
    public Map<String, Object> trend(HttpSession session) {
        if (!checkAdmin(session)) return Map.of("code",403,"msg","无权操作");
        // 订单数 & 交易额
        String sql1 = "SELECT DATE(create_time) AS dt, COUNT(*) AS orders, COALESCE(SUM(order_price),0) AS amount FROM orders WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND status != 'refunded' GROUP BY dt ORDER BY dt";
        // 注册数
        String sql2 = "SELECT DATE(create_time) AS dt, COUNT(*) AS reg FROM user WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) GROUP BY dt ORDER BY dt";
        List<Map<String, Object>> oRows = jdbc.queryForList(sql1);
        List<Map<String, Object>> uRows = jdbc.queryForList(sql2);
        // 合成为图表数据
        java.util.Set<String> dateSet = new java.util.LinkedHashSet<>();
        Map<String, Integer> orderMap = new HashMap<>();
        Map<String, Double> amountMap = new HashMap<>();
        Map<String, Integer> regMap = new HashMap<>();
        for (Map<String, Object> r : oRows) {
            String d = r.get("dt").toString().substring(5);
            dateSet.add(d);
            int count = ((Number) r.get("orders")).intValue();
            double amt = ((Number) r.get("amount")).doubleValue();
            orderMap.put(d, count);
            amountMap.put(d, Math.round(amt * 100.0) / 100.0);
        }
        for (Map<String, Object> r : uRows) {
            String d = r.get("dt").toString().substring(5);
            dateSet.add(d);
            regMap.put(d, ((Number) r.get("reg")).intValue());
        }
        List<String> dates = new java.util.ArrayList<>(dateSet);
        List<Integer> orderData = new java.util.ArrayList<>();
        List<Double> amountData = new java.util.ArrayList<>();
        List<Integer> regData = new java.util.ArrayList<>();
        for (String d : dates) {
            orderData.add(orderMap.getOrDefault(d, 0));
            amountData.add(amountMap.getOrDefault(d, 0.0));
            regData.add(regMap.getOrDefault(d, 0));
        }
        return Map.of("dates", dates, "orders", orderData, "amount", amountData, "reg", regData);
    }
}
