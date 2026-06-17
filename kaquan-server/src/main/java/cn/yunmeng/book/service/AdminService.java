package cn.yunmeng.book.service;
import cn.yunmeng.book.pojo.Admin;
import cn.yunmeng.book.pojo.Goods;
import cn.yunmeng.book.pojo.Orders;
import cn.yunmeng.book.pojo.User;
import java.util.List;
/** 管理员业务接口：登录、商品管理、用户管理、订单管理 */
public interface AdminService {
    /** 管理员登录 */
    Admin login(String name,String pwd);
    /** 查询全部商品 */
    List<Goods> allGoods();
    /** 添加商品 */
    boolean addGoods(Goods g);
    /** 编辑商品 */
    boolean editGoods(Goods g);
    /** 删除商品 */
    boolean delGoods(Integer id);
    /** 查询全部用户 */
    List<User> allUser();
    /** 更新用户信息 */
    boolean updateUser(User u);
    /** 删除用户 */
    boolean delUser(Integer id);
    /** 查询全部订单 */
    List<Orders> allOrder();
}