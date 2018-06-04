package com.wxind.httpexecutor.executor.impl;

import com.alibaba.fastjson.JSON;
import com.wxind.httpexecutor.executor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/**
 * Created by wangxindong on 18/5/17
 */
@Component
public class CommonExecutors implements HttpExecutors {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource(name = "restTemplateRouter")
    private Router restTemplateRouter;

    //默认的重试机制
    private static final Retryer DEFAULT_RETRYER = new CommonRetryer();

    @Override
    public <T> T executor(Task<T> task, Retryer retryer, Retryer.RetryMeta retryMeta, RestTemplateType restTemplateType) throws Exception {
        //先根据restTemplateType选出需要用到的restTemplate
        RestTemplate restTemplate = (RestTemplate) restTemplateRouter.choose(restTemplateType.name());
        T result = null;
        //new一个承载异常和正常返回值的容器RetryException
        RetryException exception = new RetryException();

        long overallStartTime=System.currentTimeMillis();
        logger.info("[HttpExecutors--进入HttpExecutors-{},本次任务最大需要重试{}次]", overallStartTime,null == retryMeta ? 0 : retryMeta.getMaxAttempts());
        while (true) {
            long startTime=System.currentTimeMillis();
            try {
                //每次循环时先清空RetryException容器
                exception.clear();
                //执行我们具体的任务
                result = task.excutor(restTemplate);
                //如果返回的结果正常，将结果放入RetryException
                exception.setResult(result);
            } catch (Exception e) {
                //如果返回异常，将异常放入RetryException
                exception.setException(e);
            }finally {
                logger.info("[HttpExecutors--本次HTTP请求结束]" +"\r\n结果：[{}]" + "\r\n异常信息：[{}]" + "\r\n耗时:[{}]", JSON.toJSONString(result),exception.getException()
                        ,(System.currentTimeMillis()-startTime));
                logger.info("[HttpExecutors--判断本次HTTP请求是否触发重试]");
                //判断是否需要重试
                if (null != retryer && retryer.continueOrEnd(retryer,exception,retryMeta)){
                    logger.info("[HttpExecutors--重试机制生效，已等待：{},开始第{}次重试]",retryMeta.getInterval(), retryMeta.getAttempt() - 1);
                    //进行重试，此时已经睡了间隔时间
                    continue;
                }else {
                    //跳出重试
                    logger.info("[HttpExecutors--HTTP请求{}，准备退出]",null == retryer ? "无重试机制" : null == retryMeta || retryMeta.getMaxAttempts() == 0 ? "无重试次数" : retryMeta.getAttempt() > retryMeta.getMaxAttempts() ? "到达重试临界值" : "不满足重试要求");
                    if (null != exception.getResult()){
                        //跳出重试时任务执行没有异常
                        logger.info("[HttpExecutors--退出--HTTP请求状态正常，返回结果可能未能达到预期]-[总耗时：{}]",System.currentTimeMillis() - overallStartTime);
                        return result;
                    }else {
                        //跳出重试时任务执行发生异常
                        logger.error("[HttpExecutors--退出--HTTP请求状态异常]-[总耗时：{}]", System.currentTimeMillis() - overallStartTime);
                        throw exception.getException();
                    }
                }

            }
        }
    }

    @Override
    public <T> T executor(Task<T> task, boolean needRetry, RestTemplateType restTemplateType) throws Exception {
        //如果needRetry为true，使用默认的重试机制，以及默认的重试元数据（重试2次，间隔2秒）
        return executor(task, needRetry ? DEFAULT_RETRYER : null, needRetry ? new Retryer.RetryMeta() : Retryer.RetryMeta.NO_RETRY, restTemplateType);
    }

}
