package com.example.demo.controller;

import com.example.demo.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
public class MainController {

    @Autowired
    MovieRepository movieRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    TopicRepository topicRepository;
    @Autowired
    GroupRepository groupRepository;

    @GetMapping("/")
    public String showBegin(HttpServletRequest request, Model model) {
        HttpSession session=request.getSession();

        User user=(User)session.getAttribute("currentUser");
        model.addAttribute("currentUser",user);

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

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }
    // 管理员登录
    @GetMapping("/adminLogin")
    public String adminLogin(){
        return "adminLogin";
    }
    // 管理员首页
//    adminIndex
    @GetMapping("/adminIndex")
    public String adminIndex (HttpServletRequest request, Model model) {
        HttpSession session=request.getSession();
        String admin=(String)session.getAttribute("currentAdmin");

        if (admin == null) {
            return "redirect:adminLogin";
        } else {
            model.addAttribute("currentAdmin", admin);
            return "adminIndex";
        }
    }

    @GetMapping("/updateMain")
    public String updateMain(HttpServletRequest request, Model model) {
        return showBegin(request,model);
    }
    @GetMapping("/addMovie")
    public String addMovie(HttpServletRequest request, Model model){
        HttpSession session=request.getSession();
        String admin=(String)session.getAttribute("currentAdmin");

        if (admin == null) {
            return "redirect:adminLogin";
        } else {
            model.addAttribute("currentAdmin", admin);
            return "addMovie";
        }
    }
    @GetMapping("/addBook")
    public String addBook(HttpServletRequest request, Model model){
        HttpSession session=request.getSession();
        String admin=(String)session.getAttribute("currentAdmin");

        if (admin == null) {
            return "redirect:adminLogin";
        } else {
            model.addAttribute("currentAdmin", admin);
            return "addBook";
        }

    }
    @GetMapping("/addTopic")
    public String addTopic(HttpServletRequest request, Model model){
        HttpSession session=request.getSession();
        String admin=(String)session.getAttribute("currentAdmin");

        if (admin == null) {
            return "redirect:adminLogin";
        } else {
            model.addAttribute("currentAdmin", admin);
            return "addTopic";
        }
    }
    @GetMapping("addGroup")
    public String addGroup(){
        return "addGroup";
    }

    @GetMapping("/movieList")
    public String showMovieList(HttpServletRequest request, Model model){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        model.addAttribute("currentUser",user);

        List<Movie> movieList=movieRepository.findAll();
        model.addAttribute("movieList",movieList);

        return "movielist";
    }
    @GetMapping("/bookList")
    public String showBookList(HttpServletRequest request, Model model){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        model.addAttribute("currentUser",user);

        List<Book> bookList=bookRepository.findAll();
        model.addAttribute("movieList",bookList);
        return "booklist";
    }
    @GetMapping("/topicList")
    public String showTopicList(HttpServletRequest request, Model model){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        model.addAttribute("currentUser",user);

        List<Topic> topicList=topicRepository.findAll();
        model.addAttribute("movieList",topicList);
        return "topiclist";
    }
    @GetMapping("groupList")
    public String showGroupList(HttpServletRequest request, Model model){
        HttpSession session=request.getSession();
        User user=(User)session.getAttribute("currentUser");
        model.addAttribute("currentUser",user);

        List<Group> groupList=groupRepository.findAll();
        model.addAttribute("movieList",groupList);
        return "grouplist";
    }

    @PostMapping("/search")
    public String search(@RequestParam String type,@RequestParam String name,Model model){
        if(type.equals("movie")){
            List<Movie> movieList=movieRepository.findByNameLike(name);

            model.addAttribute("movieList",movieList);
            return "movielist";
        }
        else{
            List<Book> bookList=bookRepository.findByNameLike(name);

            model.addAttribute("movieList",bookList);
            return "booklist";
        }
    }
    @PostMapping("/searchMovie")
    public String searchMovie(@RequestParam String name,Model model){
        List<Movie> movieList=movieRepository.findByNameLike(name);

        model.addAttribute("movieList",movieList);
        return "movielist";
    }
    @PostMapping("/searchBook")
    public String searchBook(@RequestParam String name,Model model){
        List<Book> bookList=bookRepository.findByNameLike(name);

        model.addAttribute("bookList",bookList);
        return "booklist";
    }

}
class SendHome{
    private String type;
    private String name;
    private String id;
    private Integer rate;
    private Integer rateNumber;
    private String img;

    public SendHome(Movie movie){
        this.type="movie";
        this.name=movie.getName();
        this.id=movie.getId();
        this.rate=movie.getRate();
        this.rateNumber=movie.getRateNumber();
        this.img=movie.getImage();
    }
    public SendHome(Book book){
        this.type="book";
        this.name=book.getName();
        this.id=book.getId();
        this.rate=book.getRate();
        this.rateNumber=book.getRateNumber();
        this.img=book.getImage();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public Integer getRateNumber() {
        return rateNumber;
    }

    public void setRateNumber(Integer rateNumber) {
        this.rateNumber = rateNumber;
    }
}
