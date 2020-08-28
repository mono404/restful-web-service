package com.mono.restfulwebservice.helloworld;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HttpServletBean;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

@RestController
public class HelloWorldController {

    @Autowired
    private MessageSource messageSource;

    // Get
    // /hello-world (endpoint)

    // @RequestMapping(method = RequestMethod.GET, path = "/hello-world")
    @GetMapping(path = "/hello-world")
    public String helloWorld() {
        return "Hello World";
    }

    // alt + enter
    @GetMapping(path = "/hello-world-bean")
    public HelloWorldBean helloWorldBean() {
        return new HelloWorldBean("Hello World");
    }

    @GetMapping(path = "/hello-world-bean/path-variable/{name}")
    public HelloWorldBean helloWorldBean(@PathVariable String name) {// 다른값일 경우 @PathVariable(value = "name")
        return new HelloWorldBean(String.format("Hello World, %s", name));
    }

    @GetMapping(path = "/hello-world-internationalized")
    public String helloWorldInternationalized(@RequestHeader(name = "Accept-Language", required = false) Locale locale) {

        return messageSource.getMessage("greeting.message", null, locale);
    }

    @GetMapping(path = "/hello-world/test")
    public ModelAndView test() {
        ModelAndView modelAndView = new ModelAndView("helloworldtest");
        return modelAndView;
    }

    @PostMapping(path = "/hello-world/submit")
    public String submit(HttpServletRequest req) {
        String name = req.getParameter("names");
        String number = req.getParameter("number");

        return "이름은 " + name + " 번호는 " + number + " 입니다.";
    }
}
