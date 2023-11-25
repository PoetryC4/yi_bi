package com.yibi.backend.model.dto.search;

import com.yibi.backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommonQueryRequest extends PageRequest implements Serializable {

    /**
     * 类型
     */
    private Boolean useEs;

    /**
     * 类型
     */
    private String category;

    /**
     * 搜索词
     */
    private String searchText;

    private static final long serialVersionUID = 1L;
}