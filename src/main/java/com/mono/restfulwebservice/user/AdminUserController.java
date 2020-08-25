package com.mono.restfulwebservice.user;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin") // 공통된 맵핑
public class AdminUserController {
    private UserDaoService service;

    public AdminUserController(UserDaoService service) {
        this.service = service;
    }

    //@GetMapping("/admin/users") // 개별맵핑
    @GetMapping("/users")
    public MappingJacksonValue retrieveAllUsers() {

        List<User> users = service.findAll();

        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
                .filterOutAllExcept("id", "name", "joinDate", "password");

        FilterProvider filters = new SimpleFilterProvider().addFilter("UserInfo", filter);

        MappingJacksonValue mapping = new MappingJacksonValue(users);

        mapping.setFilters(filters);

        return mapping;
    }

    // GET admin/users/1 -> /admin/v1/users/1
    //@GetMapping("/admin/users/{id}") //개별맵핑

    /*      버전관리        */
    //@GetMapping("/v1/users/{id}") // uri를 사용하여 버전관리
    //@GetMapping(value = "/users/{id}/", params = "version=1") //RequestParameter 사용
    //@GetMapping(value = "/users/{id}", headers="X-API-VERSION=1") //Header 사용
    @GetMapping(value = "/users/{id}", produces = "application/vnd.company.appv1+json") //mine-type 사용
    public MappingJacksonValue retrieveUserV1(@PathVariable int id) {

        User user = service.findOne(id);

        if(user == null) {
            throw new UserNotFoundException(String.format("ID[%s] not found", id));
        }

        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
                .filterOutAllExcept("id", "name", "joinDate", "password", "ssn");

        FilterProvider filters = new SimpleFilterProvider().addFilter("UserInfo", filter);

        MappingJacksonValue mapping = new MappingJacksonValue(user);

        mapping.setFilters(filters);

        return mapping;
    }

    //@GetMapping("/v2/users/{id}") // uri를 사용하여 버전관리
    //@GetMapping(value = "/users/{id}/", params = "version=2") //RequestParameter 사용
    //@GetMapping(value = "/users/{id}", headers="X-API-VERSION=2") //Header 사용
    @GetMapping(value = "/users/{id}", produces = "application/vnd.company.appv2+json") //mine-type 사용
    public MappingJacksonValue retrieveUserV2(@PathVariable int id) {

        User user = service.findOne(id);

        if(user == null) {
            throw new UserNotFoundException(String.format("ID[%s] not found", id));
        }

        // User -> UserV2
        UserV2 userV2 = new UserV2();
        BeanUtils.copyProperties(user, userV2); // id, name, joinDate, password, ssn
        userV2.setGrade("VIP");

        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter
                .filterOutAllExcept("id", "name", "joinDate", "grade");

        FilterProvider filters = new SimpleFilterProvider().addFilter("UserInfoV2", filter);

        MappingJacksonValue mapping = new MappingJacksonValue(userV2);

        mapping.setFilters(filters);

        return mapping;
    }
}
