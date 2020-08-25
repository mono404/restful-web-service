package com.mono.restfulwebservice.helloworld;
//lombok

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // 데이터 get, set
@AllArgsConstructor // 추가한 변수의 생성자
@NoArgsConstructor // 디폴트 생성자
public class HelloWorldBean {

    private String message;


/* 위에서 @(어노테이션) 덕분에 추가 안해줘도 됨

    //lombok의 Data를 @(어노테이션)해주었기 때문에 게터(geter), 세터(seter) 추가 x.
    public String getMessage() {
        return this.message;
    }

    public void setMessage(String msg) {
        this.message = msg;
    }

    //lombok의 AllArgsConstructor를 @(어노테이션) 해주었기 때문에 생성자(constructor) 추가 x.
    public HelloWorldBean(String message) {
        this.message = message;
    }
 */
}
