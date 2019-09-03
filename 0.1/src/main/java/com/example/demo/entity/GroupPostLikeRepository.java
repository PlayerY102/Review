package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GroupPostLikeRepository extends MongoRepository<GroupPostLike,String> {
    List<GroupPostLike> findByPidAndUid(String rid, String uid);
    List<GroupPostLike> findByUid(String uid);
}
