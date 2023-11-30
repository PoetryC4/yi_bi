package com.yibi.backend.job.cycle;

import com.yibi.backend.model.entity.Chart;
import com.yibi.backend.model.enums.ChartStateEnum;
import com.yibi.backend.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;


@Component
@Slf4j
public class FindChartUnfinished {

    @Resource
    private ChartService chartService;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 2 * 60 * 1000)
    public void run() {
        List<Chart> chartList = chartService.getChartsFailedAfter(5 * 60 * 1000L);
        if (CollectionUtils.isEmpty(chartList)) {
            log.info("无分析失败样例");
        } else {
            for (Chart chart : chartList) {
                chart.setIsFinished(ChartStateEnum.CHART_FAILED.getValue());
            }
            chartService.updateBatchById(chartList);
            log.info("分析失败样例: {}", chartList.size());
        }
    }
}
