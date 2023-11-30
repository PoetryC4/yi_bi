package com.yibi.backend.controller;

import com.google.gson.Gson;
import com.yibi.backend.annotation.AuthCheck;
import com.yibi.backend.common.BaseResponse;
import com.yibi.backend.common.DeleteRequest;
import com.yibi.backend.common.ErrorCode;
import com.yibi.backend.common.ResultUtils;
import com.yibi.backend.config.ThreadPoolConfig;
import com.yibi.backend.constant.UserConstant;
import com.yibi.backend.exception.BusinessException;
import com.yibi.backend.exception.ThrowUtils;
import com.yibi.backend.manager.RateLimiterManager;
import com.yibi.backend.model.dto.chart.ChartAddRequest;
import com.yibi.backend.model.dto.chart.ChartEditRequest;
import com.yibi.backend.model.dto.chart.ChartUpdateRequest;
import com.yibi.backend.model.dto.chatglm.ChatGLMRequest;
import com.yibi.backend.model.dto.chatglm.ChatGLMResponse;
import com.yibi.backend.model.dto.chatglm.ChatHistory;
import com.yibi.backend.model.entity.Chart;
import com.yibi.backend.model.entity.User;
import com.yibi.backend.model.enums.ChartStateEnum;
import com.yibi.backend.model.vo.ChartVO;
import com.yibi.backend.init.rabbitmq.MyMessageProducer;
import com.yibi.backend.service.ChartService;
import com.yibi.backend.service.ChatGLMService;
import com.yibi.backend.service.UserService;
import com.yibi.backend.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {

    @Resource
    private RateLimiterManager rateLimiterManager;

    @Resource
    private MyMessageProducer myMessageProducer;

    @Value("${llm.model-name}")
    private String modelName;

    @Value("${llm.prompt-name}")
    private String promptName;

    private static List<ChatHistory> codeGenHistory;

    private static String firstQueryTemplate = "分析需求:\n%s %s \n原始数据:\n%s";

    private static String chartTypeTemplate = "\n指定结果图表类型:\n%s";

    //private static String secondQueryTemplate = "结合以上生成的图表类型和相关表象，但不要出现任何代码相关内容，而只分析图表，我有以下需求: \n%s";

    private static String secondQueryTemplate = "结合生成的图表，但不要出现任何代码相关内容，用纯文字完成下列需求: \n%s";

    static {
        codeGenHistory = new ArrayList<>();
        codeGenHistory.add(ChatHistory.builder().role("user").content("你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容，花括号为需要替换的内容:\n分析需求:\n{数据分析的需求或者目标}\n原始数据:\n{csv格式的原始数据，用,作为分隔符}\n请根据这两部分内容，按照以下指定格式生成Echarts V5图标代码(此外不要输出任何多余的开头、结尾、注释，代码前后需要有@MySpace包裹起来)\n@MySpace\n{前端Echarts V5的option配置对象js代码，合理地将数据进行可视化，不要生成任何多余的内容，注释，前置词!!!!}\n@MySpace\n").build());
        codeGenHistory.add(ChatHistory.builder().role("assistant").content("好的").build());
        codeGenHistory.add(ChatHistory.builder().role("user").content("比如以下这个例子:\n分析需求:\n分析网站用户的增长情况原始数据:\n指定结果图表类型:\n任意\n原始数据:\n日期,用户数\n1号,10\n2号,20\n3号,30\n\n针对这个例子，你按照以上样式返回的例子可以如下:\n\n" + "@MySpace\noption = {\n" +
                "  xAxis:{\n" +
                "    type: 'category',\n" +
                "    data:['1号','2号','3号']\n" +
                "  },\n" +
                "  yAxis: {\n" +
                "    type: 'value'\n" +
                "  },\n" +
                "  series:[{\n" +
                "    data: [10,20,30],\n" +
                "    type:'line'\n" +
                "  }]\n" +
                "}\n@MySpace\n").build());
        codeGenHistory.add(ChatHistory.builder().role("assistant").content("好的").build());
    }

    @Resource
    private ChartService chartService;

    @Resource
    private UserService userService;

    @Resource
    private ChatGLMService chatGLMService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    /*@PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        chartService.validChart(chart, true);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }*/

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        // 参数校验
        chartService.validChart(chart, false);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<ChartVO> getChartVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        if (!Objects.equals(loginUser.getId(), chart.getUserId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "不可访问他人数据");
        }
        return ResultUtils.success(chartService.getChartVO(chart, request));
    }

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        // 参数校验
        chartService.validChart(chart, false);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestPart("file") MultipartFile multipartFile,
                                       ChartAddRequest chartAddRequest, HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        if (loginUser == null || loginUser.getId() < 0) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "请先登录");
        }
        // 限流
        rateLimiterManager.doRateLimiter("Rlimiter_" + loginUser.getId());

        String goal = chartAddRequest.getGoal();
        String chartType = chartAddRequest.getChartType();
        String title = chartAddRequest.getTitle();
        ThrowUtils.throwIf(StringUtils.isAnyEmpty(goal), ErrorCode.PARAMS_ERROR, "输入为空");
        ThrowUtils.throwIf(goal.length() > 1024, ErrorCode.PARAMS_ERROR, "输入内容过长");

        ThrowUtils.throwIf(!ExcelUtils.isExcelFile(multipartFile), ErrorCode.PARAMS_ERROR, "请传入.xlsx文件");
        ThrowUtils.throwIf(multipartFile.getSize() > 1024 * 1024L, ErrorCode.PARAMS_ERROR, "上传文件不得超过1M");

        String csvRes = ExcelUtils.getCsvFromXlsx(multipartFile);

        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setTitle(title);
        chart.setChartType(chartType);
        chart.setIsFinished(ChartStateEnum.CHART_WAITING.getValue());
        chart.setUserId(loginUser.getId());
        chart.setChartData(csvRes);
        boolean save = chartService.save(chart);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        ChatGLMRequest chatGLMRequest = new ChatGLMRequest();

        String input = String.format(firstQueryTemplate, goal, StringUtils.isEmpty(chartType) ? " " : String.format(chartTypeTemplate, chartType), csvRes);

        chatGLMRequest.setChartId(chart.getId());
        chatGLMRequest.setQuery(input);
        chatGLMRequest.setStream(false);
        chatGLMRequest.setModel_name(modelName);
        chatGLMRequest.setTemperature((float) 0.6);
        chatGLMRequest.setMax_tokens(1024);
        chatGLMRequest.setPrompt_name(promptName);

        List<ChatHistory> chatHistoryList = new ArrayList<>(codeGenHistory);
        // 1. 生成图表代码
        chatGLMRequest.setHistory(chatHistoryList);

        // myMessageProducer.sendMessage("yibi_exchange", "yibi_routingKey", GSON.toJson(chatGLMRequest));
        CompletableFuture.runAsync(() -> {

            String responseFromGLM1 = chatGLMService.getResponseFromGLM(chatGLMRequest);

            ChatGLMResponse chatGLMResponse1 = GSON.fromJson(responseFromGLM1, ChatGLMResponse.class);
            if (chatGLMResponse1.getChat_history_id() == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }

            String[] strings = chatGLMResponse1.getText().split("@MySpace");
            if (strings.length < 1) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成错误");
            }
            String code = strings[strings.length - 1];

            chatHistoryList.add(ChatHistory.builder().role("user").content(input).build());
            chatHistoryList.add(ChatHistory.builder().role("assistant").content("@MySpace\n" + code + "@MySpace\n").build());
            // 2. 生成结论文字
            String secondQuery = String.format(secondQueryTemplate, goal);
            chatGLMRequest.setQuery(secondQuery);
            chatGLMRequest.setHistory(chatHistoryList);

            String responseFromGLM2 = chatGLMService.getResponseFromGLM(chatGLMRequest);

            ChatGLMResponse chatGLMResponse2 = GSON.fromJson(responseFromGLM2, ChatGLMResponse.class);
            if (chatGLMResponse2.getChat_history_id() == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            chatHistoryList.add(ChatHistory.builder().role("user").content(secondQuery).build());
            chatHistoryList.add(ChatHistory.builder().role("assistant").content(chatGLMResponse2.getText()).build());

            chart.setGenText(chatGLMResponse2.getText());
            chart.setGenCode(code);
            chart.setChatHistoryList(GSON.toJson(chatHistoryList));
            chart.setUserId(loginUser.getId());
            chart.setIsFinished(ChartStateEnum.CHART_FINISHED.getValue());

            chartService.updateById(chart);
        }, threadPoolExecutor);
        return ResultUtils.success(chart.getId());
    }
}
