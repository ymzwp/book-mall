package cn.yunmeng.book.controller;
import cn.yunmeng.book.mapper.CateDao;
import cn.yunmeng.book.pojo.Category;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** 分类：前台列表 + 后台管理 */
@RestController
@RequestMapping("/cate")
public class CateController {
    @Resource private CateDao cateDao;

    private boolean isAdmin(HttpSession s) { return s.getAttribute("admin") != null; }

    /** 前台：返回树形结构 */
    @GetMapping("/list")
    public List<Category> list() {
        List<Category> all = cateDao.findAll();
        Map<Integer, Category> map = new HashMap<>();
        List<Category> roots = new ArrayList<>();
        for (Category c : all) { map.put(c.getId(), c); if (c.getLevel() == 1) roots.add(c); }
        for (Category c : all) {
            if (c.getLevel() == 1) continue;
            Category p = map.get(c.getParentId());
            if (p != null) { if (p.getChildren() == null) p.setChildren(new ArrayList<>()); p.getChildren().add(c); }
        }
        return roots;
    }

    // ===== 后台管理 =====
    @GetMapping("/admin/list")
    public Object adminList(HttpSession s) { return isAdmin(s) ? cateDao.findAll() : Map.of("code", 403); }

    @PostMapping("/admin/add")
    public Map<String, Object> add(@RequestBody Category c, HttpSession s) {
        Map<String, Object> map = new HashMap<>();
        if (!isAdmin(s)) { map.put("code", 403); return map; }
        if (c.getParentId() == null || c.getParentId() == 0) c.setLevel(1);
        else c.setLevel(2);
        map.put("code", cateDao.insert(c) > 0 ? 200 : 500);
        return map;
    }

    @PostMapping("/admin/update")
    public Map<String, Object> update(@RequestBody Category c, HttpSession s) {
        if (!isAdmin(s)) return Map.of("code", 403);
        return Map.of("code", cateDao.update(c) > 0 ? 200 : 500);
    }

    @PostMapping("/admin/del")
    public Map<String, Object> del(@RequestParam Integer id, HttpSession s) {
        if (!isAdmin(s)) return Map.of("code", 403);
        return Map.of("code", cateDao.delete(id) > 0 ? 200 : 500);
    }
}
