package cn.yunmeng.book.controller;
import cn.yunmeng.book.config.LoginRateLimiter;
import cn.yunmeng.book.mapper.InviteDao;
import cn.yunmeng.book.pojo.User;
import cn.yunmeng.book.service.SignService;
import cn.yunmeng.book.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
/**前台用户接口：登录、注册、改资料、签到、获取登录用户、退出*/
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource private UserService userService;
    @Resource private SignService signService;
    @Resource private LoginRateLimiter rateLimiter;
    @Resource private InviteDao inviteDao;
    @Resource private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /** 获取客户端真实IP */
    private String clientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }

    //登录接口（含限流保护）
    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody User user, HttpSession session, HttpServletRequest req){
        Map<String,Object> map=new HashMap<>();
        String ip = clientIp(req);
        if (!rateLimiter.allow(ip)) {
            map.put("code", 429);
            map.put("msg", "登录过于频繁，请1分钟后再试");
            return map;
        }
        User u=userService.login(user.getUsername(),user.getPassword());
        if(u!=null){
            rateLimiter.clear(ip);
            session.setAttribute("loginUser",u);
            map.put("code",200);
            map.put("data",u);
        }else map.put("code",500);
        return map;
    }
    //注册（含限流防刷 + 邀请码）
    @PostMapping("/reg")
    public Map<String,Object> reg(@RequestBody Map<String,Object> body, HttpServletRequest req){
        Map<String,Object> map=new HashMap<>();
        String ip = clientIp(req);
        if (!rateLimiter.allow(ip)) {
            map.put("code", 429); map.put("msg", "操作频繁，请稍后再试"); return map;
        }
        String username = (String) body.get("username");
        String password = (String) body.get("password");
        String inviteCode = (String) body.get("inviteCode");
        if (username == null || username.trim().length() < 2) {
            map.put("code", 400); map.put("msg", "账号至少2个字符"); return map;
        }
        if (password == null || password.trim().length() < 6) {
            map.put("code", 400); map.put("msg", "密码至少6位"); return map;
        }
        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(password);
        boolean b = inviteCode != null && !inviteCode.isBlank()
                ? userService.register(user, inviteCode.trim())
                : userService.register(user);
        map.put("code", b ? 200 : 500);
        if (!b) map.put("msg", "注册失败，账号可能已存在");
        return map;
    }

    /** 邀请统计 */
    @GetMapping("/invite")
    public Map<String,Object> inviteStats(HttpSession session){
        Map<String,Object> map = new HashMap<>();
        User login = (User) session.getAttribute("loginUser");
        if (login == null) { map.put("code", 501); return map; }
        map.put("code", 200);
        map.put("data", Map.of(
            "inviteCode", login.getInviteCode() != null ? login.getInviteCode() : "",
            "count", inviteDao.countByInviter(login.getId()),
            "list", inviteDao.findByInviter(login.getId())
        ));
        return map;
    }
    //修改昵称头像
    @PostMapping("/update")
    public Map<String,Object> update(@RequestBody User user,HttpSession session){
        User login=(User)session.getAttribute("loginUser");
        Map<String,Object> map=new HashMap<>();
        if(login==null){map.put("code",501);return map;}
        user.setId(login.getId());
        boolean b=userService.updateNickAvatar(user);
        map.put("code",b?200:500);
        return map;
    }
    //查询今日是否已签到
    @PostMapping("/sign/check")
    public Map<String,Object> signCheck(HttpSession session){
        User login=(User)session.getAttribute("loginUser");
        Map<String,Object> map=new HashMap<>();
        if(login==null){map.put("code",501);return map;}
        map.put("code",200);
        map.put("data",Map.of("signed",signService.isSign(login.getId())));
        return map;
    }
    //签到
    @PostMapping("/sign")
    public Map<String,Object> sign(HttpSession session){
        User login=(User)session.getAttribute("loginUser");
        Map<String,Object> map=new HashMap<>();
        if(login==null){map.put("code",501);return map;}
        if(signService.isSign(login.getId())){
            map.put("code",502);map.put("msg","今日已签到");
            return map;
        }
        boolean ok=signService.sign(login.getId());
        if(ok){
            User fresh = userService.getById(login.getId());
            map.put("code",200);
            map.put("msg","签到成功，+10云梦豆");
            map.put("data",Map.of("points",fresh.getPoints()));
        } else {
            map.put("code",500);
            map.put("msg","签到失败");
        }
        return map;
    }
    //获取当前登录用户（从数据库重新查询，确保余额等实时数据正确）
    @PostMapping("/getLogin")
    public Map<String,Object> getLogin(HttpSession session){
        Map<String,Object> map=new HashMap<>();
        User u=(User)session.getAttribute("loginUser");
        if(u==null){map.put("code",500);return map;}
        // 从数据库重新查询，获取最新余额
        User fresh = userService.getById(u.getId());
        map.put("code",fresh==null?500:200);
        map.put("data",fresh);
        return map;
    }
    //购买会员（扣余额+升级，需密码确认）
    @PostMapping("/buyMember")
    public Map<String,Object> buyMember(@RequestBody Map<String,Object> body, HttpSession session){
        Map<String,Object> map=new HashMap<>();
        User login=(User)session.getAttribute("loginUser");
        if(login==null){map.put("code",501);map.put("msg","请先登录");return map;}

        // 敏感操作：密码确认
        Object pwdObj = body.get("password");
        if (pwdObj == null || !passwordEncoder.matches(pwdObj.toString(), login.getPassword())) {
            map.put("code", 403); map.put("msg", "密码错误，操作取消"); return map;
        }

        // 参数：price, level
        Object priceObj = body.get("price");
        Object levelObj = body.get("level");
        if(priceObj==null||levelObj==null){map.put("code",400);map.put("msg","参数错误");return map;}
        java.math.BigDecimal price = new java.math.BigDecimal(priceObj.toString());
        String level = levelObj.toString();
        // 从数据库查最新等级，避免 session 旧数据
        User fresh = userService.getById(login.getId());
        String curLevel = fresh != null ? fresh.getMemberLevel() : "normal";
        String curLevelName = levelName(curLevel);
        String tgtLevelName = levelName(level);

        int r = userService.buyMember(login.getId(), price, level);
        if(r==1){map.put("code",200);map.put("msg","购买成功，已升级为"+tgtLevelName);}
        else if(r==0){map.put("code",500);map.put("msg","余额不足，请先充值");}
        else if(r==-1){map.put("code",500);map.put("msg","用户不存在");}
        else {map.put("code",500);map.put("msg","您当前已是"+curLevelName+"，无需购买"+tgtLevelName);}
        return map;
    }

    private static String levelName(String level) {
        switch (level) {
            case "bronze": return "青铜会员";
            case "silver": return "白银会员";
            case "gold": return "黄金会员";
            default: return "普通会员";
        }
    }

    //退出登录
    @PostMapping("/logout")
    public Map<String,Object> logout(HttpSession session){
        session.invalidate();
        return Map.of("code",200);
    }
}