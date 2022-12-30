package cn.ming.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Scope("prototype")
@Component("myTask")
public class MyTask extends AbstractTask {
    public void runTask(String... args) throws Exception {
        log.info("myTask任务执行{}", args);
        TimeUnit.SECONDS.sleep(1);
    }
}
