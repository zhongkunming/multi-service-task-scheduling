package cn.ming.provider.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "sys_task_log")
public class SysTaskLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false)
    private String id;

    @Column(name = "server_name")
    private String serverName;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "task_name")
    private String taskName;

    @Column(name = "schedule_id")
    private String scheduleId;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "schedule_time")
    private Date scheduleTime;

    @Column(name = "exception_message")
    private String exceptionMessage;

    @Column(name = "exception_stack")
    private String exceptionStack;

    @Serial
    private static final long serialVersionUID = 1L;

}