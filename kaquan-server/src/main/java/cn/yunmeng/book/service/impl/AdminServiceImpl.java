package cn.yunmeng.book.service.impl;
import cn.yunmeng.book.mapper.AdminDao;
import cn.yunmeng.book.pojo.Admin;
import cn.yunmeng.book.pojo.Goods;
import cn.yunmeng.book.pojo.Orders;
import cn.yunmeng.book.pojo.User;
import cn.yunmeng.book.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {
    @Resource private AdminDao adminDao;
    @Resource private PasswordEncoder passwordEncoder;

    /** 验证密码：兼容旧 MD5 → 自动升级 BCrypt */
    private boolean checkAndUpgrade(Admin admin, String rawPwd) {
        String stored = admin.getPassword();
        if (stored == null) return false;
        if (stored.startsWith("$")) {
            return passwordEncoder.matches(rawPwd, stored);
        }
        // 旧 MD5 兼容迁移
        if (stored.length() == 32) {
            String md5 = DigestUtils.md5DigestAsHex(rawPwd.getBytes());
            if (stored.equals(md5)) {
                admin.setPassword(passwordEncoder.encode(rawPwd));
                adminDao.updateAdminPassword(admin.getId(), admin.getPassword());
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public Admin login(String name, String pwd) {
        Admin ad = adminDao.findAdmin(name);
        if (ad == null) return null;
        return checkAndUpgrade(ad, pwd) ? ad : null;
    }

    @Override public List<Goods> allGoods() { return adminDao.selectAllGoods(); }
    @Override public boolean addGoods(Goods g) { return adminDao.addGoods(g) > 0; }
    @Override public boolean editGoods(Goods g) { return adminDao.updateGoods(g) > 0; }
    @Override public boolean delGoods(Integer id) { return adminDao.delGoods(id) > 0; }

    @Override public List<User> allUser() { return adminDao.selectAllUser(); }

    @Override
    public boolean updateUser(User u) {
        if (u.getPassword() != null && !u.getPassword().trim().isEmpty()) {
            u.setPassword(passwordEncoder.encode(u.getPassword()));
        }
        return adminDao.updateUserAll(u) > 0;
    }

    @Override public boolean delUser(Integer id) { return adminDao.deleteUser(id) > 0; }
    @Override public List<Orders> allOrder() { return adminDao.selectAllOrder(); }
}
