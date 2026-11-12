package com.sky.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;

@Component
@Slf4j
public class MyTask {
    //每5秒执行一次：秒=0/5，分、时、日、月、周都任意
    @Scheduled(cron = "0/5 * * * * ?")
    public void scheduled() {
        log.info("定时任务开始执行：{}", new Date());
    }

}
