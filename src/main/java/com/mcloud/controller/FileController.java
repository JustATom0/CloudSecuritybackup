package com.mcloud.controller;

import com.mcloud.model.FilesEntity;
import com.mcloud.repository.FileRepository;
import com.mcloud.service.UploadFileService;
import com.mcloud.service.supportToolClass.FileManage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

/**
 * Created by vellerzheng on 2017/9/20.
 */
@Controller
public class FileController {

    @Autowired
    FileRepository fileRepository;

    @Resource(name="uploadFileServiceImpl")
    private UploadFileService uploadFileService;

    @RequestMapping(value ="/clouds/filemanager/uploadfile/{id}", method = RequestMethod.GET)
    public String getUploadForm(@PathVariable("id") int uid, ModelMap modelMap){
        // UsersEntity usersEntity= userRepository.findUsersEntityById(uid);
        modelMap.addAttribute("authUsersEntity",uid);
        return "clouds/filemanager/uploadfile";
    }

    //上传文件会自动绑定到MultipartFile中
    @RequestMapping(value="/clouds/filemanager/uploadfile/add",method = RequestMethod.POST)
    public String upload(HttpServletRequest request,@RequestParam("file") MultipartFile file,
                         @RequestParam("description") String description,@RequestParam("curAuthUserEntity") int usrEty, ModelMap modelMap) throws Exception {
        System.out.println("start!");
        System.out.println(description);
        System.out.println(usrEty);
        /*还需要判断文件是否大于4M */
        //如果文件不为空，写入上传路径
        if(!file.isEmpty()) {
            //上传文件路径
            String path = request.getServletContext().getRealPath("upload");
            //上传文件分块路径
            String pathPart =request.getServletContext().getRealPath("upload")+"\\filepart";
            //上传文件名
            System.out.println(path);
            String filename = file.getOriginalFilename();
            File filepath = new File(path,filename);
            //判断路径是否存在，如果不存在就创建一个
            if (!filepath.getParentFile().exists()) {
                filepath.getParentFile().mkdirs();
            }

            File filepathPart = new File(pathPart);
            //判断路径是否存在，如果不存在就创建一个
            if (!filepathPart.exists()) {
                filepathPart.mkdirs();
            }

            //将上传文件保存到一个目标文件当中
            file.transferTo(new File(path + File.separator + filename));
            modelMap.addAttribute("fileUrl", request.getContextPath()+"/upload/"+filename);
            System.out.println("upload file finished!");

            int fileSize = (int)file.getSize();
            uploadFileService.initUploadFile(path,pathPart,fileSize,description,filename,usrEty);
            uploadFileService.dealFileUpload();
            uploadFileService.saveFileInfoToDateBase();

            /*判断路径是否存在，如果存在就删除*/
            if (filepathPart.exists()) {
                FileManage.deleteDirectory(pathPart);
            }
            if (filepath.getParentFile().exists()) {
                FileManage.deleteDirectory(path);
            }

            return "clouds/filemanager/uploadResult";
        } else {
            return "clouds/error";
        }

    }

    @RequestMapping(value ="/clouds/filemanager/files/{id}", method = RequestMethod.GET)
    public String getFiles(@PathVariable("id") int id, ModelMap modelMap){
        List<FilesEntity> fileList = fileRepository.findByFilesEntityEEndsWith(id);
        modelMap.addAttribute("loginId",id);
        modelMap.addAttribute("fileList",fileList);
        return "clouds/filemanager/files";
    }

    @RequestMapping(value ="/clouds/filemanager/files/show/{id}",method = RequestMethod.GET)
    public String showFiles(@PathVariable("id") int id ,ModelMap modelMap){
        FilesEntity filesDetial = fileRepository.findOne(id);

        modelMap.addAttribute("filesDetial",filesDetial);
        return "clouds/filemanager/fileDetial";
    }
}