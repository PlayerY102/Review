package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GroupPostRepository extends MongoRepository<GroupPost,String> {
    List<GroupPost> findByGid(String gid);
}
