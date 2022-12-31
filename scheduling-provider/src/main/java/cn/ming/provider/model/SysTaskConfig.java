package cn.ming.provider.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
@Table(name = "sys_task_config")
public class SysTaskConfig implements Serializable {

    @Id
    @GeneratedValue(generator = "sys_task_config_pk")
    @GenericGenerator(name = "sys_task_config_pk", strategy = "assigned")
    @Column(name = "key", unique = true, nullable = false)
    private String key;

    @Column(name = "sort", nullable = false)
    private String sort = "default";

    @Column(name = "value", nullable = false)
    private String value;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;


    @Serial
    private static final long serialVersionUID = 1L;
}
