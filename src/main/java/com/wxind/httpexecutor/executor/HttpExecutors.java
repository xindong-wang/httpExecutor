package com.wxind.httpexecutor.executor;


/**
 * Created by wangxindong on 18/5/17
 */
public interface HttpExecutors {
    /**
     * 自己设置重试机制、重试次数和重试间隔时间
     * @param task 需执行的restTemplate任务
     * @param retryer 重试机制
     * @param retryMeta 重试机制的元数据，包括重试次数，以及重试的间隔时间
     * @param restTemplateType  需要使用的restTemplate
     * @param <T>  返回值类型，与restTemplate需要的返回值类型相同
     * @return
     * @throws Exception
     */
    <T> T executor(Task<T> task, Retryer retryer, Retryer.RetryMeta retryMeta, RestTemplateType restTemplateType) throws Exception;

    /**
     * 根据needRetry判断是否需要重试，如果needRetry为true，则使用默认的重试机制以及默认的次数和间隔时间
     * 默认重试次数为2次  默认间隔时间为2秒
     * @param task 需执行的restTemplate任务
     * @param needRetry  是否需要重试
     * @param restTemplateType 需要使用的restTemplate
     * @param <T> 返回值类型，与restTemplate需要的返回值类型相同
     * @return
     * @throws Exception
     */
    <T> T executor(Task<T> task, boolean needRetry, RestTemplateType restTemplateType) throws Exception;

}
