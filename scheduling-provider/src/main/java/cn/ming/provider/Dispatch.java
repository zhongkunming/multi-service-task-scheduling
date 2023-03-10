package cn.ming.provider;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.pattern.parser.PatternParser;
import cn.hutool.cron.task.Task;
import cn.hutool.extra.spring.EnableSpringUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.ming.provider.model.SysTask;
import cn.ming.provider.model.SysTaskConfig;
import cn.ming.provider.repository.SysTaskConfigRepository;
import cn.ming.provider.repository.SysTaskRepository;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@EnableSpringUtil
public class Dispatch implements ApplicationListener<ApplicationEvent> {

    public static final Map<Long, String> TASK_SCHEDULE_MAPPING = new ConcurrentHashMap<>();
    public static final String SYS_TASK_COUNTER = "SYS:TASK:COUNTER";
    public static final long SYS_TASK_TASK_COUNTER_STEP_INTERVAL = 1;

    @Resource
    private RedissonClient redissonClient;
    @Resource
    private SysTaskRepository sysTaskRepository;

    @Resource
    private SysTaskConfigRepository sysTaskConfigRepository;

    @Override
    public void onApplicationEvent(@NotNull ApplicationEvent event) {
        if (event instanceof ApplicationReadyEvent) {
            start();
        } else if (event instanceof ContextClosedEvent) {
            stop();
        }

    }

    private void start() {
        initializeAllTasks();
        enableAutoRefreshDispatch();
        startDispatch();
    }

    private void stop() {
        decrTaskCounter();
        CronUtil.stop();
    }

    private void initializeAllTasks() {
        List<SysTask> tasks = sysTaskRepository.findAll();
        if (CollectionUtil.isEmpty(tasks)) {
            return;
        }
        CronUtil.setMatchSecond(true);
        for (SysTask task : tasks) {
            checkCronExpression(task.getCron());
            loadTaskToDispatch(task);
        }
    }

    private void enableAutoRefreshDispatch() {
        CronUtil.schedule("0 55 23 * * *", (Task) () -> {
            new Thread(() -> {
                log.info("???????????? Dispatch");
                CronUtil.stop();
                start();
                log.info("?????? Dispatch ??????");
            }).start();
        });
    }

    private void startDispatch() {
        CronUtil.start(false);
        initTaskCounter();
        incrTaskCounter();
    }

    private void loadTaskToDispatch(SysTask task) {
        String scheduleId = CronUtil.schedule(task.getCron(), (Task) () -> {
            String executeCommand = task.getExec();
            String beanName;
            int i = StrUtil.indexOf(executeCommand, '(');
            if (i > 0) {
                beanName = StrUtil.sub(executeCommand, 0, i);
            } else {
                beanName = executeCommand;
            }
            AbstractTask actuallyTask = SpringUtil.getBean(beanName);
            actuallyTask.run(task.getId());
        });
        TASK_SCHEDULE_MAPPING.put(task.getId(), scheduleId);
        log.info("?????? JVM taskId: {}, taskName: {}, scheduleId: {}", task.getId(), task.getName(), scheduleId);
    }


    private void checkCronExpression(@NotBlank String cron) {
        try {
            PatternParser.parse(cron);
        } catch (Exception e) {
            log.info("Cron ????????????????????????: {}, {}", cron, e.getMessage());
            throw new RuntimeException("Cron ????????????????????????: " + cron + ", " + e.getMessage());
        }
    }
    private void initTaskCounter() {
        Optional<SysTaskConfig> optional = sysTaskConfigRepository.findById(SYS_TASK_COUNTER);
        if (optional.isEmpty()) {
            SysTaskConfig sysTaskConfig = new SysTaskConfig();
            sysTaskConfig.setKey(SYS_TASK_COUNTER);
            sysTaskConfig.setValue("0");
            sysTaskConfigRepository.saveAndFlush(sysTaskConfig);
        }
    }
    private void incrTaskCounter() {
        sysTaskConfigRepository.incrTaskCounter();
        Optional<SysTaskConfig> optional = sysTaskConfigRepository.findById(SYS_TASK_COUNTER);
        if (optional.isPresent()) {
            SysTaskConfig sysTaskCounter = optional.get();
            redissonClient.getAtomicLong(SYS_TASK_COUNTER).set(Long.parseLong(sysTaskCounter.getValue()));
        }
    }

    private void decrTaskCounter() {
        sysTaskConfigRepository.decrTaskCounter();
        Optional<SysTaskConfig> optional = sysTaskConfigRepository.findById(SYS_TASK_COUNTER);
        if (optional.isPresent()) {
            SysTaskConfig sysTaskCounter = optional.get();
            redissonClient.getAtomicLong(SYS_TASK_COUNTER).set(Long.parseLong(sysTaskCounter.getValue()));
        }
    }
}
