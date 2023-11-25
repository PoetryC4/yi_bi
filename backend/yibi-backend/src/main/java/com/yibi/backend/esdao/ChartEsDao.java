package com.yibi.backend.esdao;

import com.yibi.backend.model.dto.chart.ChartEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 帖子 ES 操作
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface ChartEsDao extends ElasticsearchRepository<ChartEsDTO, Long> {

    List<ChartEsDTO> findByUserId(Long userId);
}