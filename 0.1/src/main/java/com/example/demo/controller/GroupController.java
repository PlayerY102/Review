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
public class GroupController {
    @Autowired
    GroupRepository groupRepository;
    @Autowired
    UploadFileRepository uploadFileRepository;
    @Autowired
    GroupPostRepository groupPostRepository;
    @Autowired
    GroupPostLikeRepository groupPostLikeRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    GroupApplyRepository groupApplyRepository;
    @Autowired
    GroupFollowRepository groupFollowRepository;

    @PostMapping("/addGroup")
    public String addGroup(@RequestParam(value = "image") MultipartFile file,@RequestParam String grouptype, @RequestParam String name, @RequestParam String overview,HttpServletRequest request,Model model) {
        if (file.isEmpty()) {
            return "1";
        }

        HttpSession session=request.getSession();
        User currentUser=(User)session.getAttribute("currentUser");
        if(currentUser==null){
            return "2";//用户未登录
        }

        Group group=new Group();
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
            group.setImage(url);
        } catch (IOException e) {
            e.printStackTrace();
            return "2";
        }
        group.setName(name);
        group.setOverview(overview);
        group.setTime(new Date());
        group.setAdmin(currentUser.getId());
        group.setGrouptype(grouptype);

        groupRepository.save(group);

        User user=(User)session.getAttribute("currentUser");
        model.addAttribute("currentUser",user);

        List<Group> groupList=groupRepository.findAll();
        model.addAttribute("movieList",groupList);
        return "grouplist";
    }

    @GetMapping("/ifLogin")
    @ResponseBody
    public String ifLogin(HttpServletRequest request){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        if(user==null){
            return "1";
        }
        return "0";
    }
    @GetMapping("/group/{id}")
    public String groupDetail(@PathVariable String id, HttpServletRequest request, Model model){
        HttpSession session=request.getSession();

        User user=(User)session.getAttribute("currentUser");
        if(user==null){
            return "404";
        }

        Optional<Group> group=groupRepository.findById(id);
        if(!group.isPresent()){
            return "404";//没有找到movie
        }
        List<GroupFollow> groupFollowList=groupFollowRepository.findByGidAndAndUid(id,user.getId());

        if(!group.get().getAdmin().equals(user.getId())){//不是管理员
            if(groupFollowList==null || groupFollowList.size()<=0){//没有权限
                model.addAttribute("currentUser",user);
                model.addAttribute("currentMovie",group.get());
                Optional<User> user1=userRepository.findById(group.get().getAdmin());
                model.addAttribute("admin",user1.get());
                List<GroupApply> groupApply=groupApplyRepository.findByGidAndUid(id,user.getId());
                if(groupApply!=null && groupApply.size()>0){
                    model.addAttribute("applySend","1");
                }
                else{
                    model.addAttribute("applySend","0");
                }
                return "apply";
            }
        }

        model.addAttribute("currentMovie",group.get());

        List<GroupPost> groupPostList=groupPostRepository.findByGid(id);
        List<SendReview> sendReviewList=new ArrayList<SendReview>();

        for(GroupPost i:groupPostList){
            SendReview sendReview;

            Optional<User> reviewUser=userRepository.findById(i.getUid());
            List<GroupPostLike> groupPostLikeList;
            if(user!=null){
                groupPostLikeList=groupPostLikeRepository.findByPidAndUid(i.getId(),user.getId());
            }
            else{
                groupPostLikeList=new ArrayList<GroupPostLike>();
            }
            if(groupPostLikeList!=null && groupPostLikeList.size()>0){
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
        Optional<User> user1=userRepository.findById(group.get().getAdmin());
        model.addAttribute("admin",user1.get());
        return "groupsingle";
    }

    @PostMapping("/addGroupPost")
    @ResponseBody
    public String addGroupPost(@RequestBody UploadReview review,HttpServletRequest request){

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
        GroupPost groupPost=new GroupPost();
        groupPost.setUid(currentUser.getId());
        groupPost.setGid(review.getMid());
        groupPost.setContent(review.getContent());
        groupPost.setTitle(review.getTitle());
        groupPost.setTime(new Date());
        groupPostRepository.save(groupPost);

        List<GroupPost> groupPostList=groupPostRepository.findByGid(review.mid);
        Optional<Group> group=groupRepository.findById(review.mid);
        group.get().setRateNumber(groupPostList.size());
        groupRepository.save(group.get());
        return "0";
    }

    @PostMapping("/likeGroup")
    @ResponseBody
    public String likeGroup(@RequestBody Send send,HttpServletRequest request){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        if(user==null){
            return "1";//用户未登录
        }
        String pid=send.getRid();
        List<GroupPostLike> groupPostLikeList=groupPostLikeRepository.findByPidAndUid(pid,user.getId());
        if(groupPostLikeList!=null && groupPostLikeList.size()>0){
            for(GroupPostLike i:groupPostLikeList){
                groupPostLikeRepository.delete(i);
                Optional<GroupPost> groupPost=groupPostRepository.findById(pid);
                if(groupPost.isPresent()){
                    groupPost.get().setLike(groupPost.get().getLike()-1);
                    groupPostRepository.save(groupPost.get());
                }
            }
        }
        else{
            System.out.println("in like");
            GroupPostLike groupPostLike=new GroupPostLike();
            groupPostLike.setPid(pid);
            groupPostLike.setUid(user.getId());
            groupPostLike.setTime(new Date());
            groupPostLikeRepository.save(groupPostLike);
            Optional<GroupPost> groupPost=groupPostRepository.findById(pid);
            if(groupPost.isPresent()){
                System.out.println("in present");
                groupPost.get().setLike(groupPost.get().getLike()+1);
                groupPostRepository.save(groupPost.get());
            }
        }
        return "0";
    }
    @PostMapping("/sendGroupApply")
    @ResponseBody
    public String sendGroupApply(@RequestBody Send send,HttpServletRequest request){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        if(user==null){
            return "1";//用户未登录
        }
        GroupApply groupApply=new GroupApply();
        groupApply.setGid(send.getRid());
        groupApply.setUid(user.getId());
        groupApply.setTime(new Date());

        groupApplyRepository.save(groupApply);
        return "0";
    }

    @PostMapping("/acceptGroupApply")
    @ResponseBody
    public String acceptGroupApply(@RequestBody Send send,HttpServletRequest request){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        Optional<GroupApply> groupApply=groupApplyRepository.findById(send.getRid());
        if(!groupApply.isPresent()){
            return "1";
        }
        Optional<Group> group=groupRepository.findById(groupApply.get().getGid());
        if(!group.isPresent()){
            return "2";
        }
        if(!group.get().getAdmin().equals(user.getId())){
            return "3";
        }
        GroupFollow groupFollow=new GroupFollow();
        groupFollow.setGid(groupApply.get().getGid());
        groupFollow.setUid(groupApply.get().getUid());
        groupFollow.setTime(new Date());

        groupApplyRepository.delete(groupApply.get());
        groupFollowRepository.save(groupFollow);
        return "0";
    }

    @PostMapping("/deleteGroupApply")
    @ResponseBody
    public String deleteGroupApply(@RequestBody Send send,HttpServletRequest request){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        Optional<GroupApply> groupApply=groupApplyRepository.findById(send.getRid());
        if(!groupApply.isPresent()){
            return "1";
        }
        Optional<Group> group=groupRepository.findById(groupApply.get().getGid());
        if(!group.isPresent()){
            return "2";
        }
        if(!group.get().getAdmin().equals(user.getId())){
            return "3";
        }
        groupApplyRepository.delete(groupApply.get());
        return "0";
    }
}
