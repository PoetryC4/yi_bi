package com.yibi.backend.model.dto.chart;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 更新请求
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class ChartUpdateRequest implements Serializable {

    /**
     * id
     */
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
     * 图表数据
     */
    private String chartData;

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
    private String genChart;

    private static final long serialVersionUID = 1L;
}