package com.example.demo.entity;

import org.springframework.data.annotation.Id;

import java.util.Date;

public class Topic {
    @Id
    private String id;

    private String name;
    private Integer rateNumber;
    private String image;
    private Date time;
    private String overview;

    public Topic() {
        this.name="";
        this.rateNumber=0;
        this.image="";
        this.time=new Date();
        this.overview="";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getRateNumber() {
        return rateNumber;
    }

    public void setRateNumber(Integer rateNumber) {
        this.rateNumber = rateNumber;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }
}
