package com.yibi.backend.model.dto.chart;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.yibi.backend.model.entity.Chart;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 帖子 ES 包装类
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 **/
// todo 取消注释开启 ES（须先配置 ES）
// @Document(indexName = "post")
@Data
public class ChartEsDTO implements Serializable {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * id
     */
    @Id
    private Long id;

    /**
     * 目标
     */
    @Field(type = FieldType.Text)
    private String goal;

    /**
     * 标题
     */
    @Field(type = FieldType.Text)
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

    /**
     * 创建用户 id
     */
    @Field(index = false, type = FieldType.Long)
    private Long userId;

    /**
     * 创建时间
     */
    @Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date createTime;

    /**
     * 更新时间
     */
    @Field(index = false, store = true, type = FieldType.Date, format = {}, pattern = DATE_TIME_PATTERN)
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;

    private static final Gson GSON = new Gson();

    /**
     * 对象转包装类
     *
     * @param chart
     * @return
     */
    public static ChartEsDTO objToDto(Chart chart) {
        if (chart == null) {
            return null;
        }
        ChartEsDTO postEsDTO = new ChartEsDTO();
        BeanUtils.copyProperties(chart, postEsDTO);
        return postEsDTO;
    }

    /**
     * 包装类转对象
     *
     * @param chartEsDTO
     * @return
     */
    public static Chart dtoToObj(ChartEsDTO chartEsDTO) {
        if (chartEsDTO == null) {
            return null;
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEsDTO, chart);
        return chart;
    }
}
