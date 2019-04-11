package com.huawei.imagesearch.util;

import com.cloud.sdk.DefaultRequest;
import com.cloud.sdk.auth.credentials.BasicCredentials;
import com.cloud.sdk.auth.signer.Signer;
import com.cloud.sdk.auth.signer.SignerFactory;
import com.cloud.sdk.http.HttpMethodName;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * 使用AK/SK方式鉴权的包装类
 */
public class AccessServiceUtil extends  HttpClientBase
{

    private String ak;
    private String sk;
    private String serviceName;
    private String region;
    private Signer signer;
    public AccessServiceUtil(String ak, String sk, String serviceName, String region){
        this.ak = ak;
        this.sk = sk;
        this.serviceName = serviceName;
        this.region = region;
        this.signer = SignerFactory.getSigner(serviceName, region);
    }

    /**
     * 通过AkSk进行鉴权，发送请求
     * @param httpMethod 调用方法如POST/DELETE等
     * @param url 调用url
     * @param headers 调用Header
     * @param entity 请求body
     * @return
     */
    public HttpResponse executeWithAkSk(HttpMethodName httpMethod ,
            String url, Header[] headers, HttpEntity entity){
        DefaultRequest request = new DefaultRequest(serviceName);
        long contentLength = 0;
        if(null != entity){
            contentLength = entity.getContentLength();
        }
        try
        {
            request.setEndpoint(new URI(url));
            request.setHttpMethod(httpMethod);
            request.setHeaders(translate2MapHeader(headers));
            if(null != entity)
            {
                request.setContent(entity.getContent());
            }
            String parameters = null;
            if (url.contains("?")) {
                parameters = url.substring(url.indexOf("?") + 1);
                Map parametersMap = new HashMap<String, String>();

                if (null != parameters && !"".equals(parameters)) {
                    String[] parameterArray = parameters.split("&");

                    for (String p : parameterArray) {
                        String key = p.split("=")[0];
                        String value = p.split("=")[1];
                        parametersMap.put(key, value);
                    }
                    request.setParameters(parametersMap);
                }
            }
            signer.sign(request, new BasicCredentials(ak, sk));
            HttpRequestBase httpRequestBase = createRequest(new URL(url), null,
                    request.getContent(), contentLength, httpMethod);
            Map<String, String> requestHeaders = request.getHeaders();
            // Put the header of the signed request to the new request.
            for (String key : requestHeaders.keySet()) {
                if (key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                    continue;
                }
                httpRequestBase.addHeader(key, requestHeaders.get(key));
            }
            HttpClient client = acceptsUntrustedCertsHttpClient();
            return  client.execute(httpRequestBase);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Make a request that can be sent by the HTTP client.
     *
     * @param url
     *            specifies the API access path.
     * @param header
     *            specifies the header information to be added.
     * @param content
     *            specifies the body content to be sent in the API call.
     * @param contentLength
     *            specifies the length of the content. This parameter is optional.
     * @param httpMethod
     *            specifies the HTTP method to be used.
     * @return specifies the request that can be sent by an HTTP client.
     */
    private static HttpRequestBase createRequest(URL url, Header header,
            InputStream content, Long contentLength, HttpMethodName httpMethod) {

        HttpRequestBase httpRequest;
        if (httpMethod == HttpMethodName.POST) {
            HttpPost postMethod = new HttpPost(url.toString());

            if (content != null) {
                InputStreamEntity entity = new InputStreamEntity(content,
                        contentLength);
                postMethod.setEntity(entity);
            }
            httpRequest = postMethod;
        } else if (httpMethod == HttpMethodName.PUT) {
            HttpPut putMethod = new HttpPut(url.toString());
            httpRequest = putMethod;

            if (content != null) {
                InputStreamEntity entity = new InputStreamEntity(content,
                        contentLength);
                putMethod.setEntity(entity);
            }
        } else if (httpMethod == HttpMethodName.PATCH) {
            HttpPatch patchMethod = new HttpPatch(url.toString());
            httpRequest = patchMethod;

            if (content != null) {
                InputStreamEntity entity = new InputStreamEntity(content,
                        contentLength);
                patchMethod.setEntity(entity);
            }
        } else if (httpMethod == HttpMethodName.GET) {
            httpRequest = new HttpGet(url.toString());
        } else if (httpMethod == HttpMethodName.DELETE) {
            HttpDeleteWithBody httpDelete = new HttpDeleteWithBody(url.toString());
            if(null != content){
                InputStreamEntity entity = new InputStreamEntity(content,
                        contentLength);
                httpDelete.setEntity(entity);
            }
            httpRequest = httpDelete;
        } else if (httpMethod == HttpMethodName.HEAD) {
            httpRequest = new HttpHead(url.toString());
        } else {
            throw new RuntimeException("Unknown HTTP method name: "
                    + httpMethod);
        }

        httpRequest.addHeader(header);
        return httpRequest;
    }


    private static Map<String, String> translate2MapHeader(Header[] headers){
        HashMap<String, String> newHeaders = new HashMap<>();
        for(Header header : headers){
            newHeaders.put(header.getName(), header.getValue());
        }
        return newHeaders;
    }

    /**
     * 解析response中的返回body数据，如果返回null，则代表接口调用出现问题
     * @param httpMethod 调用方法如POST/DELETE等
     * @param url 调用url
     * @param headers 调用Header
     * @param entity 请求body
     * @return body数据，如果返回null，则代表接口调用出现问题
     */
    public String executeWithAkSkResultString(HttpMethodName httpMethod ,
            String url, Header[] headers, HttpEntity entity){
        HttpResponse response = executeWithAkSk(httpMethod, url, headers, entity);
        if(null == response){
            return null;
        }else{
            try
            {
                return EntityUtils.toString(response.getEntity());
            }catch (IOException e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public String getAk()
    {
        return ak;
    }

    public String getSk()
    {
        return sk;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    public String getRegion()
    {
        return region;
    }

    public Signer getSigner()
    {
        return signer;
    }
}
