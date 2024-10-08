package cat.redis.cadis.server.service.models;

import java.util.Date;

public class ScheduledItem {
    //定时任务
    Runnable function;
    //提交时间
    Date submitTime;
    //运行次数
    int count;
    //提交时间
    Date lastInvoke;
    //定时周期
    int period;

    public Runnable getFunction() {
        return function;
    }

    public void setFunction(Runnable function) {
        this.function = function;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Date getLastInvoke() {
        return lastInvoke;
    }

    public void setLastInvoke(Date lastInvoke) {
        this.lastInvoke = lastInvoke;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }
}
