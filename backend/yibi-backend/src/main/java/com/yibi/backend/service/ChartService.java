package com.yibi.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yibi.backend.model.dto.chart.ChartQueryRequest;
import com.yibi.backend.model.dto.post.PostQueryRequest;
import com.yibi.backend.model.entity.Chart;
import com.yibi.backend.model.entity.Post;
import com.yibi.backend.model.vo.ChartVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 帖子服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface ChartService extends IService<Chart> {

    /**
     * 校验
     *
     * @param chart
     * @param add
     */
    void validChart(Chart chart, boolean add);

    /**
     * 获取查询条件
     *
     * @param chartQueryRequest
     * @return
     */
    QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest);

    /**
     * 从 ES 查询
     *
     * @param chartQueryRequest
     * @return
     */
    Page<Chart> searchFromEs(ChartQueryRequest chartQueryRequest);

    /**
     * 获取帖子封装
     *
     * @param chart
     * @param request
     * @return
     */
    ChartVO getChartVO(Chart chart, HttpServletRequest request);

    /**
     * 分页获取帖子封装
     *
     * @param chartPage
     * @param request
     * @return
     */
    Page<ChartVO> getChartVOPage(Page<Chart> chartPage, HttpServletRequest request);
}
