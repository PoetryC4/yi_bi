package com.yibi.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yibi.backend.constant.UserConstant;
import com.yibi.backend.exception.BusinessException;
import com.yibi.backend.common.ErrorCode;
import com.yibi.backend.constant.CommonConstant;
import com.yibi.backend.mapper.UserMapper;
import com.yibi.backend.model.dto.user.UserQueryRequest;
import com.yibi.backend.model.entity.User;
import com.yibi.backend.model.enums.UserRoleEnum;
import com.yibi.backend.model.vo.LoginUserVO;
import com.yibi.backend.model.vo.UserVO;
import com.yibi.backend.service.UserService;
import com.yibi.backend.utils.EmailCodeUtils;
import com.yibi.backend.utils.SqlUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.WxOAuth2UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import static com.yibi.backend.utils.EncryptionUtils.getRandomString;

/**
 * 用户服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yupi";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userEmail, String emailVerifyCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userEmail, emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
       /* String regex = "^[A-Za-z0-9+_.-]+@(.+)$";

        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);

        // 创建Matcher对象
        Matcher matcher = pattern.matcher(userEmail);

        // 使用find()方法查找匹配项
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }*/
        EmailCodeUtils.CodeEmailPair codeEmailPair = EmailCodeUtils.getPair(userEmail);
        if (codeEmailPair == null || System.currentTimeMillis() - codeEmailPair.getCreateTime() >= 300 * 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码过期或未发送验证码");
        } else if (!Objects.equals(codeEmailPair.getVerificationCode(), emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不正确");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("userEmail", userEmail);
            long count1 = this.baseMapper.selectCount(queryWrapper1);
            if (count1 > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱重复");
            }
            // 2. 加密
            String salt = getRandomString(12);
            String encryptPassword = DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserSalt(salt);
            user.setUserEmail(userEmail);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            EmailCodeUtils.removePair(userEmail);
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, String userEmail, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword) && StringUtils.isAnyBlank(userEmail, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4 && userEmail.length() < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        if (Objects.equals(userEmail, "")) {
            QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("userAccount", userAccount);
            User user1 = this.baseMapper.selectOne(queryWrapper1);
            if (user1 == null || user1.getId() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不存在");
            }
            String encryptPassword = DigestUtils.md5DigestAsHex((user1.getUserSalt() + userPassword).getBytes());
            // 查询用户是否存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            queryWrapper.eq("userPassword", encryptPassword);
            User user = this.baseMapper.selectOne(queryWrapper);
            // 用户不存在
            if (user == null) {
                log.info("user login failed, userAccount cannot match userPassword");
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
            // 3. 记录用户的登录态
            request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
            return this.getLoginUserVO(user);
        } else {

           /* String regex = "^[A-Za-z0-9+_.-]+@(.+)$";

            // 编译正则表达式
            Pattern pattern = Pattern.compile(regex);

            // 创建Matcher对象
            Matcher matcher = pattern.matcher(userEmail);

            // 使用find()方法查找匹配项
            if (!matcher.find()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
            }*/
            QueryWrapper<User> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("userEmail", userEmail);
            User user1 = this.baseMapper.selectOne(queryWrapper1);
            if (user1 == null || user1.getId() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户不存在");
            }
            String encryptPassword = DigestUtils.md5DigestAsHex((user1.getUserSalt() + userPassword).getBytes());
            // 查询用户是否存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userEmail", userEmail);
            queryWrapper.eq("userPassword", encryptPassword);
            User user = this.baseMapper.selectOne(queryWrapper);
            // 用户不存在
            if (user == null) {
                log.info("user login failed, userAccount cannot match userPassword");
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
            }
            // 3. 记录用户的登录态
            request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
            return this.getLoginUserVO(user);
        }
    }
    @Override
    public LoginUserVO userLoginByMpOpen(WxOAuth2UserInfo wxOAuth2UserInfo, HttpServletRequest request) {
        String unionId = wxOAuth2UserInfo.getUnionId();
        String mpOpenId = wxOAuth2UserInfo.getOpenid();
        // 单机锁
        synchronized (unionId.intern()) {
            // 查询用户是否已存在
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("unionId", unionId);
            User user = this.getOne(queryWrapper);
            // 被封号，禁止登录
            if (user != null && UserRoleEnum.BAN.getValue().equals(user.getUserRole())) {
                throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "该用户已被封，禁止登录");
            }
            // 用户不存在则创建
            if (user == null) {
                user = new User();
                user.setUnionId(unionId);
                user.setMpOpenId(mpOpenId);
                user.setUserAvatar(wxOAuth2UserInfo.getHeadImgUrl());
                user.setUserName(wxOAuth2UserInfo.getNickname());
                boolean result = this.save(user);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "登录失败");
                }
            }
            // 记录用户的登录态
            request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
            return getLoginUserVO(user);
        }
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public boolean userUpdatePassword(String userPassword, String checkPassword, String userEmail, String emailVerifyCode) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userPassword, checkPassword, userEmail, emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        QueryWrapper<User> queryWrapper3 = new QueryWrapper<>();
        queryWrapper3.eq("userEmail", userEmail);
        long count23 = this.baseMapper.selectCount(queryWrapper3);
        if (count23 == 0) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "邮箱不存在");
        }
       /* String regex = "^[A-Za-z0-9+_.-]+@(.+)$";

        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);

        // 创建Matcher对象
        Matcher matcher = pattern.matcher(userEmail);

        // 使用find()方法查找匹配项
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式错误");
        }*/
        EmailCodeUtils.CodeEmailPair codeEmailPair = EmailCodeUtils.getPair(userEmail);
        if (codeEmailPair == null || System.currentTimeMillis() - codeEmailPair.getCreateTime() >= 300 * 1000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码过期或未发送验证码");
        } else if (!Objects.equals(codeEmailPair.getVerificationCode(), emailVerifyCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码不正确");
        }
        synchronized (userEmail.intern()) {
            // 1. 获取用户Id
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userEmail", userEmail);
            User user1 = this.baseMapper.selectOne(queryWrapper);
            if(user1 == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
            }
            // 2. 加密
            String salt = getRandomString(12);
            String encryptPassword = DigestUtils.md5DigestAsHex((salt + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setId(user1.getId());
            user.setUserPassword(encryptPassword);
            user.setUserSalt(salt);
            user.setUserEmail(userEmail);
            boolean update = this.updateById(user);
            if (!update) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改密码失败");
            }
            EmailCodeUtils.removePair(userEmail);
            return update;
        }
    }
}
