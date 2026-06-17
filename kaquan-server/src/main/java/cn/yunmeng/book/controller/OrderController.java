package cn.yunmeng.book.controller;

import cn.yunmeng.book.mapper.UserDao;
import cn.yunmeng.book.pojo.Goods;
import cn.yunmeng.book.pojo.Orders;
import cn.yunmeng.book.pojo.User;
import cn.yunmeng.book.service.GoodsService;
import cn.yunmeng.book.service.OrderService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource private OrderService orderService;
    @Resource private GoodsService goodsService;
    @Resource private UserDao userDao;
    @Resource private PasswordEncoder passwordEncoder;

    /**
     * 创建订单（需要商品ID和充值账号）
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestBody Map<String, Object> params, HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            return Map.of("code", 501);
        }
        try {
            Integer goodsId = (Integer) params.get("goodsId");
            String accountNumber = (String) params.get("accountNumber");
            String payMethod = (String) params.get("payMethod");  // 支付方式
            Object discountObj = params.get("discount");          // 优惠抵扣金额
            java.math.BigDecimal discount = discountObj != null ? new java.math.BigDecimal(discountObj.toString()) : java.math.BigDecimal.ZERO;
            if (goodsId == null || accountNumber == null || accountNumber.trim().isEmpty()) {
                return Map.of("code", 400, "msg", "商品ID或账号不能为空");
            }
            Goods goods = goodsService.getById(goodsId);
            if (goods == null) {
                return Map.of("code", 404, "msg", "商品不存在");
            }
            // 余额支付：扣减余额
            java.math.BigDecimal finalPrice = goods.getPrice().subtract(discount);
            if ("balance".equals(payMethod)) {
                User fresh = userDao.findById(loginUser.getId());
                if (fresh.getBalance().compareTo(finalPrice) < 0) {
                    return Map.of("code", 400, "msg", "余额不足");
                }
                userDao.addBalance(loginUser.getId(), finalPrice.negate());  // 扣款
            }
            Orders order = new Orders();
            order.setUserId(loginUser.getId());
            order.setGoodsId(goodsId);
            order.setOrderPrice(finalPrice);  // 实付金额
            order.setAccountNumber(accountNumber.trim());
            order.setPayMethod(payMethod != null ? payMethod : "alipay");
            boolean ok = orderService.create(order);
            return Map.of("code", ok ? 200 : 500);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("code", 500, "msg", "下单失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户的所有订单
     */
    @PostMapping("/my")
    public Map<String, Object> my(HttpSession session) {
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) return Map.of("code", 501);
        List<Orders> list = orderService.myOrder(loginUser.getId());
        return Map.of("code", 200, "data", list);
    }

    /** 退款：已完成订单退款，退还余额 */
    @Transactional
    @PostMapping("/refund")
    public Map<String, Object> refund(@RequestBody Map<String, Object> body, HttpSession session) {
        User login = (User) session.getAttribute("loginUser");
        if (login == null) return Map.of("code", 501, "msg", "请先登录");

        Integer orderId = (Integer) body.get("id");
        if (orderId == null) return Map.of("code", 400, "msg", "订单ID不能为空");

        // 敏感操作：密码确认
        Object pwdObj = body.get("password");
        if (pwdObj == null || !passwordEncoder.matches(pwdObj.toString(), login.getPassword())) {
            return Map.of("code", 403, "msg", "密码错误，操作取消");
        }

        Orders order = orderService.getById(orderId);
        if (order == null) return Map.of("code", 404, "msg", "订单不存在");
        if (!order.getUserId().equals(login.getId())) return Map.of("code", 403, "msg", "无权操作");
        if (!"completed".equals(order.getStatus())) return Map.of("code", 400, "msg", "该订单无法退款");

        boolean ok = orderService.refund(orderId);
        if (!ok) return Map.of("code", 500, "msg", "退款失败");

        // 退还余额
        userDao.addBalance(login.getId(), order.getOrderPrice());

        return Map.of("code", 200, "msg", "退款成功，已退还 ¥" + order.getOrderPrice());
    }
}