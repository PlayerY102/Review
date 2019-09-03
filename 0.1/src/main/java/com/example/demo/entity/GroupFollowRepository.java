package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GroupFollowRepository extends MongoRepository<GroupFollow,String> {
    List<GroupFollow> findByGidAndAndUid(String gid,String uid);
    List<GroupFollow> findByUid(String uid);
}
