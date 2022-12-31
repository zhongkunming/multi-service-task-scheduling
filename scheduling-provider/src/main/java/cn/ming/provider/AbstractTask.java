package cn.ming.provider;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.system.SystemUtil;
import cn.ming.provider.model.SysTask;
import cn.ming.provider.model.SysTaskLog;
import cn.ming.provider.repository.SysTaskLogRepository;
import cn.ming.provider.repository.SysTaskRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RList;
import org.redisson.api.RPermitExpirableSemaphore;
import org.redisson.api.RedissonClient;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static cn.ming.provider.Dispatch.SYS_TASK_COUNTER;

@Slf4j
public abstract class AbstractTask {

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private SysTaskRepository sysTaskRepository;

    @Resource
    private SysTaskLogRepository sysTaskLogRepository;

    private Long taskId;

    private SysTask sysTask;

    public final void run(Long taskId) {
        this.taskId = taskId;

        if (!prepare()) {
            return;
        }

        String executeCommand = sysTask.getExec();

        String[] args = null;
        int i = StrUtil.indexOf(executeCommand, '(');
        if (i > 0) {
            String params = StrUtil.sub(executeCommand, i + 1, executeCommand.length() - 1);
            List<String> split = StrUtil.split(params, ",");
            args = new String[split.size()];
            args = split.toArray(args);
        }

        for (int count = 0; count <= sysTask.getRetry(); count++) {
            if (sysTask.getConcurrent()) {
                schedule1(args);
            } else {
                RList<Object> waitList = redissonClient.getList(lockName() + ":WAIT_LIST");
                RCountDownLatch latch = redissonClient.getCountDownLatch(lockName() + ":COUNT_DOWN_LATCH");
                RPermitExpirableSemaphore semaphore = redissonClient.getPermitExpirableSemaphore(lockName() + ":RUN_LOCK");
                RAtomicLong taskRunCounter = redissonClient.getAtomicLong(lockName() + ":RUN_COUNTER");
                RAtomicLong mCounter = redissonClient.getAtomicLong(SYS_TASK_COUNTER);

                semaphore.trySetPermits(1);
                long point = mCounter.get();
                if (waitList.size() == 0 && latch.trySetCount(point)) {
                    log.info("为 {}，设置latch", sysTask.getName());
                }
                if (waitList.size() < point) {
                    waitList.add(NetUtil.getLocalMacAddress());
                    latch.countDown();
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    latch.countDown();
                }
                waitList.clear();
                long runCounter = taskRunCounter.get();
                log.info("争抢 {} 执行锁", sysTask.getName());
                String id = semaphore.tryAcquire();
                try {
                    if (StrUtil.isBlank(id)) {
                        return;
                    }
                    long newCounter = runCounter + Dispatch.SYS_TASK_TASK_COUNTER_STEP_INTERVAL;
                    boolean b = taskRunCounter.compareAndSet(runCounter, newCounter);
                    if (b && taskRunCounter.get() == newCounter) {
                        log.info("获取到 {} 执行权", sysTask.getName());
                        schedule1(args);
                    }
                } finally {
                    if (StrUtil.isNotBlank(id)) {
                        semaphore.release(id);
                    }
                }
            }
        }
    }

    public final void schedule(Long taskId, String... args) {
        try {
            this.taskId = taskId;
            this.prepare();
            this.runTask(args);
            saveLog(null);
        } catch (Exception e) {
            saveLog(e);
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private void schedule1(String... args) {
        try {
            this.runTask(args);
            saveLog(null);
        } catch (Exception e) {
            saveLog(e);
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException ignored) {
            }
        }
    }

    private boolean prepare() {
        Optional<SysTask> optionalSysTask = sysTaskRepository.findById(taskId);
        if (optionalSysTask.isEmpty()) {
            return false;
        }
        this.sysTask = optionalSysTask.get();
        return sysTask.getEnable();
    }

    private void saveLog(Exception e) {
        SysTaskLog sysTaskLog = new SysTaskLog();
        sysTaskLog.setServerName(SystemUtil.getHostInfo().getName());
        sysTaskLog.setTaskId(taskId);
        sysTaskLog.setTaskName(sysTask.getName());
        sysTaskLog.setScheduleId(Dispatch.TASK_SCHEDULE_MAPPING.get(taskId));
        sysTaskLog.setScheduleTime(new Date());
        if (e == null) {
            sysTaskLog.setSuccess(true);
        } else {
            sysTaskLog.setSuccess(false);
            sysTaskLog.setExceptionMessage(e.getMessage());
            sysTaskLog.setExceptionStack(JSONUtil.toJsonStr(Arrays.copyOf(e.getStackTrace(), 5)));
        }
        sysTaskLogRepository.save(sysTaskLog);
    }

    private String lockName() {
        return MessageFormat.format("SYS:TASK:LOCK:{0}", taskId);
    }

    public abstract void runTask(String... args) throws Exception;
}
