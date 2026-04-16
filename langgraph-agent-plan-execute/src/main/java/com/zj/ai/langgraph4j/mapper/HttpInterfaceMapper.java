package com.zj.ai.langgraph4j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zj.ai.langgraph4j.domain.entity.HttpInterfaceEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * HTTP 接口定义 Mapper
 *
 * @author zj
 * @date 2026/04/15
 */
@Mapper
public interface HttpInterfaceMapper extends BaseMapper<HttpInterfaceEntity> {

}
