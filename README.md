# multi-service-task-scheduling

简单实现的一个支持多节点定时任务执行框架设计。
支持不同机器节点时间偏差在可控范围内的定时任务执行。
能够保证一个任务只在一个节点执行。
利用redis和数据库实现分布式信号量控制和节点间争夺执行权。
支持任务失败后重试，会自动尝试寻找新执行节点