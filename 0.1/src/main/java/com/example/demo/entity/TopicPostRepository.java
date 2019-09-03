package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TopicPostRepository extends MongoRepository<TopicPost,String> {
    List<TopicPost> findByTid(String tid);
}
