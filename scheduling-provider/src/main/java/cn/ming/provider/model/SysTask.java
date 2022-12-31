package cn.ming.provider.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "sys_task")
public class SysTask implements Serializable {
    /**
     * 任务ID
     */
    @Id
    @GeneratedValue(generator = "sys_task_pk")
    @GenericGenerator(name = "sys_task_pk", strategy = "assigned")
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    /**
     * 任务名称
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 任务cron表达式
     */
    @Column(name = "cron", nullable = false)
    private String cron;

    /**
     * 任务执行bean以及参数
     */
    @Column(name = "exec", nullable = false)
    private String exec;

    /**
     * 任务是否启动
     */
    @Column(name = "enable", nullable = false)
    private Boolean enable = true;

    /**
     * 任务重试次数
     */
    @Column(name = "retry", nullable = false)
    private Integer retry = 0;

    /**
     * 是否支持并发
     */
    @Column(name = "concurrent", nullable = false)
    private Boolean concurrent = false;

    @Serial
    private static final long serialVersionUID = 1L;
}