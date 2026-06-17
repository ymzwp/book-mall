package cn.yunmeng.book.controller;

import cn.yunmeng.book.mapper.CouponDao;
import cn.yunmeng.book.pojo.Coupon;
import cn.yunmeng.book.pojo.User;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 优惠券：前台领取/使用 + 后台管理 */
@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Resource private CouponDao couponDao;

    /** 校验管理员 */
    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("admin") != null;
    }

    // ===== 前台：获取可领优惠券 =====
    @GetMapping("/list")
    public List<Coupon> list() {
        return couponDao.findActive();
    }

    /** 领取优惠券（扣库存 + 写记录） */
    @Transactional
    @PostMapping("/take")
    public Map<String, Object> take(@RequestBody Map<String, Object> body, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        User login = (User) session.getAttribute("loginUser");
        if (login == null) { map.put("code", 501); map.put("msg", "请先登录"); return map; }
        Integer couponId = (Integer) body.get("couponId");
        if (couponId == null) { map.put("code", 400); map.put("msg", "参数错误"); return map; }
        int rows = couponDao.reduceStock(couponId);
        if (rows <= 0) { map.put("code", 500); map.put("msg", "券已领完或不存在"); return map; }
        couponDao.insertUserCoupon(login.getId(), couponId);
        map.put("code", 200); map.put("msg", "领取成功");
        return map;
    }

    /** 查询我的优惠券 */
    @GetMapping("/my")
    public Map<String, Object> my(HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        User login = (User) session.getAttribute("loginUser");
        if (login == null) { map.put("code", 501); map.put("msg", "请先登录"); return map; }
        map.put("code", 200);
        map.put("data", couponDao.findUserCoupons(login.getId()));
        return map;
    }

    /** 使用优惠券（下单时调用） */
    @PostMapping("/use")
    public Map<String, Object> use(@RequestBody Map<String, Object> body, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        User login = (User) session.getAttribute("loginUser");
        if (login == null) { map.put("code", 501); map.put("msg", "请先登录"); return map; }
        Integer userCouponId = (Integer) body.get("userCouponId");
        if (userCouponId == null) { map.put("code", 400); map.put("msg", "参数错误"); return map; }
        int rows = couponDao.useCoupon(userCouponId, login.getId());
        if (rows <= 0) { map.put("code", 500); map.put("msg", "使用失败"); return map; }
        map.put("code", 200); map.put("msg", "优惠券已使用");
        return map;
    }

    // ===== 后台管理（需管理员） =====
    @GetMapping("/admin/list")
    public Object adminList(HttpSession session) {
        if (!isAdmin(session)) return Map.of("code", 403, "msg", "无权操作");
        return couponDao.findAll();
    }

    @PostMapping("/admin/add")
    public Map<String, Object> add(@RequestBody Coupon c, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        if (!isAdmin(session)) { map.put("code", 403); map.put("msg", "无权操作"); return map; }
        int rows = couponDao.insert(c);
        map.put("code", rows > 0 ? 200 : 500);
        return map;
    }

    @PostMapping("/admin/update")
    public Map<String, Object> update(@RequestBody Coupon c, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        if (!isAdmin(session)) { map.put("code", 403); map.put("msg", "无权操作"); return map; }
        int rows = couponDao.update(c);
        map.put("code", rows > 0 ? 200 : 500);
        return map;
    }

    @PostMapping("/admin/del")
    public Map<String, Object> del(@RequestParam Integer id, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        if (!isAdmin(session)) { map.put("code", 403); map.put("msg", "无权操作"); return map; }
        int rows = couponDao.delete(id);
        map.put("code", rows > 0 ? 200 : 500);
        return map;
    }
}
