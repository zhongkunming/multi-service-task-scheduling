package cn.ming.consumer.controller;

import cn.ming.consumer.service.FeignDispatchService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("dispatch")
public class DispatchController {

    @Resource
    private FeignDispatchService feignDispatchService;

    @PostMapping("run1/{taskId}")
    public Map<String, String> test(@PathVariable("taskId") Long taskId) {
        log.info("{}", taskId);
        return feignDispatchService.test(taskId);
    }
}
