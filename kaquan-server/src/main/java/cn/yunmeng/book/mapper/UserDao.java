package cn.yunmeng.book.mapper;

import cn.yunmeng.book.pojo.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserDao {

    User findByUsername(String username);

    User findById(Integer id);

    int insertUser(User user);

    /** 更新用户信息（含手机邮箱） */
    @Update("UPDATE user SET nick_name = #{nickName}, phone = #{phone}, email = #{email}, avatar = #{avatar} WHERE id = #{id}")
    int updateInfo(User user);

    /** 扣减余额并更新会员等级（原子操作） */
    int deductBalanceAndSetLevel(@org.apache.ibatis.annotations.Param("id") Integer id,
                                 @org.apache.ibatis.annotations.Param("amount") java.math.BigDecimal amount,
                                 @org.apache.ibatis.annotations.Param("level") String level);

    /** 增加余额 */
    int addBalance(@org.apache.ibatis.annotations.Param("id") Integer id,
                   @org.apache.ibatis.annotations.Param("amount") java.math.BigDecimal amount);

    /** 更新密码（MD5→BCrypt 迁移用） */
    int updatePassword(@org.apache.ibatis.annotations.Param("id") Integer id,
                       @org.apache.ibatis.annotations.Param("password") String password);

    /** 按邀请码查用户 */
    @Select("SELECT * FROM user WHERE invite_code = #{code}")
    User findByInviteCode(String code);

    /** 注册时写入邀请码和邀请人 */
    @Insert("INSERT INTO user(username, password, nick_name, avatar, invite_code, invited_by) VALUES(#{username}, #{password}, #{nickName}, #{avatar}, #{inviteCode}, #{invitedBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertWithInvite(User user);
}
