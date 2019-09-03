package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MovieReviewLikeRepository extends MongoRepository<MovieReviewLike,String> {
    List<MovieReviewLike> findByRidAndUid(String rid,String uid);
    List<MovieReviewLike> findByUid(String uid);
}
