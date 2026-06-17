package cn.yunmeng.book.service;
import cn.yunmeng.book.pojo.Category;
import java.util.List;
/** 商品分类业务接口 */
public interface CateService {
    /** 获取全部分类列表 */
    List<Category> getList();
}