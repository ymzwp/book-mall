package cn.yunmeng.book.mapper;

import cn.yunmeng.book.pojo.Coupon;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/** 优惠券数据访问 */
@Mapper
public interface CouponDao {

    /** 查询上架优惠券（前台） */
    @Select("SELECT * FROM coupon WHERE status = 1 AND stock > 0 ORDER BY create_time DESC")
    List<Coupon> findActive();

    /** 查询全部优惠券（后台） */
    @Select("SELECT * FROM coupon ORDER BY create_time DESC")
    List<Coupon> findAll();

    /** 新增优惠券 */
    @Insert("INSERT INTO coupon(title, amount, min_amount, stock, status) VALUES(#{title}, #{amount}, #{minAmount}, #{stock}, #{status})")
    int insert(Coupon c);

    /** 更新优惠券 */
    @Update("UPDATE coupon SET title=#{title}, amount=#{amount}, min_amount=#{minAmount}, stock=#{stock}, status=#{status} WHERE id=#{id}")
    int update(Coupon c);

    /** 扣减库存 */
    @Update("UPDATE coupon SET stock = stock - 1 WHERE id = #{id} AND stock > 0")
    int reduceStock(Integer id);

    /** 删除优惠券 */
    @Delete("DELETE FROM coupon WHERE id = #{id}")
    int delete(Integer id);

    /** 查询用户已领取的未使用优惠券（联表查券信息） */
    @Select("SELECT c.*, uc.id AS user_coupon_id FROM coupon c JOIN user_coupon uc ON c.id = uc.coupon_id WHERE uc.user_id = #{userId} AND uc.status = 0 AND c.status = 1")
    List<Map<String, Object>> findUserCoupons(Integer userId);

    /** 领取优惠券：插入领取记录 */
    @Insert("INSERT INTO user_coupon(user_id, coupon_id) VALUES(#{userId}, #{couponId})")
    int insertUserCoupon(@Param("userId") Integer userId, @Param("couponId") Integer couponId);

    /** 使用优惠券：标记已使用 */
    @Update("UPDATE user_coupon SET status = 1 WHERE id = #{id} AND user_id = #{userId} AND status = 0")
    int useCoupon(@Param("id") Integer id, @Param("userId") Integer userId);
}
