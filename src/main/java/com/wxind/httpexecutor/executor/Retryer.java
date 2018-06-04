package com.wxind.httpexecutor.executor;

/**
 * 重试机制
 * Created by wangxindong on 18/5/16
 */
@FunctionalInterface
public interface Retryer {


    /**
     * 是否重试函数
     * 用来设置判断重试的条件
     * @param e
     * @return
     */
     boolean retry(RetryException e);

    /**
     *
     * @param retryer  重试机制
     * @param retryException  用来承载抛出的异常和正常的返回值
     * @param retryMeta  重试的元数据
     * @return
     */
     default boolean continueOrEnd(Retryer retryer, RetryException retryException, RetryMeta retryMeta){
         //判断是否超出设置的重试次数
         if (null == retryMeta || retryMeta.attempt++ > retryMeta.maxAttempts){
             return false;
         }
         //判断是否符合重试机制的要求
         boolean retry = retryer.retry(retryException);
         //符合重试要求，进行重试
         if (retry){
             try {
                 //重试间隔通过sleep实现
                 Thread.sleep(retryMeta.interval);
             } catch (InterruptedException e) {
             }
         }
         return retry;
     }

    /**
     * 重试机制所需要的元数据
     */
    class RetryMeta{
         public static final RetryMeta NO_RETRY = new RetryMeta(0,0);
         /**
          * 最大重试次数
          */
         private int maxAttempts;
         /**
          * 重试间隔
          * 单位为毫秒
          */
         private int interval;
         /**
          * 尝试次数
          */
         private int attempt;

         /**
          * 默认构造
          * 尝试二次
          * 每次间隔2秒
          */
         public RetryMeta() {
             this.maxAttempts = 2;
             this.interval = 2000;
             this.attempt = 1;
         }

         public RetryMeta(int maxAttempts, int interval) {
             this.maxAttempts = maxAttempts;
             this.interval = interval;
             this.attempt = 1;
         }

         public int getMaxAttempts() {
             return maxAttempts;
         }

         public void setMaxAttempts(int maxAttempts) {
             this.maxAttempts = maxAttempts;
         }

         public int getInterval() {
             return interval;
         }

         public void setInterval(int interval) {
             this.interval = interval;
         }

         public int getAttempt() {
             return attempt;
         }

         public void setAttempt(int attempt) {
             this.attempt = attempt;
         }
     }
}
