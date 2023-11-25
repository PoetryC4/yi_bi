package com.yibi.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yibi.backend.model.entity.Chart;

import java.util.Date;
import java.util.List;

/**
 * 帖子数据库操作
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
public interface ChartMapper extends BaseMapper<Chart> {

    List<Chart> listChartWithDelete(Date minUpdateTime);

}




