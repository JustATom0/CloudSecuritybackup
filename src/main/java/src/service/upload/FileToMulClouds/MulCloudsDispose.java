package src.service.upload.fileToMulClouds;

import src.yunData.aliyun.AliyunOSS;
import src.yunData.netease.Netease;
import src.yunData.qcloud.Qcloud;
import src.yunData.qiniu.Qiniu;
import src.yunData.upyun.Upyun;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vellerzheng on 2017/10/2.
 */
public class MulCloudsDispose {

    private List<String> subdirtylist;

    public void getPartFilePath(String partFileDirectory){
        File file =new File(partFileDirectory);
        File[] fileList=file.listFiles();
        subdirtylist = new ArrayList<String>();

        for(int i = 0; fileList.length > i; i++){
            if(fileList[i].isFile()){
                subdirtylist.add(fileList[i].getPath());
            }
        }
    }

    public boolean uploadPartFileToClouds(){
        if(subdirtylist.size()==5) {
            AliyunOSS aliyunOSS = new AliyunOSS();
            aliyunOSS.uploadFile(subdirtylist.get(0));


            Netease netease =new Netease();
            netease.uploadFile(subdirtylist.get(1));


            Qcloud qcloud = new Qcloud();
            qcloud.uploadFile(subdirtylist.get(2));


            Qiniu qiniu = new Qiniu();
            qiniu.randomAcessUpLoadFile(subdirtylist.get(3));


            Upyun upyun =new Upyun();
            upyun.uploadFile(subdirtylist.get(4));
        }else{
            System.out.println("file number not matched.");
            return false;
        }
        return true;

    }

    /**
     * 普通单线程测试耗时：  79575 ms
     *
     * @param args
     */
    public static void main(String[] args){
        long startTime=System.currentTimeMillis();
        String directory="D:\\Test\\split";
        MulCloudsDispose mulCloudsDispose = new MulCloudsDispose();
        mulCloudsDispose.getPartFilePath(directory);
        mulCloudsDispose.uploadPartFileToClouds();
        long endTime=System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： "+(endTime-startTime)+"ms");

    }
}
