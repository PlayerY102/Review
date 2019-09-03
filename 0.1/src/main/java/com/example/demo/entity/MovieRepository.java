package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MovieRepository extends MongoRepository<Movie,String> {
    List<Movie> findByRateBetween(int from,int to);
    List<Movie> findByNameLike(String name);
    List<Movie> findAllByOrderByTimeDesc();
    List<Movie> findAllByOrderByTimeAsc();
}
