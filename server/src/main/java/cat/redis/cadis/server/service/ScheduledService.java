package cat.redis.cadis.server.service;


import cat.redis.cadis.server.config.ServerConfig;
import cat.redis.cadis.server.service.models.ScheduledItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class ScheduledService {
    List<ScheduledItem> tasks;
    ScheduledExecutorService scheduler;

    public ScheduledService(ServerConfig serverConfig){
        tasks = new ArrayList<>();
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::loop,serverConfig.getInitialDelay(),
                serverConfig.getPeriod(), serverConfig.getTimeUnit());
    }

    public int submitTask(Runnable function,int period){
        ScheduledItem item = new ScheduledItem();
        item.setFunction(function);
        item.setCount(0);
        item.setSubmitTime(new Date());
        item.setPeriod(period);
        item.setLastInvoke(null);
        tasks.add(item);
        return tasks.size()-1;//返回这个任务得id
    }

    public int delayTask(int taskId,Long delayTime){
        if(taskId > 0 && taskId < tasks.size()){
            ScheduledItem item = tasks.get(taskId);
            long newSubmitTime = item.getSubmitTime().getTime() + delayTime;
            item.setSubmitTime(new Date(newSubmitTime));
            return 1;
        }
        return 0;
    }

    public int cancelTask(int taskId){
        if(taskId > 0 && taskId < tasks.size()){
            tasks.remove(taskId);
            return 1;
        }
        return 0;
    }

    //遍历每一个定时任务，时间到了就执行，并且记录数据
    public void loop(){
        tasks.forEach(e->{
            Date now = new Date();
            long duration = now.getTime() - e.getSubmitTime().getTime();
            if(duration % e.getPeriod() == 0){
                e.getFunction().run();
                e.setCount(e.getCount()+1);
                e.setLastInvoke(new Date());
            }
        });
    }
}
