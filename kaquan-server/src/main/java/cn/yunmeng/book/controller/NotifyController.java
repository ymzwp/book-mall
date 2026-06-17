package cn.yunmeng.book.controller;

import cn.yunmeng.book.mapper.NotifyDao;
import cn.yunmeng.book.pojo.Admin;
import cn.yunmeng.book.pojo.SysNotify;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内通知 — 前台读取 + 后台管理（需管理员登录）
 */
@RestController
@RequestMapping("/notify")
public class NotifyController {

    @Resource private NotifyDao notifyDao;

    /** 校验管理员登录 */
    private boolean isAdmin(HttpSession session) {
        return session.getAttribute("admin") != null;
    }

    // ===== 前台：获取可见通知（公开，无需登录） =====
    @GetMapping("/list")
    public List<SysNotify> list() {
        return notifyDao.findActive();
    }

    // ===== 后台：以下全部需要管理员登录 =====
    @GetMapping("/admin/list")
    public List<SysNotify> adminList(HttpSession session) {
        if (!isAdmin(session)) return List.of();
        return notifyDao.findAll();
    }

    @PostMapping("/admin/add")
    public Map<String, Object> add(@RequestBody SysNotify n, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        if (!isAdmin(session)) { map.put("code", 403); map.put("msg", "无权操作"); return map; }
        int rows = notifyDao.insert(n);
        map.put("code", rows > 0 ? 200 : 500);
        return map;
    }

    @PostMapping("/admin/update")
    public Map<String, Object> update(@RequestBody SysNotify n, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        if (!isAdmin(session)) { map.put("code", 403); map.put("msg", "无权操作"); return map; }
        int rows = notifyDao.update(n);
        map.put("code", rows > 0 ? 200 : 500);
        return map;
    }

    @PostMapping("/admin/del")
    public Map<String, Object> del(@RequestParam Integer id, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        if (!isAdmin(session)) { map.put("code", 403); map.put("msg", "无权操作"); return map; }
        int rows = notifyDao.delete(id);
        map.put("code", rows > 0 ? 200 : 500);
        return map;
    }
}
