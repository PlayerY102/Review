package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface GroupApplyRepository extends MongoRepository<GroupApply,String> {
    List<GroupApply> findByGid(String gid);
    List<GroupApply> findByGidAndUid(String gid,String uid);
}
