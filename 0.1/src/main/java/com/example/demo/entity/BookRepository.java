package com.example.demo.entity;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BookRepository extends MongoRepository<Book,String> {
    List<Book> findByRateBetween(int from,int to);
    List<Book> findByNameLike(String name);
    List<Book> findAllByOrderByTimeDesc();
    List<Book> findAllByOrderByTimeAsc();
}
