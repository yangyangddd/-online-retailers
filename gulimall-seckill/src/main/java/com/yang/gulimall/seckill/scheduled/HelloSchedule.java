package com.yang.gulimall.seckill.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
//开启定时任务
@Component
@Slf4j
public class HelloSchedule {
    //1.spring中6位组成，不允许第7位的年
    //2.在周几的位置，1-7 代表周一至周日
    //3.定时任务不应该阻塞。默认是阻塞的
    //  1). 可以让业务运行以异步的方式，自己提交到线程池
    //  2). 支持定时任务线程池
//      3). 开启@EnableAsync和 @Async 开启异步任务
    @Scheduled(cron = "* * * * * ?")
    @Async
    public void hello() throws InterruptedException {
        log.info("hello");
        Thread.sleep(3000);
    }
}
