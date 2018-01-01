package com.jj.game.boost.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by huzd on 2017/6/19.
 */
@Entity
public class DelayLostSave {
    @Id(autoincrement = true)
    @Property(nameInDb = "_id")
    private Long id;
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minitue;
    private int second;
    private String delay;
    private String lost;
    @Generated(hash = 1111229210)
    public DelayLostSave(Long id, int year, int month, int day, int hour,
            int minitue, int second, String delay, String lost) {
        this.id = id;
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minitue = minitue;
        this.second = second;
        this.delay = delay;
        this.lost = lost;
    }
    @Generated(hash = 1281015434)
    public DelayLostSave() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public int getYear() {
        return this.year;
    }
    public void setYear(int year) {
        this.year = year;
    }
    public int getMonth() {
        return this.month;
    }
    public void setMonth(int month) {
        this.month = month;
    }
    public int getDay() {
        return this.day;
    }
    public void setDay(int day) {
        this.day = day;
    }
    public int getHour() {
        return this.hour;
    }
    public void setHour(int hour) {
        this.hour = hour;
    }
    public int getMinitue() {
        return this.minitue;
    }
    public void setMinitue(int minitue) {
        this.minitue = minitue;
    }
    public int getSecond() {
        return this.second;
    }
    public void setSecond(int second) {
        this.second = second;
    }
    public String getDelay() {
        return this.delay;
    }
    public void setDelay(String delay) {
        this.delay = delay;
    }
    public String getLost() {
        return this.lost;
    }
    public void setLost(String lost) {
        this.lost = lost;
    }
}
