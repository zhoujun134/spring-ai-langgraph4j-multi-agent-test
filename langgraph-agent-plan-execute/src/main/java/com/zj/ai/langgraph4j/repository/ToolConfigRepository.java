package com.zj.ai.langgraph4j.repository;

import com.zj.ai.langgraph4j.domain.entity.ToolConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 工具配置 Repository
 *
 * @author zj
 * @date 2026/04/12
 */
@Repository
public interface ToolConfigRepository extends JpaRepository<ToolConfigEntity, Long> {

    /**
     * 根据工具名称查找
     */
    Optional<ToolConfigEntity> findByToolName(String toolName);

    /**
     * 查找启用的工具，按优先级排序
     */
    List<ToolConfigEntity> findByEnabledTrueOrderByPriorityAsc();

    /**
     * 根据工具名称查找启用的工具
     */
    Optional<ToolConfigEntity> findByToolNameAndEnabledTrue(String toolName);

    /**
     * 查找所有启用的工具
     */
    List<ToolConfigEntity> findByEnabledTrue();
}
