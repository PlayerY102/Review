package com.example.demo.entity;

import org.springframework.data.annotation.Id;

import java.util.Date;

public class TopicPostLike {
    @Id
    private String id;

    private String pid;
    private String uid;
    private Date time;

    public TopicPostLike() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
