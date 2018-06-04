package com.wxind.httpexecutor.executor;

import org.springframework.web.client.RestTemplate;

/**
 * Created by wangxindong on 18/5/22
 */
public abstract class RestTemplateRouter<T extends RestTemplate> implements Router<RestTemplate,String> {

    @Override
    public abstract RestTemplate choose(String key);
}
