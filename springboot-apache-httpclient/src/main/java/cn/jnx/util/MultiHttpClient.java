package cn.jnx.util;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;

import cn.jnx.bean.HttpResp;

/**
 * 
 * @ClassName: MultiHttpClient
 * @Description 多线程测试实例
 * @version
 * @author JH
 * @date 2020年7月17日 上午9:31:57
 */
public class MultiHttpClient {

    public static void main(String[] args) throws ClientProtocolException, IOException {
        List<NameValuePair> valuePairs = new LinkedList<NameValuePair>();
        valuePairs.add(new BasicNameValuePair("username", "admin"));
        valuePairs.add(new BasicNameValuePair("password", "123456"));
        //登录
        post("http://192.168.0.69/login", valuePairs);
        MyThread mt = new MultiHttpClient().new MyThread();
        for(int i =0;i<2;i++) {
            Thread t = new Thread(mt,i+"号");
            t.start();
        }
    }

    private static CloseableHttpClient httpClient = null;
    static {
        // 创建一个线程安全的连接管理
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(100);
        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        // 创建cookie store的本地实例
        CookieStore cookieStore = new BasicCookieStore();
        // 创建HttpClient上下文
        HttpClientContext context = HttpClientContext.create();
        context.setCookieStore(cookieStore);
        // 创建一个HttpClient
        httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig).setDefaultCookieStore(cookieStore)
                .setConnectionManager(cm).build();
    }

    public CloseableHttpResponse get(String url) throws ClientProtocolException, IOException {
        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();
        // 创建cookie store的本地实例
        CookieStore cookieStore = new BasicCookieStore();
        // 创建HttpClient上下文
        HttpClientContext context = HttpClientContext.create();

        // 创建一个HttpClient
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(globalConfig)
                .setDefaultCookieStore(cookieStore).build();
        context.setCookieStore(cookieStore);
        CloseableHttpResponse res = null;

        HttpGet get = new HttpGet(url);
        res = httpClient.execute(get, context);
        res.close();
        return res;
    }

    /**
     * 简单post接口调用
     * 
     * @param url        访问接口地址
     * @param valuePairs 键值对参数
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
    public static HttpResp post(String url, List<NameValuePair> valuePairs)
            throws ClientProtocolException, IOException {
        CloseableHttpResponse res = null;

        HttpPost post = new HttpPost(url);
        if (null != valuePairs) {
            // 注入post数据
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(valuePairs, Consts.UTF_8);
            entity.setContentType("application/x-www-form-urlencoded");
            post.setEntity(entity);
        }

        res = httpClient.execute(post);
        HttpResp resp = HttpResp.convertResp(res);
        return resp;
    }

    class MyThread implements Runnable {
        @Override
        public void run() {
            for (int n = 0; n <= 50; n++) {
                System.out.println("当前运行线程：" + Thread.currentThread().getName());
                try {
                    post("http://192.168.0.69/login", null);
                } catch (IOException e) {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

}
