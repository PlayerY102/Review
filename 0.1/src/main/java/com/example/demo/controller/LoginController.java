package com.example.demo.controller;

import com.example.demo.entity.*;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;


@Controller
public class LoginController {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private UserRepository userRepository;

    // add by xqy bookRepository
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UploadFileRepository uploadFileRepository;

    @Autowired
    private MovieReviewRepository movieReviewRepository;

    @Autowired
    private MovieReviewLikeRepository movieReviewLikeRepository;

    // add by xqy bookReviewRepository bookReviewLikeRepository
    @Autowired
    private BookReviewRepository bookReviewRepository;

    @Autowired
    private BookReviewLikeRepository bookReviewLikeRepository;

    @Autowired
    private TopicPostRepository topicPostRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupApplyRepository groupApplyRepository;

    @Autowired
    private GroupFollowRepository groupFollowRepository;

    @PostMapping(path = "/login")//登录表单的post
    @ResponseBody//返回的是一个状态，让网页进行处理
    public Object login(@RequestParam String email, @RequestParam String password, HttpServletRequest request){

        HttpSession session=request.getSession();

        List<User> users = userRepository.findByEmail(email);

        if (users == null||users.size()<=0) {
            return "1";//用户不存在
        }
        User user = users.get(0);
        if (!user.getPassword().equals(password)) {
            return "2";//密码错误
        }

        session.setAttribute("currentUser",user);//将当前用户存入session中
        return "0";//成功登录
    }

    @PostMapping(path="/add")//注册用户的post申请
    @ResponseBody//返回的是一个状态给网页，让网页进行提示交互
    public Object addNewUser (@RequestParam String name
            , @RequestParam String email, @RequestParam String password, HttpServletRequest request){

        HttpSession session=request.getSession();

        List<User> list=userRepository.findByEmail(email);
        if(list!=null&&list.size()>0){
            return "1";//email已注册
        }
        User user=new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setImage(null);
        userRepository.save(user);		//添加到db
        List<User> users=userRepository.findByEmail(email);
        if (users == null||users.size()<=0) {
            return "2";//数据库存储失败
        }
        user=users.get(0);

        session.setAttribute("currentUser",user);
        return "0";//成功注册
    }

    @RequestMapping("/logout")//登出
    public String logout(HttpServletRequest request, Model model){
        HttpSession session=request.getSession();
        session.setAttribute("currentUser",null);
        model.addAttribute("currentUser",null);

        List<Movie> movieList=movieRepository.findAllByOrderByTimeDesc();
        model.addAttribute("movieList",movieList.subList(0, Math.min(15, movieList.size())));

        List<Book> bookList=bookRepository.findAllByOrderByTimeDesc();
        model.addAttribute("bookList",bookList.subList(0,Math.min(15,bookList.size())));

        List<SendHome> sendHomeList=new ArrayList<SendHome>();
        for(Movie movie:movieList){
            SendHome sendHome=new SendHome(movie);
            sendHomeList.add(sendHome);
        }
        for(Book book:bookList){
            SendHome sendHome=new SendHome(book);
            sendHomeList.add(sendHome);
        }
        Collections.sort(sendHomeList, new Comparator<SendHome>() {
            @Override
            public int compare(SendHome sendHome, SendHome t1) {
                return t1.getRateNumber().compareTo(sendHome.getRateNumber());
            }
        });

        model.addAttribute("homeList",sendHomeList.subList(0,Math.min(10,sendHomeList.size())));

        return "index";
    }

    @GetMapping(value = "/user/image/{id}",produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE})
    @ResponseBody
    public byte[] userImage(@PathVariable String id) throws IOException {
        byte[] data = null;
        Optional<User> user=userRepository.findById(id);
        if(!user.isPresent()){
            return null;
        }
        if(user.get().getImage()==null){
            File file = new File("./src/main/resources/static/images/uploads/user-img.jpg");
            FileInputStream inputStream = new FileInputStream(file);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes, 0, inputStream.available());
            return bytes;
        }
        Optional<UploadFile> uploadFile=uploadFileRepository.findById(user.get().getImage());
        return uploadFile.get().getContent().getData();
    }


    @GetMapping("/userProfile")
    public String showUserProfile(HttpServletRequest request,Model model){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        if(user==null){
            return "404";
        }

        // 电影：添加用户发表的评论
        List<MovieReview> movieReviewList=movieReviewRepository.findByUid(user.getId());
        List<SendUserReview> userReviewList = new ArrayList<SendUserReview>();

        for(MovieReview i:movieReviewList){
            SendUserReview sendUserReview=new SendUserReview();
            sendUserReview.setTitle(i.getTitle());
            sendUserReview.setContent(i.getContent());
            sendUserReview.setLike(i.getLike());
            sendUserReview.setMid(i.getMid());
            sendUserReview.setRate(i.getRate());
            sendUserReview.setTime(i.getTime());
            Optional<Movie> movie=movieRepository.findById(i.getMid());
            if(movie.isPresent()){
                sendUserReview.setMovieName(movie.get().getName());
                sendUserReview.setImage(movie.get().getImage());
            }
            userReviewList.add(sendUserReview);
        }
        // 电影：　添加用户收藏的评论
        List<MovieReviewLike> movieReviewLikeList=movieReviewLikeRepository.findByUid(user.getId());
        List<MovieReview> likeReviewList=new ArrayList<MovieReview>();
        for(MovieReviewLike i:movieReviewLikeList){
            Optional<MovieReview> movieReview=movieReviewRepository.findById(i.getRid());
            if(movieReview.isPresent()){
                likeReviewList.add(movieReview.get());
            }
        }

        List<SendUserReview>sendLikeList=new ArrayList<SendUserReview>();
        for(MovieReview i:likeReviewList){
            SendUserReview sendUserReview=new SendUserReview();
            sendUserReview.setTitle(i.getTitle());
            sendUserReview.setContent(i.getContent());
            sendUserReview.setLike(i.getLike());
            sendUserReview.setMid(i.getMid());
            sendUserReview.setRate(i.getRate());
            sendUserReview.setTime(i.getTime());
            Optional<Movie> movie=movieRepository.findById(i.getMid());
            if(movie.isPresent()){
                sendUserReview.setMovieName(movie.get().getName());
                sendUserReview.setImage(movie.get().getImage());
            }
            sendLikeList.add(sendUserReview);
        }
        model.addAttribute("currentUser",user);
        model.addAttribute("userReviewList",userReviewList);
        model.addAttribute("likeReviewList",sendLikeList);



        // add by xqy  userReviewListBook, sendLikeListBook

        // 图书：　添加用户评论的书籍评论
        List<BookReview> bookReviewList= bookReviewRepository.findByUid(user.getId());
        List<SendUserReviewBook> userReviewListBook=new ArrayList<SendUserReviewBook>();
        for(BookReview i:bookReviewList){
            SendUserReviewBook sendUserReviewBook=new SendUserReviewBook();
            sendUserReviewBook.setTitle(i.getTitle());
            sendUserReviewBook.setContent(i.getContent());
            sendUserReviewBook.setLike(i.getLike());
            sendUserReviewBook.setBid(i.getBid());
            sendUserReviewBook.setRate(i.getRate());
            sendUserReviewBook.setTime(i.getTime());
            Optional<Book> book= bookRepository.findById(i.getBid());
            if(book.isPresent()){
                sendUserReviewBook.setBookName(book.get().getName());
                sendUserReviewBook.setImage(book.get().getImage());
            }
            userReviewListBook.add(sendUserReviewBook);
        }

        // 图书：　添加用户收藏的图书评价
        List<BookReviewLike> bookReviewLikeList = bookReviewLikeRepository.findByUid(user.getId());
        List<BookReview> likeReviewListBook=new ArrayList<BookReview>();

        for(BookReviewLike i:bookReviewLikeList){
            Optional<BookReview> bookReview = bookReviewRepository.findById(i.getRid());
            if(bookReview.isPresent()){
                likeReviewListBook.add(bookReview.get());
            }
        }

        List<SendUserReviewBook> sendLikeListBook = new ArrayList<SendUserReviewBook>();
        for(BookReview i:likeReviewListBook){
            SendUserReviewBook sendUserReviewBook=new SendUserReviewBook();
            sendUserReviewBook.setTitle(i.getTitle());
            sendUserReviewBook.setContent(i.getContent());
            sendUserReviewBook.setLike(i.getLike());
            sendUserReviewBook.setBid(i.getBid());
            sendUserReviewBook.setRate(i.getRate());
            sendUserReviewBook.setTime(i.getTime());
            Optional<Book> book = bookRepository.findById(i.getBid());
            if(book.isPresent()){
                sendUserReviewBook.setBookName(book.get().getName());
                sendUserReviewBook.setImage(book.get().getImage());
            }
            sendLikeListBook.add(sendUserReviewBook);
        }


        model.addAttribute("userReviewListBook",userReviewListBook);
        model.addAttribute("likeReviewListBook",sendLikeListBook);


        List<Group> groupList=new ArrayList<Group>();

        List<Group> groupList1=groupRepository.findByAdmin(user.getId());
        for(Group i:groupList1){
            groupList.add(i);
        }

        List<GroupFollow> groupFollowList=groupFollowRepository.findByUid(user.getId());

        for(GroupFollow i:groupFollowList){
            Optional<Group> group=groupRepository.findById(i.getGid());
            if(group.isPresent()){
                groupList.add(group.get());
            }
        }

        model.addAttribute("groupList",groupList);



        List<GroupApply> groupApplyList=new ArrayList<GroupApply>();
        for(Group i:groupList1){
            List<GroupApply> temp=groupApplyRepository.findByGid(i.getId());
            groupApplyList.addAll(temp);
        }

        List<SendUserApply> sendUserApplyList=new ArrayList<SendUserApply>();
        for(GroupApply i:groupApplyList){
            SendUserApply sendUserApply=new SendUserApply();
            sendUserApply.setAid(i.getId());
            sendUserApply.setGid(i.getGid());
            sendUserApply.setTime(i.getTime());
            sendUserApply.setUid(i.getUid());

            Optional<User> user1=userRepository.findById(i.getUid());
            if(user1.isPresent()){
                sendUserApply.setUserImage(user1.get().getImage());
                sendUserApply.setUserName(user1.get().getName());
            }

            Optional<Group> group=groupRepository.findById(i.getGid());
            if(group.isPresent()){
                sendUserApply.setGroupImage(group.get().getImage());
                sendUserApply.setGroupName(group.get().getName());
            }
            sendUserApplyList.add(sendUserApply);
        }
        model.addAttribute("groupApplyList",sendUserApplyList);

        return "userprofile";
    }

    @PostMapping("/changeUserImage")
    @ResponseBody
    public String changeUserImage(@RequestParam(value = "image") MultipartFile file,HttpServletRequest request){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        if(user==null){
            return "1";//用户未登录
        }
        if (file.isEmpty()) {
            return "2";//空文件
        }
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
            user.setImage(url);
        } catch (IOException e) {
            e.printStackTrace();
            return "3";//保存文件出错
        }
        userRepository.save(user);
        return "0";
    }
}


class SendUserReview{
    private String mid;
    private String image;
    private String title;
    private String content;
    private Integer rate;
    private Integer like;
    private Date time;
    private String movieName;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public Integer getLike() {
        return like;
    }

    public void setLike(Integer like) {
        this.like = like;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }
}


// add by xqy SendUserReviewBook
class SendUserReviewBook{
    private String bid;
    private String image;
    private String title;
    private String content;
    private Integer rate;
    private Integer like;
    private Date time;
    private String bookName;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    public Integer getLike() {
        return like;
    }

    public void setLike(Integer like) {
        this.like = like;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
}

class SendUserApply{
    private String uid;
    private String userImage;
    private String userName;
    private String aid;
    private String gid;
    private String groupImage;
    private String groupName;
    private Date time;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
