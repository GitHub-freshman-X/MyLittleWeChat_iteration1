package com.easychat.controller;


import com.easychat.annotation.GlobalInterceptor;
import com.easychat.entity.config.Appconfig;
import com.easychat.entity.constants.Constants;
import com.easychat.entity.dto.SysSettingDto;
import com.easychat.entity.query.GroupInfoQuery;
import com.easychat.entity.vo.PaginationResultVO;
import com.easychat.entity.vo.ResponseVO;
import com.easychat.redis.RedisComponent;
import com.easychat.service.GroupInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@RestController("AdminSettingController")
@RequestMapping("/admin")
public class AdminSettingController extends ABaseController {
    @Resource
    private RedisComponent redisComponent;
    @Autowired
    private Appconfig appconfig;

    @RequestMapping("/getSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO getSysSetting (){
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        return getSuccessResponseVO(sysSettingDto);
    }

    @RequestMapping("/saveSysSetting")
    @GlobalInterceptor(checkAdmin = true)
    public ResponseVO saveSysSetting (SysSettingDto sysSettingDto,
                                      MultipartFile robotFile,
                                      MultipartFile robotCover) throws IOException {
        if(robotFile != null){
            String baseFolder = appconfig.getProjectFolder()+ Constants.FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFolder+Constants.FILE_FOLDER_AVATAR_NAME);
            if(!targetFileFolder.exists()){
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath()+"/"+Constants.ROBOT_UID+Constants.IMAGE_SUFFIX;
            robotFile.transferTo(new File(filePath));
            robotFile.transferTo(new File(filePath+Constants.COVER_IMAGE_SUFFIX));
        }
        redisComponent.saveSysSetting(sysSettingDto);
        return getSuccessResponseVO(null);
    }
}
