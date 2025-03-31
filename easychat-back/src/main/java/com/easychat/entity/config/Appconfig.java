package com.easychat.entity.config;


import com.easychat.utils.StringTools;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("appconfig")
public class Appconfig {
    @Value("${ws.port:}")
    public Integer wsPort;

    @Value("${project.folder:}")
    public String projectFolder;

    @Value("${admin.email:}")
    public String adminEmail;

    public String getProjectFolder() {
        if(StringTools.isEmpty(projectFolder)&&!projectFolder.endsWith("/")){
            projectFolder=projectFolder+"/";
        }
        return projectFolder;
    }
    public Integer getWsPort() {
        return wsPort;
    }
    public String getAdminEmail() {
        return adminEmail;
    }
}
