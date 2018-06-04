package com.wxind.httpexecutor.executor;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;


/**
 * Created by wangxindong on 18/5/17
 */
@Component("restTemplateRouter")
public class DynamicsRestTemplateRouter extends RestTemplateRouter {

    @Resource(name = "shortRestTemplate")
    private RestTemplate bigDataRestTemplate;


    @Resource(name = "longRestTemplate")
    private RestTemplate longRestTemplate;


    @Resource(name = "commonRestTemplate")
    private RestTemplate commonRestTemplate;


    @Override
    public RestTemplate choose(String key) {

        switch (key){
            case "SHORT":
                return bigDataRestTemplate;
            case "LONG":
                return longRestTemplate;
            default:
                return commonRestTemplate;

        }
    }
}
