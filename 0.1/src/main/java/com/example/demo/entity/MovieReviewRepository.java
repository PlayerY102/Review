package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MovieReviewRepository extends MongoRepository<MovieReview,String> {
    List<MovieReview> findByMid(String mid);
    List<MovieReview> findByUid(String uid);
}
