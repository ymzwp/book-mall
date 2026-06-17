package cn.yunmeng.book.service;
import cn.yunmeng.book.pojo.User;
public interface UserService {
    User login(String username,String pwd);
    boolean register(User user);
    /** 注册（带邀请码） */
    boolean register(User user, String inviteCode);
    boolean updateNickAvatar(User user);
    User getById(Integer id);
    /** 购买会员：扣余额、升级，返回 1成功 0余额不足 -1用户不存在 */
    int buyMember(Integer userId, java.math.BigDecimal price, String level);
}