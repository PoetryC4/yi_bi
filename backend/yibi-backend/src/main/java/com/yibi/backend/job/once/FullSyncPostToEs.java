package com.yibi.backend.job.once;

import com.yibi.backend.esdao.ChartEsDao;
import com.yibi.backend.esdao.PostEsDao;
import com.yibi.backend.mapper.ChartMapper;
import com.yibi.backend.model.dto.chart.ChartEsDTO;
import com.yibi.backend.model.dto.post.PostEsDTO;
import com.yibi.backend.model.entity.Chart;
import com.yibi.backend.model.entity.Post;
import com.yibi.backend.service.ChartService;
import com.yibi.backend.service.PostService;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.CommandLineRunner;

/**
 * 全量同步帖子到 es
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
// todo 取消注释开启任务
//@Component
@Slf4j
public class FullSyncPostToEs implements CommandLineRunner {

    @Resource
    private PostService postService;

    @Resource
    private PostEsDao postEsDao;
    @Resource
    private ChartService chartService;

    @Resource
    private ChartEsDao chartEsDao;

    @Override
    public void run(String... args) {
        final int pageSize = 500;
        List<Post> postList = postService.list();
        if (!CollectionUtils.isEmpty(postList)) {
            List<PostEsDTO> postEsDTOList = postList.stream().map(PostEsDTO::objToDto).collect(Collectors.toList());
            int total = postEsDTOList.size();
            log.info("FullSyncPostToEs start, total {}", total);
            for (int i = 0; i < total; i += pageSize) {
                int end = Math.min(i + pageSize, total);
                log.info("sync from {} to {}", i, end);
                postEsDao.saveAll(postEsDTOList.subList(i, end));
            }
            log.info("FullSyncPostToEs end, total {}", total);
        }
        List<Chart> userList = chartService.list();
        if (!CollectionUtils.isEmpty(userList)) {
            List<ChartEsDTO> chartEsDTOS = userList.stream().map(ChartEsDTO::objToDto).collect(Collectors.toList());
            int total = chartEsDTOS.size();
            log.info("FullSyncUserToEs start, total {}", total);
            for (int i = 0; i < total; i += pageSize) {
                int end = Math.min(i + pageSize, total);
                log.info("sync from {} to {}", i, end);
                chartEsDao.saveAll(chartEsDTOS.subList(i, end));
            }
            log.info("FullSyncUserToEs end, total {}", total);
        }
    }
}
