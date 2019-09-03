package com.example.demo.controller;

import com.example.demo.entity.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


@Controller
public class AdminController {
    @Autowired
    private AdminRepository repository;


    // 管理员登录
    @PostMapping(path = "/adminLogin")//登录表单的post
    @ResponseBody//返回的是一个状态，让网页进行处理
    public Object login(@RequestParam String email, @RequestParam String password, HttpServletRequest request){

        HttpSession session=request.getSession();
        if (!email.equals("admin@qq.com")) {
            return "1";//用户不存在
        }

        if (!password.equals("123456")) {
            return "2"; //密码错误
        }

        session.setAttribute("currentAdmin", "admin"); //将当前用户存入session中
        return "0";//成功登录
    }

    // 管理员登出
    @RequestMapping("/adminLogout")//登出
    public String logout(HttpServletRequest request, Model model){
        HttpSession session=request.getSession();
        session.setAttribute("currentAdmin",null);
        model.addAttribute("currentAdmin",null);
        return "adminLogin";
    }
}
