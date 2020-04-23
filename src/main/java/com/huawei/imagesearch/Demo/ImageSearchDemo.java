package com.huawei.imagesearch.Demo;

import com.huawei.imagesearch.ImageSearchService;
import com.huawei.imagesearch.TokenUtil;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

public class ImageSearchDemo
{
    /**
     * Demo代码，简介ImageSearch使用功能
     */
    public static void main(String[] args) throws URISyntaxException, UnsupportedOperationException, IOException
    {
        String domainName = "***";      //账户为主账号，此处输入与用户名一致，若为子账号，此处输入为主账户用户名
        String username = "zhangsan";    // 此处，请输入用户名
        String password = "***";      // 此处，请输入对应用户名的密码
        String projectName = "cn-north-1"; // 此处，请输入服务的区域信息，参考地址: http://developer.huaweicloud.com/dev/endpoint

        String projectId = "projectId"; //从我的凭证，项目列表中获取
        String instanceName = "test-instance";//实例名称
        String region = "cn-north-1"; // 此处，请输入服务的区域信息，参考地址: http://developer.huaweicloud.com/dev/endpoint
        String service = "cn-north-1a"; // 此处，请输入可用分区名称，参考地址: http://developer.huaweicloud.com/dev/endpoint
        String ak = "****"; //ak,sk从我的凭证，管理访问秘钥中获取
        String sk = "****";
        //使用ak/sk进行鉴权，推荐使用此方式
        ImageSearchService imageSearchService = new ImageSearchService(service, region, ak, sk);

        //使用用户名密码获取token，后续使用token进行鉴权
        String token = TokenUtil.getToken(username, password, domainName, projectName); //获取token
        System.out.println(token);
        //使用token进行鉴权的方式
        ImageSearchService imageSearchService2 = new ImageSearchService(token, projectName);


        //根据用户名和模型创建实例,注：图片自定义标签的命名不支持字母大写，可参考tag1此类命名
        ArrayList<String> createServiceTag = new ArrayList<>();
        createServiceTag.add("tag1");
        createServiceTag.add("tag2");
        // 创建实例的图片数量规格目前仅支持30000000 model参数当前common-search、image-recommend、image-copyright、furniture-search(仅支持国内站)
        printResult(imageSearchService.requestCreateService(projectId, instanceName, "image-copyright",
                "register service for user model test.",
                createServiceTag,
                30000000));
        //添加图像索引，使用图像的Base64
        HashMap<String, String> createIndexTags = new HashMap<>();
        createIndexTags.put("tag1","test-image");
        printResult(imageSearchService.requestCreateIndexBase64(projectId, instanceName, "data/is-demo-1.jpg",
                "data/is-demo-1.jpg",createIndexTags));

        //添加图像索引，使用图像的URL(当前仅支持OBS图像的URL)
        printResult(imageSearchService.requestCreateIndexUrl(projectId, instanceName,
                "https://bucketName.obs.myhwclouds.com/image/test1.jpg", createIndexTags));


        //搜索相似图像，使用图像的Base64
        printResult(imageSearchService.requestSearchSimBase64(projectId, instanceName, "data/is-demo-2.jpg",
                10, 0, null));

        //搜索相似图像，使用图像的URL(当前仅支持OBS图像的URL)
        printResult(imageSearchService.requestSearchSimUrl(projectId, instanceName, "https://bucketName.obs.myhwclouds.com/image/test2.jpg",
                10, 0 , null));

        //搜索相似图像,使用图像标签
        printResult(imageSearchService.requestSearchSimTags(projectId, instanceName, 10, 0,createIndexTags));


       //查询图像索引是否存在
        printResult(imageSearchService.requestImageCheck(projectId, instanceName, "https://bucketName.obs.myhwclouds.com/image/test1.jpg"));

        //通过图片路径删除索引库中对应图片
        printResult(imageSearchService.requestDeleteIndex(projectId, instanceName, "https://bucketName.obs.myhwclouds.com/image/test1.jpg"));

        //修改图像索引库中已存在的图片信息
        printResult("修改" + imageSearchService.requestUpdateTags(projectId, instanceName, "https://bucketName.obs.myhwclouds.com/image/test1.jpg", createIndexTags));

        // 查询用户实例信息
        printResult(imageSearchService.requestGetServiceInfo(projectId, instanceName));

        //删除实例
        printResult(imageSearchService.requestDeleteService(projectId, instanceName));
    }


    private static void printResult(String result){
        if(null == result){
            System.out.println("调用服务接口失败");
        }else{
            System.out.println(result);
        }
    }
}
