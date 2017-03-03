package fetalmovement.android.ttonway.com.fetalmovement.model;

import io.realm.RealmObject;

/**
 * Created by ttonway on 2017/2/28.
 */

public class OneHourStatistic extends RealmObject {

    long beginTime;
    long endTime;
    int movements;
    long lastTime;

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getMovements() {
        return movements;
    }

    public void setMovements(int movements) {
        this.movements = movements;
    }

    public long getLastTime() {
        return lastTime;
    }

    public void setLastTime(long lastTime) {
        this.lastTime = lastTime;
    }
}
