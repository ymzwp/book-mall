package cn.yunmeng.book.mapper;

import cn.yunmeng.book.pojo.Orders;
import org.apache.ibatis.annotations.*;
import java.util.List;

/** 订单数据访问 */
@Mapper
public interface OrderDao {
    /** 创建订单 */
    @Insert("INSERT INTO orders(user_id, goods_id, order_price, account_number, pay_method, status) VALUES(#{userId}, #{goodsId}, #{orderPrice}, #{accountNumber}, #{payMethod}, 'completed')")
    int insert(Orders order);

    /** 按用户ID查订单列表（最新在前） */
    @Select("SELECT * FROM orders WHERE user_id = #{uid} ORDER BY create_time DESC")
    List<Orders> findByUid(Integer uid);

    /** 按ID查单个订单 */
    @Select("SELECT * FROM orders WHERE id = #{id}")
    Orders findById(Integer id);

    /** 退款：更新订单状态为 refunded */
    @Update("UPDATE orders SET status = 'refunded' WHERE id = #{id} AND status = 'completed'")
    int refund(Integer id);
}
