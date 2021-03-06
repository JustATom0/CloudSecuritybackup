package com.mcloud.controller;

import com.mcloud.model.UserAdviceEntity;
import com.mcloud.model.UsersEntity;
import com.mcloud.repository.UserAdviceRepository;
import com.mcloud.repository.UserRepository;
import com.mcloud.util.common.BloomFilterUtils;
import com.mcloud.util.common.CustomDateConverter;
import com.mcloud.util.common.InfoJson;
import com.mcloud.util.redis.RedisUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by vellerzheng on 2017/9/20.
 */
@Controller
public class BaseController {

    @Autowired
    UserAdviceRepository userAdviceRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    RedisUtil redisUtil;

    @PostConstruct
    public void initUserInfoToRedis(){
        //将用户信息导入redis
        List<UsersEntity> usersInfo = userRepository.findAll();
        for(UsersEntity user : usersInfo){
            //初始化布隆过滤器
            BloomFilterUtils.create(user.getUsername());
            redisUtil.setEx(user.getUsername(),3600000,user);
        }
    }

    @RequestMapping(value = "/",method = RequestMethod.GET)
    public  String index() {

        return "index.html";
    }

    @RequestMapping(value ="/clouds/error", method = RequestMethod.GET)
    public String getError(){
        return "clouds/error";
    }

    @RequestMapping(value ="/clouds/home", method = RequestMethod.GET)
    public String getHome(){
        return "clouds/home";
    }


    @RequestMapping(value = "/clouds/users/admin/welcomeAdmin/{userName}", method = RequestMethod.GET)
    public String getWelcomeAdmin(ModelMap modelMap, @PathVariable("userName")String userName) {
        UsersEntity utyAdmin = (UsersEntity) redisUtil.get(userName);
        modelMap.addAttribute("loginUser",utyAdmin);
        return "clouds/users/admin/welcomeAdmin";
    }

    @RequestMapping(value = "/clouds/users/default/welcome/{userName}", method = RequestMethod.GET)
    public String getWelcome(ModelMap modelMap, @PathVariable("userName")String userName) {
        UsersEntity ordinaryUser = (UsersEntity) redisUtil.get(userName);
        modelMap.addAttribute("loginUser",ordinaryUser);
        return "clouds/users/default/welcome";
    }

    @RequestMapping(value = "/clouds/users/top")
    public String getTop() { return "clouds/utils/account"; }

    @RequestMapping(value = "/clouds/users/logout", method = RequestMethod.GET)
    public String logOut() { return "clouds/users/logout"; }

    @RequestMapping(value = "/clouds/users/passwordReset", method = RequestMethod.GET)
    public String getPasswordReset(ModelMap modelMap) {
        Subject subject = SecurityUtils.getSubject();
        String  username = (String) subject.getPrincipal();
        UsersEntity loginUser = (UsersEntity) redisUtil.get(username);
        modelMap.addAttribute("loginUser",loginUser);
        return "clouds/users/passwordReset";
    }

    @RequestMapping(value = "/js/AJAX.js/adviceUpload",method = RequestMethod.POST)
    @ResponseBody
    public String getPublicAdvice(HttpServletRequest request, HttpServletResponse response, @RequestBody UserAdviceEntity userAdviceEntity) throws IOException {


        String email = userAdviceEntity.getEmail();
        String name = userAdviceEntity.getName();
        String idea =userAdviceEntity.getMainIdea();
        String message = userAdviceEntity.getMessageDetail();
        userAdviceEntity.setSubmitTime(CustomDateConverter.currentTime());
        userAdviceRepository.saveAndFlush(userAdviceEntity);

        return "upload advice successfully!";
    }

    @RequestMapping(value = "/clouds/users/default/cloudConfig")
    public String cloudConfig() { return "/clouds/users/default/cloudConfig"; }

    /*用户上传视频配置页面 */
    @RequestMapping(value ="/clouds/filemanager/uploadMedia/{userName}", method = RequestMethod.GET)
    public String getUploadForm(@PathVariable("userName") String username, ModelMap modelMap){
        UsersEntity usersEntity = (UsersEntity) redisUtil.get(username);
        modelMap.addAttribute("authUsersEntity",usersEntity.getUsername());
        modelMap.addAttribute("loginUser",usersEntity);
        return "clouds/filemanager/uploadMedia";
    }

}

