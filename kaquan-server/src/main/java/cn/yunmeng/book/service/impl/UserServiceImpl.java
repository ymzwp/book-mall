package cn.yunmeng.book.service.impl;
import cn.yunmeng.book.mapper.InviteDao;
import cn.yunmeng.book.mapper.UserDao;
import cn.yunmeng.book.pojo.User;
import cn.yunmeng.book.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl implements UserService {
    @Resource private UserDao userDao;
    @Resource private InviteDao inviteDao;
    @Resource private PasswordEncoder passwordEncoder;

    private boolean isMd5(String hash) {
        return hash != null && hash.length() == 32 && !hash.startsWith("$");
    }

    private boolean checkAndUpgrade(User user, String rawPwd) {
        String stored = user.getPassword();
        if (stored == null) return false;
        if (stored.startsWith("$")) return passwordEncoder.matches(rawPwd, stored);
        if (isMd5(stored)) {
            String md5 = DigestUtils.md5DigestAsHex(rawPwd.getBytes());
            if (stored.equals(md5)) {
                user.setPassword(passwordEncoder.encode(rawPwd));
                userDao.updatePassword(user.getId(), user.getPassword());
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public User login(String username, String pwd) {
        User user = userDao.findByUsername(username);
        if (user == null) return null;
        return checkAndUpgrade(user, pwd) ? user : null;
    }

    /** 生成唯一邀请码 */
    private String genInviteCode() {
        String code;
        do { code = "XJ" + String.format("%05d", (int) (Math.random() * 100000)); }
        while (userDao.findByInviteCode(code) != null);
        return code;
    }

    @Override
    @Transactional
    public boolean register(User user, String inviteCode) {
        if (userDao.findByUsername(user.getUsername()) != null) return false;
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setNickName("新注册用户");
        user.setAvatar("");
        user.setInviteCode(genInviteCode());
        // 处理邀请
        if (inviteCode != null && !inviteCode.isBlank()) {
            User inviter = userDao.findByInviteCode(inviteCode.trim());
            if (inviter != null) user.setInvitedBy(inviter.getId());
        }
        boolean ok = userDao.insertWithInvite(user) > 0;
        // 给邀请人发奖励
        if (ok && user.getInvitedBy() != null) {
            inviteDao.insert(user.getInvitedBy(), user.getId(), "100云梦豆");
        }
        return ok;
    }

    @Override
    public boolean register(User user) {
        return register(user, null);
    }

    @Override public boolean updateNickAvatar(User user) { return userDao.updateInfo(user) > 0; }
    @Override public User getById(Integer id) { return userDao.findById(id); }

    @Override
    public int buyMember(Integer userId, java.math.BigDecimal price, String level) {
        User user = userDao.findById(userId);
        if (user == null) return -1;
        String cur = user.getMemberLevel();
        String[] ranks = {"normal", "bronze", "silver", "gold"};
        int curIdx = java.util.Arrays.asList(ranks).indexOf(cur != null ? cur : "normal");
        int tgtIdx = java.util.Arrays.asList(ranks).indexOf(level);
        if (tgtIdx <= curIdx) return -2;
        int rows = userDao.deductBalanceAndSetLevel(userId, price, level);
        return rows > 0 ? 1 : 0;
    }
}
