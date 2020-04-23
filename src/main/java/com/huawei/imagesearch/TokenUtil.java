package com.huawei.imagesearch;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.huawei.imagesearch.util.HttpClientUtils;

public class TokenUtil
{

    /**
     * 构造使用Token方式访问服务的请求Token对象
     *
     * @param username    用户名
     * @param passwd      密码
     * @param domainName  域名
     * @param projectName 项目名称
     * @return 构造访问的JSON对象
     */
    private static String requestBody(String username, String passwd, String domainName, String projectName) {
        JSONObject auth = new JSONObject();

        JSONObject identity = new JSONObject();

        JSONArray methods = new JSONArray();
        methods.add("password");
        identity.put("methods", methods);

        JSONObject password = new JSONObject();

        JSONObject user = new JSONObject();
        user.put("name", username);
        user.put("password", passwd);

        JSONObject domain = new JSONObject();
        domain.put("name", domainName);
        user.put("domain", domain);

        password.put("user", user);

        identity.put("password", password);

        JSONObject scope = new JSONObject();

        JSONObject scopeProject = new JSONObject();
        scopeProject.put("name", projectName);

        scope.put("project", scopeProject);

        auth.put("identity", identity);
        auth.put("scope", scope);

        JSONObject params = new JSONObject();
        params.put("auth", auth);
        return params.toJSONString();
    }

    /**
     * 获取Token参数， 注意，此函数的目的，主要为了从HTTP请求返回体中的Header中提取出Token
     * 参数名为: X-Subject-Token
     *
     * @param username    用户名
     * @param password    密码
     * @param projectName 区域名，可以参考http://developer.huaweicloud.com/dev/endpoint
     * @return 包含Token串的返回体，
     * @throws URISyntaxException
     * @throws UnsupportedOperationException
     * @throws IOException
     */
    public static String getToken(String username, String password, String domainName, String projectName)
            throws URISyntaxException, UnsupportedOperationException, IOException {
        String requestBody = requestBody(username, password, domainName, projectName);
        String url = "https://iam.myhuaweicloud.com/v3/auth/tokens";

        Header[] headers = new Header[]{new BasicHeader("Content-Type", ContentType.APPLICATION_JSON.toString())};
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");
        HttpResponse response = HttpClientUtils.post(url, headers, stringEntity);
//        System.out.println(response.getEntity().getContent());
        Header[] xst = response.getHeaders("x-subject-token");
        return xst[0].getValue();

    }
}
