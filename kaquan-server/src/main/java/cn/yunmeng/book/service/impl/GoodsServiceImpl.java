package cn.yunmeng.book.service.impl;
import cn.yunmeng.book.mapper.GoodsDao;
import cn.yunmeng.book.pojo.Goods;
import cn.yunmeng.book.service.GoodsService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
/** 商品业务实现 */
@Service
public class GoodsServiceImpl implements GoodsService {
    @Resource private GoodsDao goodsDao;
    @Override public List<Goods> getUpAll(){return goodsDao.findUpAll();}
    @Override public List<Goods> getByCid(Integer cid){return goodsDao.findByCid(cid);}
    @Override public Goods getById(Integer id){return goodsDao.findById(id);}
}