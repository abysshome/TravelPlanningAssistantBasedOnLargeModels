package org.example.backend_1.controller;

import org.example.backend_1.pojo.LoginReturn;
import org.example.backend_1.pojo.Result;
import org.example.backend_1.pojo.User;
import org.example.backend_1.service.UserService;
import org.example.backend_1.utils.AliOSSUtils;
import org.example.backend_1.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private AliOSSUtils aliOSSUtils;

    @GetMapping("/login")
    public Result login(User user){
        System.out.println(user);
        User u=userService.login(user);
        if(u!=null){
            //自定义信息
            Map<String,Object> claims=new HashMap<>();
            claims.put("id",u.getId());
            claims.put("username",u.getUsername());
            claims.put("phone",u.getPhone());

            //使用JWT工具类，生成身份令牌
            String token= JwtUtils.generateJwt(claims);

            return Result.success(u);
        }
        return Result.error("用户名或密码错误");
    }

    @PostMapping("/register")
    public Result register(User user){
        System.out.println(user);
        userService.register(user);
        User u=userService.getUserById(user.getId());
        if(u!=null) {
            return Result.success(u);
        } else {
            return Result.error("注册失败");
        }
    }

    @GetMapping("/preferences")
    public Result preferencesGet(Integer id){
        User u=userService.preferenceGet(id);
        if(u!=null) {
            return Result.success(u);
        } else {
            return Result.error("获取喜好失败");
        }
    }

    @PostMapping("/preferences")
    public Result preferencesCollect(User user){
        System.out.println(user);
        User u=userService.preferencesCollect(user);
        if(u!=null) {
            return Result.success(user);
        } else {
            return Result.error("喜好添加失败");
        }
    }

    @PostMapping("/uploadImage")
    public Result uploadImage(Integer id, MultipartFile image)throws IOException {
        String imgUrl=aliOSSUtils.upload(image);
        userService.updateInfoById(id,imgUrl);
        return Result.success(userService.getUserById(id));

    }
}
