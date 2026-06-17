package cn.yunmeng.book.service;
import cn.yunmeng.book.pojo.Goods;
import java.util.List;
/** 商品业务接口 */
public interface GoodsService {
    /** 获取所有上架商品 */
    List<Goods> getUpAll();
    /** 按分类获取商品 */
    List<Goods> getByCid(Integer cid);
    /** 按ID获取商品详情 */
    Goods getById(Integer id);
}