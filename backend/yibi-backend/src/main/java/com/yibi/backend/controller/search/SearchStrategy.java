package com.yibi.backend.controller.search;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yibi.backend.model.dto.search.CommonQueryRequest;

import javax.servlet.http.HttpServletRequest;

public interface SearchStrategy<T> {
    public Page<T> doSearch(CommonQueryRequest commonQueryRequest, HttpServletRequest request);
}
