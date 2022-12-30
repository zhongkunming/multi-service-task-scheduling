package cn.ming.provider.repository;

import cn.ming.provider.model.SysTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysTaskRepository extends JpaRepository<SysTask, Long> {
}