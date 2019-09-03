package com.example.demo.entity;

import org.springframework.data.annotation.Id;

import java.util.Date;

public class MovieReviewLike {
    @Id
    private String id;

    private String rid;
    private String uid;
    private Date time;

    public MovieReviewLike() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
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
