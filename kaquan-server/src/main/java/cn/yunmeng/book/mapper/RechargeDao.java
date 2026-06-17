package cn.yunmeng.book.mapper;

import cn.yunmeng.book.pojo.RechargeOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

/** 充值订单数据访问 */
@Mapper
public interface RechargeDao {

    /** 创建充值订单 */
    @Insert("INSERT INTO recharge_order(user_id, order_no, amount, pay_method, status, balance_before, balance_after, remark, create_time) " +
            "VALUES(#{userId}, #{orderNo}, #{amount}, #{payMethod}, #{status}, #{balanceBefore}, #{balanceAfter}, #{remark}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RechargeOrder order);

    /** 按用户ID查充值记录 */
    @Select("SELECT * FROM recharge_order WHERE user_id = #{userId} AND deleted = 0 ORDER BY create_time DESC")
    List<RechargeOrder> findByUserId(Integer userId);
}
