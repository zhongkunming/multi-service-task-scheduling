package cn.ming.provider.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.ming.provider.AbstractTask;
import cn.ming.provider.model.SysTask;
import cn.ming.provider.repository.SysTaskRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("dispatch")
public class DispatchController {

    @Resource
    private SysTaskRepository sysTaskRepository;

    @PostMapping("run1/{taskId}")
    public Map<String, String> test(@PathVariable Long taskId) {
        Map<String, String> map = new HashMap<>(1);
        map.put("code", "500");

        Optional<SysTask> optionalSysTask = sysTaskRepository.findById(taskId);
        if (optionalSysTask.isEmpty()) {
            return map;
        }
        SysTask sysTask = optionalSysTask.get();
        String executeCommand = sysTask.getExec();
        int i = StrUtil.indexOf(executeCommand, '(');
        String beanName;
        String[] args = null;
        if (i > 0) {
            beanName = StrUtil.sub(executeCommand, 0, i);

            String params = StrUtil.sub(executeCommand, i + 1, executeCommand.length() - 1);
            List<String> split = StrUtil.split(params, ",");
            args = new String[split.size()];
            args = split.toArray(args);
        } else {
            beanName = executeCommand;
        }

        AbstractTask actuallyTask = SpringUtil.getBean(beanName);
        actuallyTask.schedule(taskId, args);

        map.put("code", "200");
        return map;
    }
}
