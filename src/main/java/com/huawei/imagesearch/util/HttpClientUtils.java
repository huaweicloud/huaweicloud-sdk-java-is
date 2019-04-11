package com.huawei.imagesearch.util;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

public class HttpClientUtils extends  HttpClientBase{

    public static HttpResponse post(String url, Header[] headers, HttpEntity entity) {
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = acceptsUntrustedCertsHttpClient();
            HttpPost post = new HttpPost(url);
            if (null != headers) {
                post.setHeaders(headers);
            }
            if (null != entity) {
                post.setEntity(entity);
            }
            response = httpClient.execute(post);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }

    public static HttpResponse put(String url, Header[] headers, HttpEntity entity) {
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = acceptsUntrustedCertsHttpClient();
            HttpPut put = new HttpPut(url);
            if (null != headers) {
                put.setHeaders(headers);
            }
            if (null != entity) {
                put.setEntity(entity);
            }
            response = httpClient.execute(put);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }

    public static HttpResponse deleteWithBody(String url, Header[] headers, HttpEntity entity) {
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = acceptsUntrustedCertsHttpClient();
            HttpDeleteWithBody delete = new HttpDeleteWithBody(url);

            if (null != headers) {
                delete.setHeaders(headers);
            }
            if (null != entity) {
                delete.setEntity(entity);
            }
            response = httpClient.execute(delete);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }


    public static HttpResponse get(String url, Header[] headers){
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {

            httpClient = acceptsUntrustedCertsHttpClient();
            HttpGet get = new HttpGet(url);
            if (null != headers) {
                get.setHeaders(headers);
            }
            response = httpClient.execute(get);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
        return response;
    }

    /**
     * 对post请求进行封装
     * @param url  地址
     * @param headers header头
     * @param stringEntity body体
     * @return 调用结果
     */
    public static String getHttpPostResult(String url, Header[] headers, StringEntity stringEntity){
        try
        {
            HttpResponse response = HttpClientUtils.post(url, headers, stringEntity);
            return EntityUtils.toString(response.getEntity());
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对delete请求进行封装
     * @param url 地址
     * @param headers header头
     * @param stringEntity body体
     * @return 调用结果
     */
    public static String getHttpDeleteResult(String url, Header[] headers, StringEntity stringEntity){
        try {
            HttpResponse response = HttpClientUtils.deleteWithBody(url, headers, stringEntity);
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对Get方法进行封装
     * @param url 地址
     * @param headers header头
     * @return 调用结果
     */
    public static String getHttpGetResult(String url, Header[] headers){
        try {
            HttpResponse response = HttpClientUtils.get(url, headers);
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 对put方法进行封装
     * @param url 地址
     * @param headers header头
     * @param stringEntity body体
     * @return 调用结果
     */
    public static String getHttpPutResult(String url, Header[] headers, StringEntity stringEntity){
        try {
            HttpResponse response = HttpClientUtils.put(url, headers, stringEntity);
            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
