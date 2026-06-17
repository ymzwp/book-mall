package cn.yunmeng.book.controller;

import cn.yunmeng.book.mapper.RechargeDao;
import cn.yunmeng.book.mapper.UserDao;
import cn.yunmeng.book.pojo.RechargeOrder;
import cn.yunmeng.book.pojo.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 充值接口：创建充值订单、查询充值记录 */
@RestController
@RequestMapping("/recharge")
public class RechargeController {

    @Resource private UserDao userDao;
    @Resource private RechargeDao rechargeDao;
    @Resource private PasswordEncoder passwordEncoder;

    /** 创建充值订单（需登录、密码确认、金额校验） */
    @Transactional
    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Map<String, Object> body, HttpSession session) {
        Map<String, Object> map = new HashMap<>();

        User login = (User) session.getAttribute("loginUser");
        if (login == null) { map.put("code", 501); map.put("msg", "请先登录"); return map; }

        Object amountObj = body.get("amount");
        Object payMethodObj = body.get("payMethod");
        if (amountObj == null || payMethodObj == null) {
            map.put("code", 400); map.put("msg", "参数错误"); return map;
        }
        BigDecimal amount;
        try { amount = new BigDecimal(amountObj.toString()); }
        catch (NumberFormatException e) { map.put("code", 400); map.put("msg", "金额格式错误"); return map; }
        if (amount.compareTo(new BigDecimal("1")) < 0 || amount.compareTo(new BigDecimal("5000")) > 0) {
            map.put("code", 400); map.put("msg", "充值金额需在1~5000元之间"); return map;
        }

        Object pwdObj = body.get("password");
        if (pwdObj == null || !passwordEncoder.matches(pwdObj.toString(), login.getPassword())) {
            map.put("code", 403); map.put("msg", "密码错误"); return map;
        }

        User fresh = userDao.findById(login.getId());
        BigDecimal before = fresh.getBalance() != null ? fresh.getBalance() : BigDecimal.ZERO;
        String payMethod = payMethodObj != null ? payMethodObj.toString() : "alipay";

        int rows = userDao.addBalance(login.getId(), amount);
        if (rows <= 0) { map.put("code", 500); map.put("msg", "充值失败"); return map; }

        RechargeOrder order = new RechargeOrder();
        order.setUserId(login.getId());
        order.setOrderNo(genOrderNo());
        order.setAmount(amount);
        order.setPayMethod(payMethod);
        order.setStatus("success");
        order.setBalanceBefore(before);
        order.setBalanceAfter(before.add(amount));
        order.setRemark(payMethod + "充值 ¥" + amount);
        rechargeDao.insert(order);

        map.put("code", 200);
        map.put("msg", "充值成功");
        map.put("data", Map.of("before", before, "after", before.add(amount), "orderNo", order.getOrderNo()));
        return map;
    }

    /** 查询当前用户充值记录 */
    @PostMapping("/list")
    public Map<String, Object> list(HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        User login = (User) session.getAttribute("loginUser");
        if (login == null) { map.put("code", 501); map.put("msg", "请先登录"); return map; }
        List<RechargeOrder> list = rechargeDao.findByUserId(login.getId());
        map.put("code", 200);
        map.put("data", list);
        return map;
    }

    /** 生成订单号: R + 时间戳 + 6位随机数 */
    private String genOrderNo() {
        return "R" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%06d", (int) (Math.random() * 1000000));
    }
}
