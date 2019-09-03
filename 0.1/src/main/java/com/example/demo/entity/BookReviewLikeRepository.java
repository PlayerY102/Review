package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookReviewLikeRepository extends MongoRepository<BookReviewLike,String> {
    List<BookReviewLike> findByRidAndUid(String rid, String uid);
    List<BookReviewLike> findByUid(String uid);
}
