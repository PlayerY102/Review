package com.example.demo.entity;

import org.springframework.data.annotation.Id;

import java.util.Date;

public class TopicPost {
    @Id
    private String id;

    private String uid;
    private String tid;
    private String title;
    private String content;
    private Integer like;
    private Date time;

    public TopicPost() {
        this.title="";
        this.content="";
        this.like=0;
        this.time=new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getLike() {
        return like;
    }

    public void setLike(Integer like) {
        this.like = like;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
