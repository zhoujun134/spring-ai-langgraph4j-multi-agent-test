package com.zj.ai.langgraph4j.repository;

import com.zj.ai.langgraph4j.domain.entity.ModelConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 模型配置 Repository
 *
 * @author zj
 * @date 2026/04/12
 */
@Repository
public interface ModelConfigRepository extends JpaRepository<ModelConfigEntity, Long> {

    /**
     * 根据模型名称查找
     */
    Optional<ModelConfigEntity> findByModelName(String modelName);

    /**
     * 查找默认模型
     */
    Optional<ModelConfigEntity> findByIsDefaultTrue();

    /**
     * 查找所有启用的模型
     */
    List<ModelConfigEntity> findByEnabledTrue();

    /**
     * 根据提供商查找
     */
    List<ModelConfigEntity> findByProvider(String provider);
}
