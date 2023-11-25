package com.yibi.backend.model.vo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yibi.backend.model.entity.Chart;
import com.yibi.backend.model.entity.Post;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子视图
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class ChartVO implements Serializable {

    private final static Gson GSON = new Gson();

    /**
     * id
     */
    private Long id;

    /**
     * 目标
     */
    private String goal;

    /**
     * 输入图表数据
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

    /**
     * 创建用户 id
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
     * 创建人信息
     */
    private UserVO user;

    /**
     * 是否已点赞
     */
    private Boolean hasThumb;

    /**
     * 是否已收藏
     */
    private Boolean hasFavour;

    /**
     * 包装类转对象
     *
     * @param chartVO
     * @return
     */
    public static Chart voToObj(ChartVO chartVO) {
        if (chartVO == null) {
            return null;
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartVO, chart);
        return chart;
    }

    /**
     * 对象转包装类
     *
     * @param chart
     * @return
     */
    public static ChartVO objToVo(Chart chart) {
        if (chart == null) {
            return null;
        }
        ChartVO chartVO = new ChartVO();
        BeanUtils.copyProperties(chart, chartVO);
        return chartVO;
    }
}
