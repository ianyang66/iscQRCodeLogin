package cn.trunch.auth.service;

import cn.trunch.auth.dao.UserDao;
import cn.trunch.auth.entity.Message;
import cn.trunch.auth.entity.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LoginService {

    @Resource
    UserDao userDao;

    public Message checkPassword(String userId, String userPassword) {
        User user = userDao.getUserInfo(userId);
        if (null == userPassword)
            return new Message(201, "登錄失敗", new User());
        if (null == user)
            return new Message(202, "登錄失敗", new User());
        if (!userPassword.equals(user.getUserPassword()))
            return new Message(203, "登錄失敗", new User());
        return new Message(200, "登錄成功", user);
    }

    public Message getUserInfo(String userId) {
        User user = userDao.getUserInfo(userId);
        if (null == user)
            return new Message(201, "獲取使用者資訊失敗", new User());
        return new Message(200, "獲取使用者資訊成功", user);
    }
}
