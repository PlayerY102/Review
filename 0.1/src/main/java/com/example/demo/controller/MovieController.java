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
public class MovieController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    MovieRepository movieRepository;
    @Autowired
    UploadFileRepository uploadFileRepository;
    @Autowired
    MovieReviewRepository movieReviewRepository;
    @Autowired
    MovieReviewLikeRepository movieReviewLikeRepository;

    @PostMapping("/addMovie")
    public String addMovie(@RequestParam(value = "image") MultipartFile file, @RequestParam String name, @RequestParam String overview,
                           @RequestParam String director, @RequestParam String actor, @RequestParam String movietype, @RequestParam Date time) {
                    // add by xqy  @RequestParam String director, @RequestParam String actor, @RequestParam String movietype,

        if (file.isEmpty()) {
            return "1";
        }
        Movie movie = new Movie();
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
            movie.setImage(url);
        } catch (IOException e) {
            e.printStackTrace();
            return "2";
        }
        movie.setName(name);
        movie.setOverview(overview);
        movie.setTime(time);

        // add by xqy
        movie.setDirector(director);
        movie.setActor(actor);
        movie.setMovietype(movietype);

        movieRepository.save(movie);
        return "addMovie";
    }

    @GetMapping("/movie/{id}")
    public String movieDetail(@PathVariable String id,HttpServletRequest request,Model model){
        HttpSession session=request.getSession();

        User user=(User)session.getAttribute("currentUser");

        Optional<Movie> movie=movieRepository.findById(id);
        if(!movie.isPresent()){
            return "404";//没有找到movie
        }
        model.addAttribute("currentMovie",movie.get());

        List<MovieReview> movieReviewList=movieReviewRepository.findByMid(id);
        List<SendReview> sendReviewList=new ArrayList<SendReview>();

        for(MovieReview i:movieReviewList){
            SendReview sendReview;

            Optional<User> reviewUser=userRepository.findById(i.getUid());
            List<MovieReviewLike> movieReviewLikeList;
            if(user!=null){
                movieReviewLikeList=movieReviewLikeRepository.findByRidAndUid(i.getId(),user.getId());
            }
            else{
                movieReviewLikeList=new ArrayList<MovieReviewLike>();
            }
            if(movieReviewLikeList!=null && movieReviewLikeList.size()>0){
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
        return "moviesingle";
    }

    @PostMapping("/addMovieReview")
    @ResponseBody
    public String addMovieReview(@RequestBody UploadReview review,HttpServletRequest request){

        HttpSession session=request.getSession();
        User currentUser=(User)session.getAttribute("currentUser");
        if(currentUser==null){
            return "1";//用户未登录
        }
        if(review.getRate()==null){
            return "2";
        }
        if(review.getContent()==null){
            return "3";
        }
        if(review.getTitle()==null){
            return "4";
        }
        MovieReview movieReview=new MovieReview();
        movieReview.setUid(currentUser.getId());
        movieReview.setMid(review.getMid());
        movieReview.setContent(review.getContent());
        movieReview.setRate(review.getRate());
        movieReview.setTitle(review.getTitle());
        movieReview.setTime(new Date());
        movieReviewRepository.save(movieReview);

        List<MovieReview> movieReviewList=movieReviewRepository.findByMid(review.mid);
        int total=0;
        for(MovieReview i:movieReviewList){
            total+=i.getRate();
        }
        Optional<Movie> movie=movieRepository.findById(review.mid);
        movie.get().setRate(total*10/movieReviewList.size());
        movie.get().setRateNumber(movieReviewList.size());
        movieRepository.save(movie.get());
        return "0";
    }

    @PostMapping("/likeMovie")
    @ResponseBody
    public String likeMovie(@RequestBody Send send,HttpServletRequest request){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        if(user==null){
            return "1";//用户未登录
        }
        String rid=send.getRid();
        List<MovieReviewLike> movieReviewLikeList=movieReviewLikeRepository.findByRidAndUid(rid,user.getId());
        if(movieReviewLikeList!=null && movieReviewLikeList.size()>0){
            for(MovieReviewLike i:movieReviewLikeList){
                movieReviewLikeRepository.delete(i);
                Optional<MovieReview> movieReview=movieReviewRepository.findById(rid);
                if(movieReview.isPresent()){
                    movieReview.get().setLike(movieReview.get().getLike()-1);
                    movieReviewRepository.save(movieReview.get());
                }
            }
        }
        else{
            System.out.println("in like");
            MovieReviewLike movieReviewLike=new MovieReviewLike();
            movieReviewLike.setRid(rid);
            movieReviewLike.setUid(user.getId());
            movieReviewLike.setTime(new Date());
            movieReviewLikeRepository.save(movieReviewLike);
            Optional<MovieReview> movieReview=movieReviewRepository.findById(rid);
            if(movieReview.isPresent()){
                System.out.println("in present");
                movieReview.get().setLike(movieReview.get().getLike()+1);
                movieReviewRepository.save(movieReview.get());
            }
        }
        return "0";
    }
}
class UploadReview{
    Integer rate;
    String title;
    String mid;
    String content;

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
class SendReview{
    private String rid;
    private String uid;
    private String title;
    private String content;
    private Integer rate;
    private Date time;
    private Integer like;
    private Integer ifLike;
    private String userName;

    public SendReview(MovieReview movieReview,Integer ifLike,String userName) {
        this.rid=movieReview.getId();
        this.uid=movieReview.getUid();
        this.title=movieReview.getTitle();
        this.content=movieReview.getContent();
        this.rate=movieReview.getRate();
        this.time=movieReview.getTime();
        this.like=movieReview.getLike();
        this.ifLike=ifLike;
        this.userName=userName;
    }

    public SendReview(BookReview bookReview,Integer ifLike,String userName) {
        this.rid=bookReview.getId();
        this.uid=bookReview.getUid();
        this.title=bookReview.getTitle();
        this.content=bookReview.getContent();
        this.rate=bookReview.getRate();
        this.time=bookReview.getTime();
        this.like=bookReview.getLike();
        this.ifLike=ifLike;
        this.userName=userName;
    }
    public SendReview(TopicPost topicPost,Integer ifLike,String userName){
        this.rid=topicPost.getId();
        this.uid=topicPost.getUid();
        this.title=topicPost.getTitle();
        this.content=topicPost.getContent();
        this.rate=0;
        this.time=topicPost.getTime();
        this.like=topicPost.getLike();
        this.ifLike=ifLike;
        this.userName=userName;
    }
    public SendReview(GroupPost groupPost,Integer ifLike,String userName){
        this.rid=groupPost.getId();
        this.uid=groupPost.getUid();
        this.title=groupPost.getTitle();
        this.content=groupPost.getContent();
        this.rate=0;
        this.time=groupPost.getTime();
        this.like=groupPost.getLike();
        this.ifLike=ifLike;
        this.userName=userName;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Integer getLike() {
        return like;
    }

    public void setLike(Integer like) {
        this.like = like;
    }

    public Integer getIfLike() {
        return ifLike;
    }

    public void setIfLike(Integer ifLike) {
        this.ifLike = ifLike;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
class Send{
    private String rid;

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }
}
