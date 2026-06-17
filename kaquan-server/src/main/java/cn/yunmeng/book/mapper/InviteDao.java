package cn.yunmeng.book.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/** 邀请奖励数据访问 */
@Mapper
public interface InviteDao {

    /** 插入邀请奖励记录 */
    @Insert("INSERT INTO invite_reward(inviter_id, invitee_id, reward) VALUES(#{inviterId}, #{inviteeId}, #{reward})")
    int insert(@Param("inviterId") Integer inviterId, @Param("inviteeId") Integer inviteeId, @Param("reward") String reward);

    /** 查询邀请人旗下的被邀请记录 */
    @Select("SELECT ir.*, u.nick_name AS inviteeName, u.create_time AS inviteeTime FROM invite_reward ir JOIN user u ON ir.invitee_id = u.id WHERE ir.inviter_id = #{inviterId} ORDER BY ir.create_time DESC")
    List<Map<String, Object>> findByInviter(Integer inviterId);

    /** 统计邀请人数 */
    @Select("SELECT COUNT(*) FROM invite_reward WHERE inviter_id = #{inviterId}")
    int countByInviter(Integer inviterId);
}
