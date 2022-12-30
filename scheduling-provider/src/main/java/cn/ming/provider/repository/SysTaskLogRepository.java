package cn.ming.provider.repository;

import cn.ming.provider.model.SysTaskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SysTaskLogRepository extends JpaRepository<SysTaskLog,String> {
}