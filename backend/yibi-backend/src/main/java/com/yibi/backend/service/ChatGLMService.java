package com.yibi.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yibi.backend.model.dto.chart.ChartQueryRequest;
import com.yibi.backend.model.dto.chatglm.ChatGLMRequest;
import com.yibi.backend.model.entity.Chart;
import com.yibi.backend.model.vo.ChartVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 帖子服务
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface ChatGLMService {
    String getResponseFromGLM(ChatGLMRequest chatGLMRequest);
}
