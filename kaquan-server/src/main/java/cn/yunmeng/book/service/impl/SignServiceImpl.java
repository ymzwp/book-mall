package cn.yunmeng.book.service.impl;
import cn.yunmeng.book.mapper.SignDao;
import cn.yunmeng.book.pojo.UserSign;
import cn.yunmeng.book.service.SignService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;

@Service
public class SignServiceImpl implements SignService {
    @Resource private SignDao signDao;

    @Override public boolean isSign(Integer uid) { return signDao.getToday(uid) != null; }

    @Override
    @Transactional
    public boolean sign(Integer uid) {
        UserSign s = new UserSign();
        s.setUserId(uid);
        s.setSignDate(new Date());
        signDao.insertSign(s);
        signDao.addPoints(uid);  // 签到送 10 云梦豆
        return true;
    }
}
