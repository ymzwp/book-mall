package cn.yunmeng.book.service.impl;
import cn.yunmeng.book.mapper.CateDao;
import cn.yunmeng.book.pojo.Category;
import cn.yunmeng.book.service.CateService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
/** 商品分类业务实现 */
@Service
public class CateServiceImpl implements CateService {
    @Resource private CateDao cateDao;
    @Override public List<Category> getList(){return cateDao.findAll();}
}