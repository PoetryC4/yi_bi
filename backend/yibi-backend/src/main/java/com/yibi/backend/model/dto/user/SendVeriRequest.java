package com.yibi.backend.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 *
 */
@Data
public class SendVeriRequest implements Serializable {

    private static final long serialVersionUID = 3191241716373120793L;

    private String userEmail;
}
