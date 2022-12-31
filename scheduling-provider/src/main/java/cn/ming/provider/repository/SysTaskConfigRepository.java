package cn.ming.provider.repository;

import cn.ming.provider.model.SysTaskConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SysTaskConfigRepository extends JpaRepository<SysTaskConfig, String> {

    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update sys_task_config set value = cast(value as bigint) + 1 where key = 'SYS:TASK:COUNTER'", nativeQuery = true)
    void incrTaskCounter();

    @Transactional(rollbackFor = Exception.class)
    @Modifying
    @Query(value = "update sys_task_config set value = cast(value as bigint) - 1 where key = 'SYS:TASK:COUNTER'", nativeQuery = true)
    void decrTaskCounter();
}