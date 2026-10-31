package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Tag(name = "通用接口")
@Slf4j
public class CommonController {

    /**
     * 获取本地存储目录：项目根目录下的 Photos 文件夹
     */
    private File getUploadDir() {
        File workDir = new File(System.getProperty("user.dir"));
        //IDEA 默认在 sky-server 模块目录下启动，此时 Photos 位于上一级的项目根目录
        if (workDir.getName().equals("sky-server")) {
            workDir = workDir.getParentFile();
        }
        return new File(workDir, "Photos");
    }

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        log.info("文件上传：{}", file.getOriginalFilename());

        File dir = getUploadDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        //使用 UUID 生成唯一文件名，避免同名文件被覆盖，保留原始扩展名
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID() + suffix;

        try {
            file.transferTo(new File(dir, fileName));
            log.info("文件保存成功：{}", new File(dir, fileName).getAbsolutePath());
            return Result.success(fileName);
        } catch (IOException e) {
            log.error("文件上传失败：{}", e);
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}
