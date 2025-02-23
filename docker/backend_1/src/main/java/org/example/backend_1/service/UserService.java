package org.example.backend_1.service;

import org.example.backend_1.pojo.User;

public interface UserService {
    User login(User user);

    void register(User user);

    User preferenceGet(Integer  id);

    User preferencesCollect(User user);

    User getUserById(Integer id);

    void updateInfoById(Integer id, String imgUrl);
}
