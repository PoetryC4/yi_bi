package com.yibi.backend.controller.search.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibi.backend.controller.search.SearchStrategy;
import com.yibi.backend.model.dto.chart.ChartQueryRequest;
import com.yibi.backend.model.dto.post.PostQueryRequest;
import com.yibi.backend.model.dto.search.CommonQueryRequest;
import com.yibi.backend.model.dto.user.UserQueryRequest;
import com.yibi.backend.model.entity.Chart;
import com.yibi.backend.model.entity.Post;
import com.yibi.backend.model.entity.User;
import com.yibi.backend.model.vo.ChartVO;
import com.yibi.backend.model.vo.PostVO;
import com.yibi.backend.model.vo.UserVO;
import com.yibi.backend.service.ChartService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Component
public class SearchStrategyChartDefault implements SearchStrategy {
    private static SearchStrategyChartDefault searchStrategyChartDefault;

    @Resource
    private ChartService chartService;

    @PostConstruct //通过@PostConstruct实现初始化bean之前进行的操作
    public void init() {
        searchStrategyChartDefault = this;
        searchStrategyChartDefault.chartService = this.chartService;
        // 初使化时将已静态化的testService实例化
    }
    @Override
    public Page<ChartVO> doSearch(CommonQueryRequest commonQueryRequest, HttpServletRequest request) {
        ChartQueryRequest chartQueryRequest = new ChartQueryRequest();
        BeanUtils.copyProperties(commonQueryRequest, chartQueryRequest);
        chartQueryRequest.setSearchText(commonQueryRequest.getSearchText());

        Page<Chart> chartPage = searchStrategyChartDefault.chartService.page(new Page<>(commonQueryRequest.getCurrent(), commonQueryRequest.getPageSize()),
                searchStrategyChartDefault.chartService.getQueryWrapper(chartQueryRequest));
        Page<ChartVO> chartVOPage = searchStrategyChartDefault.chartService.getChartVOPage(chartPage, request);
        return chartVOPage;
    }
}
