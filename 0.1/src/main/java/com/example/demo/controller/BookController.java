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
public class BookController {
    @Autowired
    UserRepository userRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    UploadFileRepository uploadFileRepository;
    @Autowired
    BookReviewRepository bookReviewRepository;
    @Autowired
    BookReviewLikeRepository bookReviewLikeRepository;

    @PostMapping("/addBook")
    public String addBook(@RequestParam(value = "image") MultipartFile file, @RequestParam String name, @RequestParam String overview,
                          @RequestParam String author, @RequestParam String press, @RequestParam String price, @RequestParam Date time) {
                    // add by xqy  @RequestParam String author, @RequestParam String press, @RequestParam String price

        if (file.isEmpty()) {
            return "1";
        }
        Book book=new Book();
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
            book.setImage(url);
        } catch (IOException e) {
            e.printStackTrace();
            return "2";
        }
        book.setName(name);
        book.setOverview(overview);
        book.setTime(time);

        // add by xqy
        book.setAuthor(author);
        book.setPress(press);
        book.setPrice(price);

        bookRepository.save(book);
        return "addBook";
    }

    @GetMapping("/book/{id}")
    public String bookDetail(@PathVariable String id, HttpServletRequest request, Model model){
        HttpSession session=request.getSession();

        User user=(User)session.getAttribute("currentUser");

        Optional<Book> book=bookRepository.findById(id);
        if(!book.isPresent()){
            return "404";//没有找到movie
        }
        model.addAttribute("currentMovie",book.get());

        List<BookReview> bookReviewList=bookReviewRepository.findByBid(id);
        List<SendReview> sendReviewList=new ArrayList<SendReview>();

        for(BookReview i:bookReviewList){
            SendReview sendReview;

            Optional<User> reviewUser=userRepository.findById(i.getUid());
            List<BookReviewLike> bookReviewLikeList;
            if(user!=null){
                bookReviewLikeList=bookReviewLikeRepository.findByRidAndUid(i.getId(),user.getId());
            }
            else{
                bookReviewLikeList=new ArrayList<BookReviewLike>();
            }
            if(bookReviewLikeList!=null && bookReviewLikeList.size()>0){
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
        return "booksingle";
    }

    @PostMapping("/addBookReview")
    @ResponseBody
    public String addBookReview(@RequestBody UploadReview review,HttpServletRequest request){

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
        BookReview bookReview=new BookReview();
        bookReview.setUid(currentUser.getId());
        bookReview.setBid(review.getMid());
        bookReview.setContent(review.getContent());
        bookReview.setRate(review.getRate());
        bookReview.setTitle(review.getTitle());
        bookReview.setTime(new Date());
        bookReviewRepository.save(bookReview);

        List<BookReview> bookReviewList=bookReviewRepository.findByBid(review.mid);
        int total=0;
        for(BookReview i:bookReviewList){
            total+=i.getRate();
        }
        Optional<Book> book=bookRepository.findById(review.mid);
        book.get().setRate(total*10/bookReviewList.size());
        book.get().setRateNumber(bookReviewList.size());
        bookRepository.save(book.get());
        return "0";
    }

    @PostMapping("/likeBook")
    @ResponseBody
    public String likeBook(@RequestBody Send send,HttpServletRequest request){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        if(user==null){
            return "1";//用户未登录
        }
        String rid=send.getRid();
        List<BookReviewLike> bookReviewLikeList=bookReviewLikeRepository.findByRidAndUid(rid,user.getId());
        if(bookReviewLikeList!=null && bookReviewLikeList.size()>0){
            for(BookReviewLike i:bookReviewLikeList){
                bookReviewLikeRepository.delete(i);
                Optional<BookReview> bookReview=bookReviewRepository.findById(rid);
                if(bookReview.isPresent()){
                    bookReview.get().setLike(bookReview.get().getLike()-1);
                    bookReviewRepository.save(bookReview.get());
                }
            }
        }
        else{
            System.out.println("in like");
            BookReviewLike bookReviewLike=new BookReviewLike();
            bookReviewLike.setRid(rid);
            bookReviewLike.setUid(user.getId());
            bookReviewLike.setTime(new Date());
            bookReviewLikeRepository.save(bookReviewLike);
            Optional<BookReview> bookReview=bookReviewRepository.findById(rid);
            if(bookReview.isPresent()){
                System.out.println("in present");
                bookReview.get().setLike(bookReview.get().getLike()+1);
                bookReviewRepository.save(bookReview.get());
            }
        }
        return "0";
    }
}

