package com.wxind.httpexecutor.executor;


import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

/**
 * Created by wangxindong on 18/5/17
 */
@Component
public class CommonRetryer implements Retryer{


    @Override
    public boolean retry(RetryException e) {
        if (e.getException() instanceof ResourceAccessException){
            return true;
        }
        return false;
    }

}
