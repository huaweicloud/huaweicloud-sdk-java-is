package com.huawei.imagesearch;

import com.alibaba.fastjson.JSONObject;
import com.cloud.sdk.http.HttpMethodName;
import com.huawei.imagesearch.util.AccessServiceUtil;
import com.huawei.imagesearch.util.HttpClientUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 调用图像搜索的工具类，对相关参数和网络请求进行了封装
 */
public class ImageSearchService
{

    private String token;
    private String ak;
    private String sk;
    private boolean useToken = true;
    private AccessServiceUtil accessServiceUtil = null;
    private String endPointUrl = "https://imagesearch.cn-north-1.myhuaweicloud.com";

    /**
     * 传入用户token，使用此构造函数，则后续请求全都是用token方式鉴权
     * @param token
     */
    public ImageSearchService(String token){
        this.useToken = true;
        this.token = token;
    }

    /**
     * 传入用户token，使用此构造函数，则后续请求全都是用token方式鉴权
     * @param token token认证串
     * @param endPointUrl endPoint地址
     */
    public ImageSearchService(String token, String endPointUrl){
        this.useToken = true;
        this.token = token;
        this.endPointUrl = endPointUrl;
    }

    /**
     * 使用此构造函数，后续请求都是用ak/sk鉴权,指定endPoint地址
     * @param serviceName
     * @param region
     * @param ak ak串，在我的凭证页面中
     * @param sk sk串，在我的凭证页面中
     * @param endPointUrl endPoint地址
     */
    public ImageSearchService(String serviceName, String region, String ak, String sk, String endPointUrl){
        this.useToken = false;
        this.ak = ak;
        this.sk = sk;
        this.endPointUrl = endPointUrl;
        this.accessServiceUtil = new AccessServiceUtil(ak, sk, serviceName, region);
    }

    /**
     * 使用此方式，后续请求都是用ak/sk鉴权
     * @param serviceName
     * @param region
     * @param ak ak串，在我的凭证页面中
     * @param sk sk串，在我的凭证页面中
     */
    public ImageSearchService(String serviceName, String region, String ak, String sk){
        this.useToken = false;
        this.ak = ak;
        this.sk = sk;
        this.accessServiceUtil = new AccessServiceUtil(ak, sk, serviceName, region);
    }


    private static final String REGISTER_SERVICE_URL = "%s/v1/%s/service";
    /**
     * 指定用户名和模型创建实例，实例中会生成图片索引库，用来存放图片
     * @param projectId 项目projectID
     * @param name 服务名称
     * @param model 模型名称 如（image-copyright/image-recommend）
     * @param description 描述信息
     * @param level 实例的图片数量规格，默认为1000000,目前支持：1000000,5000000,10000000,50000000,100000000
     * @param tags 图片标签，最多支持10个标签
     * @return 调用接口的返回值,如果返回null，则说明调用失败
     */
    public String requestCreateService(String projectId,
            String name, String model, String description, List<String> tags, Integer level){
        String url = String.format(REGISTER_SERVICE_URL, this.endPointUrl, projectId);
        //请求体
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("model", model);
        json.put("description", description);
        json.put("tags", tags);
        json.put("level",level);
        String requestBody = json.toJSONString();
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");

        if(this.isUseToken())
        {
            Header[] headers = new Header[] { new BasicHeader("X-Auth-Token", this.token),
                    new BasicHeader("Content-Type", "application/json") };
            return HttpClientUtils.getHttpPostResult(url, headers, stringEntity);
        }else{
            Header[] headers = new Header[]{new BasicHeader("Content-Type", "application/json")};
            return accessServiceUtil.executeWithAkSkResultString(HttpMethodName.POST, url, headers, stringEntity);
        }

    }

    private static final String CREATE_INDEX_URL = "%s/v1/%s/%s/image";
    /**
     * 创建图像索引,使用Base64编码后的文件方式，使用Token认证方式访问服务；</br>
     * 支持JPG/PNG/BMP格式
     * @param projectId 项目projectID
     * @param instanceName 服务名称
     * @param path  图片OBS地址，可以是华为云OBS地址或者外网S3地址，当时外网S3地址的时候，必须同时传入文件流
     * @param file  本地图像的路径,用于生成Base64图像对象
     * @param tags 图片标签
     * @return 调用接口的返回值,如果返回null，则说明调用失败
     * @throws IOException 文件格式转化异常时，抛出此异常
     */
    public String requestCreateIndexBase64(String projectId, String instanceName,
            String path, String file, HashMap<String, String> tags)throws IOException{
        String url = String.format(CREATE_INDEX_URL, this.endPointUrl, projectId, instanceName);

        //请求体
        JSONObject json = new JSONObject();
        json.put("path", path);
        json.put("file", toBase64Str(file));
        json.put("tags", tags);
        String requestBody = json.toJSONString();
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");
        if(this.isUseToken())
        {
            Header[] headers = new Header[]{new BasicHeader("X-Auth-Token", token),
                    new BasicHeader("Content-Type", "application/json")};
            return HttpClientUtils.getHttpPostResult(url, headers, stringEntity);
        }else{
            Header[] headers = new Header[]{new BasicHeader("Content-Type", "application/json")};
            return accessServiceUtil.executeWithAkSkResultString(HttpMethodName.POST, url, headers, stringEntity);
        }
    }

    /**
     * 创建图像索引，使用图像URL(当前仅支持OBS路径)，使用Token认证方式访问服务
     * @param projectId 项目ID
     * @param instanceName 用户实例名称
     * @param path 此时path作为下载图片的地址（当前仅支持从华为云图像搜索服务所在区域的OBS下载图片），同时，path也作为图片索引ID
     * @param tags 图片标签项，最多不超过10个，需要自定义标签名，格式为key：value对
     * @return 调用接口的返回值,如果返回null，则说明调用失败
     */
    public String requestCreateIndexUrl(String projectId, String instanceName,
            String path , HashMap<String, String> tags){
        String url = String.format(CREATE_INDEX_URL, this.endPointUrl, projectId, instanceName);
        //请求体
        JSONObject json = new JSONObject();
        json.put("path", path);
        json.put("tags", tags);
        String requestBody = json.toJSONString();
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");
        if(this.isUseToken())
        {
            Header[] headers = new Header[]{new BasicHeader("X-Auth-Token", token),
                    new BasicHeader("Content-Type", "application/json")};
            return HttpClientUtils.getHttpPostResult(url, headers, stringEntity);
        }else{
            Header[] headers = new Header[]{new BasicHeader("Content-Type", "application/json")};
            return accessServiceUtil.executeWithAkSkResultString(HttpMethodName.POST, url, headers, stringEntity);
        }
    }

    private static final String IMAGR_SEARCH_URL = "%s/v1/%s/%s/image/search";
    /**
     * 搜索相似图像，使用Base64编码后的文件方式，使用Token认证方式访问服务；</br>
     * 支持JPG/PNG/BMP格式
     * @param projectId 项目ID
     * @param instanceName 用户实例名称
     * @param file 本地图像的路径,用于生成Base64图像对象
     * @param limit 返回查询结果的前多少个, 默认返回前10个
     * @param offset 查询结果偏移量,如offset=5，则代表从查询结果的第6个开始返回。
     * @param tags 搜索标签,如果不需要标签，则传null
     * @return 调用接口的返回值,如果返回null，则说明调用失败
     * @throws IOException 文件格式转化异常时，抛出此异常
     */
    public String requestSearchSimBase64(String projectId, String instanceName,
            String file, Integer limit, Integer offset, HashMap<String, String> tags)
            throws IOException{
        String url = String.format(IMAGR_SEARCH_URL, this.endPointUrl, projectId, instanceName);
        //请求体
        JSONObject json = new JSONObject();
        json.put("file", toBase64Str(file));
        json.put("limit", limit);
        json.put("offset", offset);
        if(null == tags || 0 == tags.size())
        {
            json.put("tags", tags);
        }
        String requestBody = json.toJSONString();
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");

        if(this.useToken)
        {
            Header[] headers = new Header[]{new BasicHeader("X-Auth-Token", token),
                    new BasicHeader("Content-Type", "application/json")};
            return HttpClientUtils.getHttpPostResult(url, headers, stringEntity);
        }else{
            Header[] headers = new Header[]{new BasicHeader("Content-Type", "application/json")};
            return accessServiceUtil.executeWithAkSkResultString(HttpMethodName.POST, url, headers, stringEntity);
        }
    }

    /**
     * 搜索相似图像，使用图像URL，使用Token认证方式访问服务
     * @param projectId 项目ID
     * @param instanceName 用户实例名称
     * @param path  图像的URL路径，用于图片下载和索引路径，当前仅支持OBS路径
     * @param limit 返回被检索图像的数量，默认10
     * @param offset 偏移量，指定搜索结果返回起始位置，默认0
     * @param tags 搜索标签,如果不需要标签，则传null
     * @return 调用接口的返回值,如果返回null，则说明调用失败
     */
    public String requestSearchSimUrl(String projectId, String instanceName,
            String path, Integer limit, Integer offset, HashMap<String, String> tags){
        String url = String.format(IMAGR_SEARCH_URL, this.endPointUrl, projectId, instanceName);
        //请求体
        JSONObject json = new JSONObject();
        json.put("path",path);
        json.put("limit", limit);
        json.put("offset", offset);
        if(null == tags || 0 == tags.size())
        {
            json.put("tags", tags);
        }
        String requestBody = json.toJSONString();
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");
        if(this.isUseToken())
        {
            Header[] headers = new Header[]{new BasicHeader("X-Auth-Token", token),
                    new BasicHeader("Content-Type", "application/json")};
            return HttpClientUtils.getHttpPostResult(url, headers, stringEntity);
        }else{
            Header[] headers = new Header[]{new BasicHeader("Content-Type", "application/json")};
            return accessServiceUtil.executeWithAkSkResultString(HttpMethodName.POST, url, headers, stringEntity);
        }
    }

    /**
     * 搜索相似图像，使用图片标签搜索，使用Token认证方式访问服务
     * @param projectId 项目ID
     * @param instanceName 用户实例名称
     * @param limit 返回被检索图像的数量，默认10
     * @param offset 偏移量，指定搜索结果返回起始位置，默认0
     * @param tags 搜索标签
     * @return 调用接口的返回值,如果返回null，则说明调用失败
     */
    public String requestSearchSimTags(String projectId, String instanceName,
            Integer limit, Integer offset, HashMap<String, String> tags){
        String url = String.format(IMAGR_SEARCH_URL, this.endPointUrl, projectId, instanceName);
        //请求体
        JSONObject json = new JSONObject();
        json.put("limit", limit);
        json.put("offset", offset);
        json.put("tags", tags);
        String requestBody = json.toJSONString();
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");
        if(this.isUseToken())
        {
            Header[] headers = new Header[]{new BasicHeader("X-Auth-Token", token),
                    new BasicHeader("Content-Type", "application/json")};
            return HttpClientUtils.getHttpPostResult(url, headers, stringEntity);
        }else{
            Header[] headers = new Header[]{new BasicHeader("Content-Type", "application/json")};
            return accessServiceUtil.executeWithAkSkResultString(HttpMethodName.POST, url, headers, stringEntity);
        }
    }



    private static final String CROSS_SEARCH_PATH = "%s/v1/%s/image/cross-search";
    /**
     * 给定源实例中多个图片路径，到目标实例中搜索相同或相似图片。目前仅图库模型支持该接口，其他模型暂不支持
     * @param projectId  项目ID
     * @param limit 返回被检索图片的数量，默认10，取值范围为大于或等于0的整数
     * @param offset  偏移量，从第一条数据偏移offset条数据后开始查询，默认为0，取值范围为大于或等于0的整数
     * @param paths 查询路径列表，一次查询最多支持10个
     * @param sourceInstance 源实例，用于获取paths字段中路径对应的图片，作为查询图片，到target_instance中查询相同或相似的图片，仅支持一个源实例
     * @param targetInstance 目标实例，根据从source_instance中获取的查询图片，在此实例中查询相同或相似的图片，仅支持一个目标实例。
     * @return 调用接口的返回值,如果返回null，则说明调用失败
     */
    public String requestCrossSearch(String projectId, Integer limit, Integer offset, ArrayList<String> paths,
            String sourceInstance, String targetInstance){
        String url = String.format(CROSS_SEARCH_PATH, this.endPointUrl, projectId);
        //请求体
        JSONObject json = new JSONObject();
        json.put("limit",limit);
        json.put("offset",offset);
        json.put("paths", paths);
        json.put("source_instance", sourceInstance);
        json.put("target_instance", targetInstance);
        String requestBody = json.toJSONString();
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");
        if(this.isUseToken())
        {
            Header[] headers = new Header[]{new BasicHeader("X-Auth-Token", token),
                    new BasicHeader("Content-Type", "application/json")};
            return HttpClientUtils.getHttpPostResult(url, headers, stringEntity);
        }else{
            Header[] headers = new Header[]{new BasicHeader("Content-Type", "application/json")};
            return accessServiceUtil.executeWithAkSkResultString(HttpMethodName.POST, url, headers, stringEntity);
        }
    }

    private static final String IMAGR_CHECK_URL = "%s/v1/%s/%s/image/check";
    /**
     * 查询图像，使用图像URL，使用Token认证方式访问服务
     * @param projectId 项目ID
     * @param instanceName 用户实例名称
     * @param path 图片的URL路径，与添加图片的路径一致
     * @return 调用接口的返回值,如果返回null，则说明调用失败
     */
    public String requestImageCheck(String projectId, String instanceName,
            String path){
        String url = String.format(IMAGR_CHECK_URL, this.endPointUrl, projectId, instanceName);

        //请求体
        JSONObject json = new JSONObject();
        json.put("path", path);
        String requestBody = json.toJSONString();
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");
        if(this.isUseToken())
        {
            Header[] headers = new Header[]{new BasicHeader("X-Auth-Token", token),
                    new BasicHeader("Content-Type", "application/json")};
            return HttpClientUtils.getHttpPostResult(url, headers, stringEntity);
        }else{
            Header[] headers = new Header[]{new BasicHeader("Content-Type", "application/json")};
            return accessServiceUtil.executeWithAkSkResultString(HttpMethodName.POST, url, headers, stringEntity);
        }
    }


    private static final String DELETE_INDEX_URL = "%s/v1/%s/%s/image";
    /**
     * 删除图像索引，使用图像URL，使用Token认证方式访问服务
     * @param projectId  项目ID
     * @param instanceName 用户实例名称
     * @param path 图像的URL路径，与添加图片的路径一致
     * @return  调用接口的返回值,如果返回null，则说明调用失败
     */
    public String requestDeleteIndex(String projectId, String instanceName,
            String path){
        String url = String.format(DELETE_INDEX_URL, this.endPointUrl, projectId, instanceName);
        //请求体
        JSONObject json = new JSONObject();
        json.put("path", path);
        String requestBody = json.toJSONString();
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");

        if(this.isUseToken())
        {
            Header[] headers = new Header[]{new BasicHeader("X-Auth-Token", token),
                    new BasicHeader("Content-Type", "application/json")};
            return HttpClientUtils.getHttpDeleteResult(url, headers, stringEntity);
        }else{
            Header[] headers = new Header[]{new BasicHeader("Content-Type", "application/json")};
            return accessServiceUtil.executeWithAkSkResultString(HttpMethodName.DELETE, url, headers, stringEntity);
        }
    }


    private static final String UPDATE_TAG_URL = "%s/v1/%s/%s/image";
    /**
     * 修改图像索引库中已存在的图片信息
     * @param projectId 项目projectID
     * @param instanceName 服务名称
     * @param path 图片OBS地址，可以是华为云OBS地址或者外网S3地址，当时外网S3地址的时候，必须同时传入文件流
     * @param tags 搜索标签
     * @return 调用接口的返回值,如果返回null，则说明调用失败
     */
    public String requestUpdateTags(String projectId, String instanceName,
            String path, HashMap<String, String> tags){
        String url = String.format(UPDATE_TAG_URL, this.endPointUrl, projectId, instanceName);
        //请求体
        JSONObject json = new JSONObject();
        json.put("path", path);
        json.put("tags", tags);
        String requestBody = json.toJSONString();
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");

        if(this.isUseToken())
        {
            Header[] headers = new Header[]{new BasicHeader("X-Auth-Token", token),
                    new BasicHeader("Content-Type", "application/json")};
            return HttpClientUtils.getHttpPutResult(url, headers, stringEntity);
        }else{
            Header[] headers = new Header[]{new BasicHeader("Content-Type", "application/json")};
            return accessServiceUtil.executeWithAkSkResultString(HttpMethodName.PUT, url, headers, stringEntity);
        }
    }

    private static final String DELETE_SERVICE_URL = "%s/v1/%s/service/%s";
    /**
     * 删除已存在的实例
     * @param projectId 项目ID
     * @param instanceName 用户实例名称
     * @return 调用接口的返回值,如果返回null，则说明调用失败
     */
    public String requestDeleteService(String projectId, String instanceName){
        String url = String.format(DELETE_SERVICE_URL, this.endPointUrl, projectId, instanceName);
        //请求体
        JSONObject json = new JSONObject();
        String requestBody = json.toJSONString();
        StringEntity stringEntity = new StringEntity(requestBody, "utf-8");


        if(this.isUseToken())
        {
            Header[] headers = new Header[]{new BasicHeader("X-Auth-Token", token),
                    new BasicHeader("Content-Type", "application/json")};
            return HttpClientUtils.getHttpDeleteResult(url, headers, stringEntity);
        }else{
            Header[] headers = new Header[]{new BasicHeader("Content-Type", "application/json")};
            return accessServiceUtil.executeWithAkSkResultString(HttpMethodName.DELETE, url, headers, null);
        }
    }


    private static final String GET_SERVICE_INFO_URL = "%s/v1/%s/service/%s";
    /**
     * 获取用户指定服务详细信息
     * @param projectId 项目ID
     * @param instanceName 用户实例名称
     * @return 调用接口的返回值,如果返回null，则说明调用失败
     */
    public String requestGetServiceInfo(String projectId, String instanceName){
        String url = String.format(GET_SERVICE_INFO_URL, this.endPointUrl, projectId, instanceName);
        if(this.isUseToken())
        {
            Header[] headers = new Header[]{new BasicHeader("X-Auth-Token", token),
                    new BasicHeader("Content-Type", "application/json")};
            return HttpClientUtils.getHttpGetResult(url, headers);
        }else{
            Header[] headers = new Header[]{new BasicHeader("Content-Type", "application/json")};
            return accessServiceUtil.executeWithAkSkResultString(HttpMethodName.GET, url, headers, null);
        }
    }


    /**
     * 将二进制文件转为经Base64编码之后的对象
     *
     * @param file 文件名
     * @return 被Base64编码之后的对象
     * @throws IOException 文件格式转化异常时，抛出此异常
     */
    public static String toBase64Str(String file) throws IOException {
        byte[] fileData = FileUtils.readFileToByteArray(new File(file));
        return Base64.encodeBase64String(fileData);
    }

    public String getToken()
    {
        return token;
    }

    public void setToken(String token)
    {
        this.token = token;
    }

    public String getAk()
    {
        return ak;
    }

    public String getSk()
    {
        return sk;
    }

    public boolean isUseToken()
    {
        return useToken;
    }

}
