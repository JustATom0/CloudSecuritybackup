package com.mcloud.realm;

import com.mcloud.model.RoleEntity;
import com.mcloud.model.UsersEntity;
import com.mcloud.repository.RoleRepository;
import com.mcloud.repository.UserRepository;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by vellerzheng on 2017/10/13.
 */


@Component
public class LoginRealm extends AuthorizingRealm{

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;


    /**
     * 获取身份信息，我们可以在这个方法中，从数据库获取该用户的权限和角色信息
     *     当调用权限验证时，就会调用此方法
     */
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {

       String username = (String) getAvailablePrincipal(principalCollection);

        RoleEntity roleEntity = null;

        try {
            UsersEntity userlogin =userRepository.findByUsernameEndsWith(username);
            //获取角色对象
            roleEntity = roleRepository.findRoleEntityById(userlogin.getUserRoleIdByRoleId().getRoleId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //通过用户名从数据库获取权限/角色信息
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        Set<String> r = new HashSet<String>();
        if (roleEntity != null) {
            r.add(roleEntity.getRoleName());
            info.setRoles(r);
        }

        return info;
    }

    /**
     * 在这个方法中，进行身份验证
     *         login时调用
     */
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authctoken) throws AuthenticationException {
        UsernamePasswordToken token =(UsernamePasswordToken) authctoken;
        //用户名
        String username = token.getUsername();
        //密码
        String password = new String((char[])authctoken.getCredentials());

        UsersEntity userLogin = null;
        try {
            userLogin = userRepository.findByUsernameEndsWith(username);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (userLogin == null) {
            //没有该用户名
            throw new UnknownAccountException();
        } else if (!password.equals(userLogin.getPassword())) {
            //密码错误
            throw new IncorrectCredentialsException();
        }
        Object principal=token.getUsername();
        String credentials =userLogin.getPassword();
        ByteSource credentialsSalt=ByteSource.Util.bytes(userLogin.getUsername());
        //身份验证通过,返回一个身份信息
        AuthenticationInfo aInfo = new SimpleAuthenticationInfo(principal,credentials,credentialsSalt,getName());

        return aInfo;
    }
}