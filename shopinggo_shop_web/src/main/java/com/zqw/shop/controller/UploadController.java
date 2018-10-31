package com.zqw.shop.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import utils.FastDFSClient;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String file_server_url;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file){

        try {
            FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");

            String originalFilename = file.getOriginalFilename();   //获取文件全名称
            //获取文件后缀名
            String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

            //上传
            String fileId = client.uploadFile(file.getBytes(), extName);

            String url = file_server_url + fileId;  //图片的完整地址
            System.out.println(url);
            return new Result(true, url);

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, "上传失败");
        }
    }

    @RequestMapping("/deleteImage")
    public Result deleteImage(String url){

        try {
            //url: http://192.168.236.135/group1/M00/00/03/wKjsh1siWlOAdBObAABzcHMTfgg401.jpg
            String fileId = url.replace(file_server_url, "");

            FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf");

            int i = client.deleteFile(fileId);
            if(i==0){
                return new Result(true, "删除成功");
            }else{
                return new Result(false, "删除失败");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true, "删除失败");
        }
    }
}
