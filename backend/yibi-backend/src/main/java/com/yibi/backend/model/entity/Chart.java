package com.yibi.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.yibi.backend.model.dto.chatglm.ChatHistory;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@TableName(value = "chart")
@Data
public class Chart implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 目标
     */
    private String goal;

    /**
     * 标题
     */
    private String title;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 结果文字
     */
    private String genText;

    /**
     * 生成图表
     */
    private String genCode;

    /**
     * 历史
     */
    private List<ChatHistory> chatHistoryList;

    /**
     * 创建用户
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 是否为VIP
     */
    private Integer isVip;
    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}