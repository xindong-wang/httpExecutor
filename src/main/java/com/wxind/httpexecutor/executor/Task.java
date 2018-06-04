package com.wxind.httpexecutor.executor;

import org.springframework.web.client.RestTemplate;

/**
 * Created by wangxindong on 18/5/17
 */
public interface Task<T> {

     T excutor(RestTemplate restTemplate);
}
