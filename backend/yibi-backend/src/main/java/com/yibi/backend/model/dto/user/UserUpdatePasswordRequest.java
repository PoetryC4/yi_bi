package com.yibi.backend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户修改密码请求体
 *
 */
@Data
public class UserUpdatePasswordRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userPassword;

    private String checkPassword;

    private String userEmail;

    private String emailVerifyCode;
}
