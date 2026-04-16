package com.zj.ai.langgraph4j.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zj.ai.langgraph4j.domain.entity.HttpInterfaceEntity;
import com.zj.ai.langgraph4j.mapper.HttpInterfaceMapper;
import com.zj.ai.langgraph4j.service.HttpInterfaceService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * HTTP 接口定义服务实现
 *
 * @author zj
 * @date 2026/04/15
 */
@Service
public class HttpInterfaceServiceImpl extends ServiceImpl<HttpInterfaceMapper, HttpInterfaceEntity>
        implements HttpInterfaceService {

    @Override
    public Optional<HttpInterfaceEntity> findByInterfaceCode(String interfaceCode) {
        LambdaQueryWrapper<HttpInterfaceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HttpInterfaceEntity::getInterfaceCode, interfaceCode);
        return Optional.ofNullable(getOne(wrapper));
    }

    @Override
    public List<HttpInterfaceEntity> findByCategory(String category) {
        LambdaQueryWrapper<HttpInterfaceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HttpInterfaceEntity::getCategory, category)
                .orderByAsc(HttpInterfaceEntity::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<HttpInterfaceEntity> findAllEnabled() {
        LambdaQueryWrapper<HttpInterfaceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HttpInterfaceEntity::getEnabled, true)
                .orderByAsc(HttpInterfaceEntity::getSortOrder);
        return list(wrapper);
    }

    @Override
    public Optional<HttpInterfaceEntity> findEnabledByCode(String interfaceCode) {
        LambdaQueryWrapper<HttpInterfaceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HttpInterfaceEntity::getInterfaceCode, interfaceCode)
                .eq(HttpInterfaceEntity::getEnabled, true);
        return Optional.ofNullable(getOne(wrapper));
    }

    @Override
    public List<HttpInterfaceEntity> findEnabledByCategory(String category) {
        LambdaQueryWrapper<HttpInterfaceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HttpInterfaceEntity::getCategory, category)
                .eq(HttpInterfaceEntity::getEnabled, true)
                .orderByAsc(HttpInterfaceEntity::getSortOrder);
        return list(wrapper);
    }

    @Override
    public List<HttpInterfaceEntity> findByMethod(String method) {
        LambdaQueryWrapper<HttpInterfaceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HttpInterfaceEntity::getMethod, method.toUpperCase())
                .orderByAsc(HttpInterfaceEntity::getSortOrder);
        return list(wrapper);
    }

    @Override
    public boolean updateEnabled(Long id, Boolean enabled) {
        LambdaUpdateWrapper<HttpInterfaceEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(HttpInterfaceEntity::getId, id)
                .set(HttpInterfaceEntity::getEnabled, enabled);
        return update(wrapper);
    }

    @Override
    public boolean existsByInterfaceCode(String interfaceCode) {
        LambdaQueryWrapper<HttpInterfaceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HttpInterfaceEntity::getInterfaceCode, interfaceCode);
        return count(wrapper) > 0;
    }
}
