package cn.yunmeng.book.mapper;

import cn.yunmeng.book.pojo.UserSign;
import org.apache.ibatis.annotations.*;

/** 签到数据访问 */
@Mapper
public interface SignDao {
    /** 查询今日是否已签到 */
    @Select("SELECT * FROM user_sign WHERE user_id = #{uid} AND sign_date = CURDATE()")
    UserSign getToday(Integer uid);

    /** 插入签到记录 */
    @Insert("INSERT INTO user_sign(user_id, sign_date) VALUES(#{userId}, #{signDate})")
    int insertSign(UserSign sign);

    /** 签到奖励：+10 云梦豆 */
    @Update("UPDATE user SET points = points + 10 WHERE id = #{uid}")
    int addPoints(@Param("uid") Integer uid);
}
