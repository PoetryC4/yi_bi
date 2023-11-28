package com.yibi.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yibi.backend.common.ErrorCode;
import com.yibi.backend.constant.CommonConstant;
import com.yibi.backend.exception.BusinessException;
import com.yibi.backend.exception.ThrowUtils;
import com.yibi.backend.mapper.ChartMapper;
import com.yibi.backend.model.dto.chart.ChartEsDTO;
import com.yibi.backend.model.dto.chart.ChartQueryRequest;
import com.yibi.backend.model.entity.Chart;
import com.yibi.backend.model.entity.User;
import com.yibi.backend.model.vo.ChartVO;
import com.yibi.backend.model.vo.UserVO;
import com.yibi.backend.service.ChartService;
import com.yibi.backend.service.UserService;
import com.yibi.backend.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子服务实现
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    private final static Gson GSON = new Gson();

    @Resource
    private UserService userService;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public void validChart(Chart chart, boolean add) {
        if (chart == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String goal = chart.getGoal();
        String chartData = chart.getTitle();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(goal, chartData), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(goal) && goal.length() > 800) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = chartQueryRequest.getSearchText();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        Long id = chartQueryRequest.getId();
        Long userId = chartQueryRequest.getUserId();
        String goal = chartQueryRequest.getGoal();
        String chartData = chartQueryRequest.getTitle();

        // 拼接查询条件
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.like("goal", searchText).or().like("chartData", searchText);
        }
        queryWrapper.like(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.like(StringUtils.isNotBlank(chartData), "chartData", chartData);

        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<Chart> searchFromEs(ChartQueryRequest chartQueryRequest) {
        Long id = chartQueryRequest.getId();
        String searchText = chartQueryRequest.getSearchText();
        Long userId = chartQueryRequest.getUserId();
        String goal = chartQueryRequest.getGoal();
        String chartData = chartQueryRequest.getTitle();

        // es 起始页为 0
        long current = chartQueryRequest.getCurrent() - 1;
        long pageSize = chartQueryRequest.getPageSize();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder);
        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {

            BoolQueryBuilder boolQueryBuilder1 = new BoolQueryBuilder();

            boolQueryBuilder1.should(QueryBuilders.fuzzyQuery("goal", searchText).fuzziness(Fuzziness.ONE));
            boolQueryBuilder1.should(QueryBuilders.matchPhraseQuery("goal", searchText));
            boolQueryBuilder1.should(QueryBuilders.matchPhrasePrefixQuery("goal", searchText));
            boolQueryBuilder1.should(QueryBuilders.fuzzyQuery("chartData", searchText).fuzziness(Fuzziness.ONE));
            boolQueryBuilder1.should(QueryBuilders.matchPhraseQuery("chartData", searchText));
            boolQueryBuilder1.should(QueryBuilders.matchPhrasePrefixQuery("chartData", searchText));
            boolQueryBuilder1.minimumShouldMatch(1);

            searchQueryBuilder.withQuery(boolQueryBuilder1);
        }
        searchQueryBuilder.withPageable(pageRequest)
                .withHighlightFields(new HighlightBuilder.Field("goal").preTags("<span style='background-color:yellow'>").postTags("</span>").requireFieldMatch(false)) // 加高亮
                .withHighlightFields(new HighlightBuilder.Field("content").preTags("<span style='background-color:yellow'>").postTags("</span>").requireFieldMatch(false)) // 加高亮
                .withSorts(sortBuilder);
        /*NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest).withSorts(sortBuilder).build();*/
        NativeSearchQuery searchQuery = searchQueryBuilder.build();
        SearchHits<ChartEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, ChartEsDTO.class);
        Page<Chart> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Chart> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        if (searchHits.hasSearchHits()) {
            searchHits.getSearchHits().forEach(searchHit -> {
                Long chartId = searchHit.getContent().getId();
                Chart chart = this.baseMapper.selectById(chartId);
                if (chart == null) {
                    // 从 es 清空 db 已物理删除的数据
                    String delete = elasticsearchRestTemplate.delete(String.valueOf(chartId), ChartEsDTO.class);
                    log.info("delete chart {}", delete);
                } else {
                    if (searchHit.getHighlightFields().containsKey("goal")) {
                        chart.setGoal(searchHit.getHighlightFields().get("goal").get(0));
                    }
                    if (searchHit.getHighlightFields().containsKey("chartData")) {
                        chart.setTitle(searchHit.getHighlightFields().get("chartData").get(0));
                    }
                    resourceList.add(chart);
                }
            });
        }
        page.setRecords(resourceList);
        return page;
    }

    @Override
    public ChartVO getChartVO(Chart chart, HttpServletRequest request) {
        ChartVO chartVO = ChartVO.objToVo(chart);
        long chartId = chart.getId();
        // 1. 关联查询用户信息
        Long userId = chart.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        chartVO.setUser(userVO);
        if (!userService.isAdmin(request)) {
            chartVO.setChatHistoryList(null);
        }
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        return chartVO;
    }

    @Override
    public Page<ChartVO> getChartVOPage(Page<Chart> chartPage, HttpServletRequest request) {
        List<Chart> chartList = chartPage.getRecords();
        Page<ChartVO> chartVOPage = new Page<>(chartPage.getCurrent(), chartPage.getSize(), chartPage.getTotal());
        if (CollectionUtils.isEmpty(chartList)) {
            return chartVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = chartList.stream().map(Chart::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        // 填充信息
        List<ChartVO> chartVOList = chartList.stream().map(chart -> {
            ChartVO chartVO = ChartVO.objToVo(chart);
            Long userId = chart.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            chartVO.setUser(userService.getUserVO(user));
            return chartVO;
        }).collect(Collectors.toList());
        chartVOPage.setRecords(chartVOList);
        return chartVOPage;
    }

}




