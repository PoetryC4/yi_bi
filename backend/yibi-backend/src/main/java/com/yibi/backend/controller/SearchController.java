package com.yibi.backend.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.gson.Gson;
import com.yibi.backend.common.BaseResponse;
import com.yibi.backend.common.ErrorCode;
import com.yibi.backend.common.ResultUtils;
import com.yibi.backend.controller.search.SearchFactory;
import com.yibi.backend.controller.search.SearchStrategy;
import com.yibi.backend.exception.BusinessException;
import com.yibi.backend.exception.ThrowUtils;
import com.yibi.backend.model.dto.search.CommonQueryRequest;
import com.yibi.backend.service.PostService;
import com.yibi.backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.yibi.backend.utils.SqlUtils.isAnyNull;

@Slf4j
@RestController
@RequestMapping("/search")
public class SearchController {

    @PostMapping("/list")
    public BaseResponse<IPage> listObjectByPage(@RequestBody CommonQueryRequest commonQueryRequest,
                                                HttpServletRequest request) {
        if (commonQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = commonQueryRequest.getCurrent();
        long size = commonQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 25, ErrorCode.PARAMS_ERROR);
        String category = commonQueryRequest.getCategory();
        Boolean useEs = commonQueryRequest.getUseEs();
        if (isAnyNull(category, useEs)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        SearchStrategy searchStrategy = SearchFactory.newInstance(category, useEs);
        return ResultUtils.success(searchStrategy.doSearch(commonQueryRequest, request));
    }

}
