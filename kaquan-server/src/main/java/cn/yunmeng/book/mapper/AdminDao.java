package cn.yunmeng.book.mapper;

import cn.yunmeng.book.pojo.Admin;
import cn.yunmeng.book.pojo.Goods;
import cn.yunmeng.book.pojo.Orders;
import cn.yunmeng.book.pojo.User;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

/** 管理员数据访问（含商品/用户/订单管理） */
@Mapper
public interface AdminDao {

    /** 按用户名查管理员 */
    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin findAdmin(String username);

    /** 查所有商品 */
    @Select("SELECT * FROM goods")
    List<Goods> selectAllGoods();

    /** 添加商品 */
    @Insert("INSERT INTO goods(cate_id, goods_name, price, stock, goods_img, info, detail_imgs, status) VALUES(#{cateId}, #{goodsName}, #{price}, #{stock}, #{goodsImg}, #{info}, #{detailImgs}, 1)")
    int addGoods(Goods goods);

    /** 更新商品 */
    @Update("UPDATE goods SET cate_id = #{cateId}, goods_name = #{goodsName}, price = #{price}, stock = #{stock}, goods_img = #{goodsImg}, info = #{info}, detail_imgs = #{detailImgs}, status = #{status} WHERE id = #{id}")
    int updateGoods(Goods goods);

    /** 删除商品 */
    @Delete("DELETE FROM goods WHERE id = #{id}")
    int delGoods(Integer id);

    /** 查所有用户 */
    @Select("SELECT * FROM user")
    List<User> selectAllUser();

    /** 更新用户全部信息 */
    @Update("UPDATE user SET username = #{username}, password = #{password}, nick_name = #{nickName}, phone = #{phone}, email = #{email}, avatar = #{avatar} WHERE id = #{id}")
    int updateUserAll(User user);

    /** 更新管理员密码 */
    @Update("UPDATE admin SET password = #{password} WHERE id = #{id}")
    int updateAdminPassword(@Param("id") Integer id, @Param("password") String password);

    /** 删除用户 */
    @Delete("DELETE FROM user WHERE id = #{uid}")
    int deleteUser(Integer uid);

    /** 查所有订单 */
    @Select("SELECT * FROM orders ORDER BY create_time DESC")
    List<Orders> selectAllOrder();

    /** 分页查订单 */
    @Select("SELECT * FROM orders ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<Orders> selectOrderPage(@Param("offset") int offset, @Param("size") int size);

    /** 订单总数 */
    @Select("SELECT COUNT(*) FROM orders")
    int countOrders();

    /** 热销排行：按商品销量降序取 TOP 3 */
    @Select("SELECT g.goods_name AS name, COUNT(*) AS orders FROM orders o JOIN goods g ON o.goods_id = g.id WHERE o.status != 'refunded' GROUP BY o.goods_id ORDER BY orders DESC LIMIT 3")
    List<Map<String, Object>> hotRank();
}
