package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UploadFileRepository extends MongoRepository<UploadFile,String> {

}
