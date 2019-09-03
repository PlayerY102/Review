package com.example.demo.entity;

import org.springframework.data.annotation.Id;

import java.util.Date;

public class Movie {
    @Id
    private String id;

    private String name;
    private Integer rate;
    private Integer rateNumber;
    private String image;
    private Date time;
    private String overview;

    // add by xqy
    private String director;
    private String actor;
    private String movietype;

    public Movie() {
        name="";
        rate=60;
        rateNumber=0;
        image="";
        time=new Date();
        overview="";

        // add by xqy
        director="";
        actor= "";
        movietype="";

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

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
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

    // add by xqy
    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getActor() {
        return actor;
    }

    public void setActor(String actor) {
        this.actor = actor;
    }

    public String getMovietype() {
        return movietype;
    }

    public void setMovietype(String movietype) {
        this.movietype = movietype;
    }
}
