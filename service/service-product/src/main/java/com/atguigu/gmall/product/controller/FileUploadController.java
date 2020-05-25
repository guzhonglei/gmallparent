package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.baomidou.mybatisplus.extension.api.R;
import io.swagger.annotations.Api;
import jodd.io.FileNameUtil;
import org.apache.commons.io.FilenameUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Api(tags = "图片上传接口")
@RestController
@RequestMapping("admin/product")
public class FileUploadController {

    //文件上传,返回文件地址
    @Value("${fileServer.url}")
    private String fileUrl;


    @RequestMapping("fileUpload")
    public Result fileUpload(MultipartFile file) throws IOException, MyException {

        String configFile = this.getClass().getResource("/tracker.conf").getFile();

        String path = null;
        if(configFile != null){
            //初始化文件
            ClientGlobal.init(configFile);
            //文件上传 需要tracker，storage
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = trackerClient.getConnection();

            StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
            //上传文件
            path = storageClient1.upload_appender_file1(file.getBytes(), FilenameUtils.getExtension(file.getOriginalFilename()), null);

            System.out.println("图片路径:" + fileUrl + path);

        }
        return Result.ok(fileUrl+path);
    }

}
