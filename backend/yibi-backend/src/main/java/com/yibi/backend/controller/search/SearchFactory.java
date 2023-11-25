package com.yibi.backend.controller.search;

import com.yibi.backend.common.ErrorCode;
import com.yibi.backend.controller.search.impl.SearchStrategyChartEs;
import com.yibi.backend.controller.search.impl.SearchStrategyPostDefault;
import com.yibi.backend.controller.search.impl.SearchStrategyPostEs;
import com.yibi.backend.controller.search.impl.SearchStrategyChartDefault;
import com.yibi.backend.exception.BusinessException;
import com.yibi.backend.model.enums.SearchTypeEnum;

public class SearchFactory {
    public static SearchStrategy newInstance(String category, Boolean useEs) {
        if (useEs) {
            if (SearchTypeEnum.CHART.getValue().equals(category)) {
                return new SearchStrategyChartEs();
            } else if (SearchTypeEnum.POST.getValue().equals(category)) {
                return new SearchStrategyPostEs();
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        } else {
            if (SearchTypeEnum.CHART.getValue().equals(category)) {
                return new SearchStrategyChartDefault();
            } else if (SearchTypeEnum.POST.getValue().equals(category)) {
                return new SearchStrategyPostDefault();
            } else {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
    }
}
