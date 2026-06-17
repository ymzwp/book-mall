package cn.yunmeng.book.service;
import cn.yunmeng.book.pojo.Orders;
import java.util.List;
/** 订单业务接口 */
public interface OrderService {
    /** 创建订单 */
    boolean create(Orders o);
    /** 查询我的订单 */
    List<Orders> myOrder(Integer uid);
    /** 按ID查订单 */
    Orders getById(Integer id);
    /** 退款 */
    boolean refund(Integer id);
}