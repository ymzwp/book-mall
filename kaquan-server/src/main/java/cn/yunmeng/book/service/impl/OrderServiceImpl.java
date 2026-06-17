package cn.yunmeng.book.service.impl;
import cn.yunmeng.book.mapper.OrderDao;
import cn.yunmeng.book.pojo.Orders;
import cn.yunmeng.book.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import java.util.List;
/** 订单业务实现 */
@Service
public class OrderServiceImpl implements OrderService {
    @Resource private OrderDao orderDao;
    @Override public boolean create(Orders o){return orderDao.insert(o)>0;}
    @Override public List<Orders> myOrder(Integer uid){return orderDao.findByUid(uid);}
    @Override public Orders getById(Integer id){return orderDao.findById(id);}
    @Override public boolean refund(Integer id){return orderDao.refund(id)>0;}
}