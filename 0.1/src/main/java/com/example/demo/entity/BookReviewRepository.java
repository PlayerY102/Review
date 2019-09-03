package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookReviewRepository extends MongoRepository<BookReview,String> {
    List<BookReview> findByBid(String bid);
    List<BookReview> findByUid(String uid);
}
