package com.example.demo.controller;

import com.example.demo.entity.*;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

@Controller
public class TopicController {
    @Autowired
    TopicRepository topicRepository;
    @Autowired
    UploadFileRepository uploadFileRepository;
    @Autowired
    TopicPostRepository topicPostRepository;
    @Autowired
    TopicPostLikeRepository topicPostLikeRepository;
    @Autowired
    UserRepository userRepository;

    @PostMapping("/addTopic")
    public String addTopic(@RequestParam(value = "image") MultipartFile file, @RequestParam String name, @RequestParam String overview, @RequestParam Date time) {
        // add by xqy  @RequestParam String author, @RequestParam String press, @RequestParam String price

        if (file.isEmpty()) {
            return "1";
        }
        Topic topic=new Topic();
        String fileName = file.getOriginalFilename();

        try {
            UploadFile uploadFile = new UploadFile();
            uploadFile.setName(fileName);
            uploadFile.setCreateTime(new Date());
            uploadFile.setContent(new Binary(file.getBytes()));
            uploadFile.setContentType(file.getContentType());
            uploadFile.setSize(file.getSize());

            UploadFile savedFile = uploadFileRepository.save(uploadFile);
            String url = savedFile.getId();
            topic.setImage(url);
        } catch (IOException e) {
            e.printStackTrace();
            return "2";
        }
        topic.setName(name);
        topic.setOverview(overview);
        topic.setTime(time);


        topicRepository.save(topic);
        return "addTopic";
    }

    @GetMapping("/topic/{id}")
    public String topicDetail(@PathVariable String id, HttpServletRequest request, Model model){
        HttpSession session=request.getSession();

        User user=(User)session.getAttribute("currentUser");

        Optional<Topic> topic=topicRepository.findById(id);
        if(!topic.isPresent()){
            return "404";//没有找到movie
        }
        model.addAttribute("currentMovie",topic.get());

        List<TopicPost> topicPostList=topicPostRepository.findByTid(id);
        List<SendReview> sendReviewList=new ArrayList<SendReview>();

        for(TopicPost i:topicPostList){
            SendReview sendReview;

            Optional<User> reviewUser=userRepository.findById(i.getUid());
            List<TopicPostLike> topicPostLikeList;
            if(user!=null){
                topicPostLikeList=topicPostLikeRepository.findByPidAndUid(i.getId(),user.getId());
            }
            else{
                topicPostLikeList=new ArrayList<TopicPostLike>();
            }
            if(topicPostLikeList!=null && topicPostLikeList.size()>0){
                if(reviewUser.isPresent()){
                    sendReview=new SendReview(i,1,reviewUser.get().getName());
                }else{
                    sendReview=new SendReview(i,1,"用户已注销");
                }
            }else{
                if(reviewUser.isPresent()) {
                    sendReview = new SendReview(i, 0, reviewUser.get().getName());
                }else{
                    sendReview=new SendReview(i,0,"用户已注销");
                }
            }
            sendReviewList.add(sendReview);
        }
        List<SendReview> sendReviewList2=new ArrayList<SendReview>(sendReviewList);
        // 时间排序
        Collections.sort(sendReviewList, new Comparator<SendReview>() {
            @Override
            public int compare(SendReview sendReview, SendReview t1) {
                return t1.getTime().compareTo(sendReview.getTime());  // 降序
//                return sendReview.getTime().compareTo(t1.getTime());
            }
        });
        model.addAttribute("movieReviewListOrderByTime",sendReviewList);
        // 评价排序
        Collections.sort(sendReviewList2, new Comparator<SendReview>() {
            @Override
            public int compare(SendReview sendReview, SendReview t1) {
                return t1.getLike().compareTo(sendReview.getLike());
//                return sendReview.getLike().compareTo(t1.getLike());
            }
        });
        model.addAttribute("movieReviewListOrderByLike",sendReviewList2);
        model.addAttribute("currentUser",user);
        return "topicsingle";
    }

    @PostMapping("/addTopicPost")
    @ResponseBody
    public String addTopicPost(@RequestBody UploadReview review,HttpServletRequest request){

        HttpSession session=request.getSession();
        User currentUser=(User)session.getAttribute("currentUser");
        if(currentUser==null){
            return "1";//用户未登录
        }

        if(review.getContent()==null){
            return "3";
        }
        if(review.getTitle()==null){
            return "4";
        }
        TopicPost topicPost=new TopicPost();
        topicPost.setUid(currentUser.getId());
        topicPost.setTid(review.getMid());
        topicPost.setContent(review.getContent());
        topicPost.setTitle(review.getTitle());
        topicPost.setTime(new Date());
        topicPostRepository.save(topicPost);

        List<TopicPost> topicPostList=topicPostRepository.findByTid(review.mid);
        int total=0;
        Optional<Topic> topic=topicRepository.findById(review.mid);
        topic.get().setRateNumber(topicPostList.size());
        topicRepository.save(topic.get());
        return "0";
    }

    @PostMapping("/likeTopic")
    @ResponseBody
    public String likeTopic(@RequestBody Send send,HttpServletRequest request){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        if(user==null){
            return "1";//用户未登录
        }
        String pid=send.getRid();
        List<TopicPostLike> topicPostLikeList=topicPostLikeRepository.findByPidAndUid(pid,user.getId());
        if(topicPostLikeList!=null && topicPostLikeList.size()>0){
            for(TopicPostLike i:topicPostLikeList){
                topicPostLikeRepository.delete(i);
                Optional<TopicPost> topicPost=topicPostRepository.findById(pid);
                if(topicPost.isPresent()){
                    topicPost.get().setLike(topicPost.get().getLike()-1);
                    topicPostRepository.save(topicPost.get());
                }
            }
        }
        else{
            System.out.println("in like");
            TopicPostLike topicPostLike=new TopicPostLike();
            topicPostLike.setPid(pid);
            topicPostLike.setUid(user.getId());
            topicPostLike.setTime(new Date());
            topicPostLikeRepository.save(topicPostLike);
            Optional<TopicPost> topicPost=topicPostRepository.findById(pid);
            if(topicPost.isPresent()){
                System.out.println("in present");
                topicPost.get().setLike(topicPost.get().getLike()+1);
                topicPostRepository.save(topicPost.get());
            }
        }
        return "0";
    }
}
