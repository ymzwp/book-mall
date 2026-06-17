package cn.yunmeng.book.controller;
import cn.yunmeng.book.mapper.GoodsDao;
import cn.yunmeng.book.pojo.Goods;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.*;

/** 商品接口 */
@RestController
@RequestMapping("/goods")
public class GoodsController {
    @Resource private GoodsDao goodsDao;

    /** 商品列表：支持分页 */
    @GetMapping("/list")
    public Map<String, Object> list(
            @RequestParam(required = false) Integer cid,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize) {
        int offset = (page - 1) * pageSize;
        List<Goods> list;
        int total;
        if (cid != null) {
            list = goodsDao.findByCidPage(cid, offset, pageSize);
            total = goodsDao.countByCid(cid);
        } else {
            list = goodsDao.findUpPage(offset, pageSize);
            total = goodsDao.countAll();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("list", list);
        map.put("total", total);
        map.put("page", page);
        map.put("pageSize", pageSize);
        map.put("totalPages", (int) Math.ceil((double) total / pageSize));
        return map;
    }

    /** 根据id查单个商品 */
    @GetMapping("/get")
    public Goods get(Integer id) { return goodsDao.findById(id); }
}
