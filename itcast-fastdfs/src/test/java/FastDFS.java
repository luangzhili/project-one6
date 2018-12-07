import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.junit.Test;

import java.io.IOException;

/**
 * DATE:2018/12/1
 * USER:lzlWhite
 */
public class FastDFS {
    @Test
    public void test() throws Exception {
        //1. 获取追踪服务器的配置文件路径并设置到全局配置对象中；
        String con_filename = ClassLoader.getSystemResource("fastdfs/tracker.conf").getPath();
        ClientGlobal.init(con_filename);

        // 2. 创建追踪服务器客户端；
        TrackerClient trackerClient = new TrackerClient();
        // 3. 利用追踪服务器客户端获取追踪服务对象；
        TrackerServer trackerServer = trackerClient.getConnection();
        // 4. 创建存储服务对象；可以由追踪服务器获得所以可以为null
        StorageServer storageServer = null;
        //5. 创建存储客户端对象（从第五步开始）
        StorageClient storageClient = new StorageClient(trackerServer, storageServer);
        //6. 利用存储客户端对象的上传方法上传图片
        String[] upload_file = storageClient.upload_file("D:\\2.jpg", "jpg", null);
        if (upload_file != null && upload_file.length > 0) {
            //7. 处理上传的返回结果
            for (String str : upload_file) {
                System.out.println(str);
            }
            // 获取存储服务器信息
            String groupName = upload_file[0];
            String filename = upload_file[1];
            ServerInfo[] serverInfos =
                    trackerClient.getFetchStorages(trackerServer, groupName, filename);
            for (ServerInfo serverInfo : serverInfos) {
                System.out.println("ip=" + serverInfo.getIpAddr() + " ；port=" + serverInfo.getPort());
            }
// 组合可以访问的路径
            String url = "http://" + serverInfos[0].getIpAddr() + "/" +
                    groupName + "/" + filename;
            System.out.println(url);
        }
    }
}

