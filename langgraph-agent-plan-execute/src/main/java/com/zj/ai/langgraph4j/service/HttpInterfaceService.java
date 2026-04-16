package com.zj.ai.langgraph4j.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zj.ai.langgraph4j.domain.entity.HttpInterfaceEntity;

import java.util.List;
import java.util.Optional;

/**
 * HTTP 接口定义服务接口
 *
 * @author zj
 * @date 2026/04/15
 */
public interface HttpInterfaceService extends IService<HttpInterfaceEntity> {

    /**
     * 根据接口编码查询
     *
     * @param interfaceCode 接口编码
     * @return 接口定义
     */
    Optional<HttpInterfaceEntity> findByInterfaceCode(String interfaceCode);

    /**
     * 查询指定分类下的所有接口
     *
     * @param category 分类
     * @return 接口列表
     */
    List<HttpInterfaceEntity> findByCategory(String category);

    /**
     * 查询所有启用的接口
     *
     * @return 接口列表
     */
    List<HttpInterfaceEntity> findAllEnabled();

    /**
     * 根据接口编码查询启用的接口
     *
     * @param interfaceCode 接口编码
     * @return 接口定义
     */
    Optional<HttpInterfaceEntity> findEnabledByCode(String interfaceCode);

    /**
     * 根据分类查询启用的接口
     *
     * @param category 分类
     * @return 接口列表
     */
    List<HttpInterfaceEntity> findEnabledByCategory(String category);

    /**
     * 根据请求方法查询接口
     *
     * @param method HTTP 方法
     * @return 接口列表
     */
    List<HttpInterfaceEntity> findByMethod(String method);

    /**
     * 启用/禁用接口
     *
     * @param id      接口ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    boolean updateEnabled(Long id, Boolean enabled);

    /**
     * 检查接口编码是否存在
     *
     * @param interfaceCode 接口编码
     * @return 是否存在
     */
    boolean existsByInterfaceCode(String interfaceCode);
}
