package com.wxind.httpexecutor;

import com.wxind.httpexecutor.executor.HttpExecutors;
import com.wxind.httpexecutor.executor.RestTemplateType;
import com.wxind.httpexecutor.executor.RetryException;
import com.wxind.httpexecutor.executor.Retryer;
import jdk.nashorn.internal.ir.RuntimeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@RestController
public class HttpexecutorApplication {

	private static final HostnameVerifier PROMISCUOUS_VERIFIER = (s, sslSession ) -> true;

	public static void main(String[] args) {
		SpringApplication.run(HttpexecutorApplication.class, args);
	}


	@Autowired
	private HttpExecutors httpClient;

	@RequestMapping("/testApi/decision")
	public String decision(){


		Map<String,Object> requestObject = new HashMap<>();
		//重试机制为全部重试，重试1次，间隔2秒
		try {
			RuntimeNode.Request excutor = httpClient.executor((RestTemplate r) -> r.getForObject("xxxxxxxxx", RuntimeNode.Request.class, requestObject),
					(RetryException e) -> true,
					new Retryer.RetryMeta(1, 2000), RestTemplateType.LONG);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//重试机制抛出异常，并且异常为NullPointerException时重试，重试3次，间隔1秒
		try {
			HashMap excutor1 = httpClient.executor((RestTemplate r) -> r.getForObject("xxxxxxxxx", HashMap.class, requestObject),
					(RetryException e) -> e.getException() instanceof NullPointerException,
					new Retryer.RetryMeta(3, 1000), RestTemplateType.LONG);
		} catch (Exception e) {
			e.printStackTrace();

		}

		//重试机制为返回值为HashMap，并且HashMap包含111的key时进行重试，重试3次，间隔1秒
		try {
			HashMap excutor1 = httpClient.executor((RestTemplate r) -> r.getForObject("xxxxxxxxx", HashMap.class, requestObject),
					(RetryException e) -> {
						HashMap result = (HashMap) e.getResult();
						return result.containsKey("111");
					},
					new Retryer.RetryMeta(3, 1000), RestTemplateType.LONG);
		} catch (Exception e) {
			e.printStackTrace();

		}

		//重试机制为全部重试，重试0次，间隔2秒
		try {
			HashMap excutor2 = httpClient.executor((RestTemplate r) -> r.getForObject("xxxxxxxxx", HashMap.class, requestObject),
					(RetryException e) -> true,
					new Retryer.RetryMeta(0, 2000), RestTemplateType.LONG);
		} catch (Exception e) {
			e.printStackTrace();

		}

		//重试机制为全部重试，无重试元数据 不进行重试
		try {
			HashMap excutor3 = httpClient.executor((RestTemplate r) -> r.getForObject("xxxxxxxxx", HashMap.class, requestObject),
					(RetryException e) -> true, null, RestTemplateType.LONG);
		} catch (Exception e) {
			e.printStackTrace();

		}

		//无重试机制，重试3次，间隔1秒 不进行重试
		try {
			HashMap excutor4 = httpClient.executor((RestTemplate r) -> r.getForObject("xxxxxxxxx", HashMap.class, requestObject),
					null,
					new Retryer.RetryMeta(3, 1000), RestTemplateType.LONG);
		} catch (Exception e) {
			e.printStackTrace();

		}

		//无重试机制，无重试元数据 不进行重试
		try {
			HashMap excutor5 = httpClient.executor((RestTemplate r) -> r.getForObject("xxxxxxxxx", HashMap.class, requestObject),
					null,
					null, RestTemplateType.LONG);
		} catch (Exception e) {
			e.printStackTrace();

		}

		//needRetry为false，不进行重试
		try {
			HashMap excutor6 = httpClient.executor((RestTemplate r) -> r.getForObject("xxxxxxxxx", HashMap.class, requestObject),
					false, RestTemplateType.LONG);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//needRetry为true，按默认机制和默认元数据重试
		try {
			HashMap excutor7 = httpClient.executor((RestTemplate r) -> r.getForObject("xxxxxxxxx", HashMap.class, requestObject),
					true, RestTemplateType.LONG);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//重试机制为全部不重试，重试次数为0，间隔为2秒 不进行重试
		try {
			HashMap excutor8 = httpClient.executor((RestTemplate r) -> r.getForObject("xxxxxxxxx", HashMap.class, requestObject),
					(RetryException e) -> false,
					new Retryer.RetryMeta(0, 2000), RestTemplateType.LONG);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//重试机制为全部不重试，重试次数为3，间隔为2秒 不进行重试
		try {
			HashMap excutor9 = httpClient.executor((RestTemplate r) -> r.getForObject("xxxxxxxxx", HashMap.class, requestObject),
					(RetryException e) -> false,
					new Retryer.RetryMeta(3, 2000), RestTemplateType.LONG);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//重试机制为全部不重试，无重试元数据 不进行重试
		try {
			HashMap excutor10 = httpClient.executor(r -> r.getForObject("xxxxxxxxx", HashMap.class, requestObject),
					(RetryException e) -> false,
					null, RestTemplateType.LONG);
		} catch (Exception e) {
			e.printStackTrace();
		}


		return "ok";
	}


	@Bean
	public RestTemplate commonRestTemplate(){
		RestTemplate restTemplate = new RestTemplate();
		//验证主机名和服务器验证方案的匹配是可接受的

		restTemplate.setRequestFactory(getRequestFactory(20000,20000));
		List<ClientHttpRequestInterceptor> interceptorsTimeout = new ArrayList<ClientHttpRequestInterceptor>();
		interceptorsTimeout.add(new HeaderRequestInterceptor());
		restTemplate.setInterceptors(interceptorsTimeout);

		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
		restTemplate.getMessageConverters().set(1,stringConverter);

		return restTemplate;
	}

	@Bean
	public RestTemplate longRestTemplate(){
		RestTemplate restTemplate = new RestTemplate();
		//验证主机名和服务器验证方案的匹配是可接受的

		restTemplate.setRequestFactory(getRequestFactory(30000,30000));
		List<ClientHttpRequestInterceptor> interceptorsTimeout = new ArrayList<ClientHttpRequestInterceptor>();
		interceptorsTimeout.add(new HeaderRequestInterceptor());
		restTemplate.setInterceptors(interceptorsTimeout);

		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
		restTemplate.getMessageConverters().set(1,stringConverter);

		return restTemplate;
	}


	@Bean
	public RestTemplate shortRestTemplate(){

		RestTemplate bigDataRestTemplate = new RestTemplate();
		//验证主机名和服务器验证方案的匹配是可接受的

		bigDataRestTemplate.setRequestFactory(getRequestFactory(10000,10000));
		List<ClientHttpRequestInterceptor> interceptorsTimeout = new ArrayList<ClientHttpRequestInterceptor>();
		interceptorsTimeout.add(new HeaderRequestInterceptor());
		bigDataRestTemplate.setInterceptors(interceptorsTimeout);

		StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
		bigDataRestTemplate.getMessageConverters().set(1,stringConverter);

		return bigDataRestTemplate;
	}

	private SimpleClientHttpRequestFactory getRequestFactory(int readTimeout , int connectTimeout) {

		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
			@Override
			protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
				if (connection instanceof HttpsURLConnection) {
					((HttpsURLConnection) connection).setHostnameVerifier(PROMISCUOUS_VERIFIER);
					((HttpsURLConnection) connection).setSSLSocketFactory(trustSelfSignedSSL());
				}
				super.prepareConnection(connection, httpMethod);
			}
		};
		requestFactory.setReadTimeout(readTimeout);
		requestFactory.setConnectTimeout(connectTimeout);

		return requestFactory;
	}

	private static class HeaderRequestInterceptor implements ClientHttpRequestInterceptor {
		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body,
											ClientHttpRequestExecution execution) throws IOException {
			HttpRequest wrapper = new HttpRequestWrapper(request);
			wrapper.getHeaders().set("Accept-charset", "utf-8");
			return execution.execute(wrapper, body);
		}
	}

	public SSLSocketFactory trustSelfSignedSSL() {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[]{tm}, null);
			return ctx.getSocketFactory();
		} catch (Exception ex) {
			throw new RuntimeException("Exception occurred ", ex);
		}
	}
}
