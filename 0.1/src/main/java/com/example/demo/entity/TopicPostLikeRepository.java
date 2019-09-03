package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TopicPostLikeRepository extends MongoRepository<TopicPostLike,String> {
    List<TopicPostLike> findByPidAndUid(String rid, String uid);
    List<TopicPostLike> findByUid(String uid);
}
