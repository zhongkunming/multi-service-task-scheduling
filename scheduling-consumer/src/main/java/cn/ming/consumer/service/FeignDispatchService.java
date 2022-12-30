package cn.ming.consumer.service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

@FeignClient("provider-app")
public interface FeignDispatchService {
    @PostMapping("/dispatch/run1/{taskId}")
    Map<String, String> test(@PathVariable("taskId") Long taskId);
}
