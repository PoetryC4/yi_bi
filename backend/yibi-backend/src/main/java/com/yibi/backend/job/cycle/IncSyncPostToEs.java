package com.yibi.backend.job.cycle;

import com.yibi.backend.esdao.ChartEsDao;
import com.yibi.backend.esdao.PostEsDao;
import com.yibi.backend.mapper.ChartMapper;
import com.yibi.backend.mapper.PostMapper;
import com.yibi.backend.model.dto.chart.ChartEsDTO;
import com.yibi.backend.model.dto.post.PostEsDTO;
import com.yibi.backend.model.entity.Chart;
import com.yibi.backend.model.entity.Post;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 增量同步帖子到 es
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class IncSyncPostToEs {

    @Resource
    private PostMapper postMapper;

    @Resource
    private PostEsDao postEsDao;
    @Resource
    private ChartMapper chartMapper;

    @Resource
    private ChartEsDao chartEsDao;

    /**
     * 每分钟执行一次
     */
    @Scheduled(fixedRate = 60 * 1000)
    public void run() {
        // 查询近 5 分钟内的数据
        Date fiveMinutesAgoDate = new Date(new Date().getTime() - 5 * 60 * 1000L);
        final int pageSize = 500;
        List<Post> postList = postMapper.listPostWithDelete(fiveMinutesAgoDate);
        if (CollectionUtils.isEmpty(postList)) {
            log.info("no inc post");
        } else {
            List<PostEsDTO> postEsDTOList = postList.stream()
                    .map(PostEsDTO::objToDto)
                    .collect(Collectors.toList());
            int total1 = postEsDTOList.size();
            log.info("IncSyncPostToEs start, total {}", total1);
            for (int i = 0; i < total1; i += pageSize) {
                int end = Math.min(i + pageSize, total1);
                log.info("sync from {} to {}", i, end);
                postEsDao.saveAll(postEsDTOList.subList(i, end));
            }
            log.info("IncSyncPostToEs end, total {}", total1);
        }
        List<Chart> userList = chartMapper.listChartWithDelete(fiveMinutesAgoDate);
        if (CollectionUtils.isEmpty(userList)) {
            log.info("no inc user");
        } else {
            List<ChartEsDTO> chartEsDTOS = userList.stream()
                    .map(ChartEsDTO::objToDto)
                    .collect(Collectors.toList());
            int total2 = chartEsDTOS.size();
            log.info("IncSyncUserToEs start, total {}", total2);
            for (int i = 0; i < total2; i += pageSize) {
                int end = Math.min(i + pageSize, total2);
                log.info("sync from {} to {}", i, end);
                chartEsDao.saveAll(chartEsDTOS.subList(i, end));
            }
            log.info("IncSyncPostToEs end, total {}", total2);
        }
    }
}
