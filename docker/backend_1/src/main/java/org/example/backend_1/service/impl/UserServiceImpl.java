package org.example.backend_1.service.impl;

import org.example.backend_1.mapper.UserMapper;
import org.example.backend_1.pojo.User;
import org.example.backend_1.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public User login(User user) {
        return userMapper.getByUsernameAndPassword(user);
    }

    @Override
    public void register(User user) {
        userMapper.addNewUser(user);
    }

    @Override
    public User preferenceGet(Integer id) {
        return userMapper.getById(id);
    }

    @Override
    public User preferencesCollect(User user) {
        userMapper.addPreference(user);
        return userMapper.getByUsernameAndPassword(user);
    }

    @Override
    public User getUserById(Integer id) {
        return userMapper.getUserById(id);
    }

    @Override
    public void updateInfoById(Integer id, String imgUrl) {
        userMapper.updateInfoById(id, imgUrl);
    }
}
