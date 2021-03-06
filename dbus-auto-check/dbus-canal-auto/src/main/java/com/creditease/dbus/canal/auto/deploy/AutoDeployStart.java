package com.creditease.dbus.canal.auto.deploy;
import com.creditease.dbus.canal.auto.deploy.container.CuratorContainer;
import com.creditease.dbus.canal.auto.deploy.bean.DeployPropsBean;
import com.creditease.dbus.canal.auto.deploy.utils.FileUtils;
import com.creditease.dbus.canal.auto.deploy.utils.ZKUtils;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import static com.creditease.dbus.canal.auto.deploy.utils.FileUtils.*;

public class AutoDeployStart {

    private static final String DEPLOY_PROS_NAME="canal-auto.properties";

    public static void main(String[] args) throws Exception {

        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;

        try {
            //获得当前目录
            String currentPath =System.getProperty("user.dir");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String strTime = sdf.format(new Date());
            File reportFile = new File(currentPath, "canal_deploy_report" + strTime + ".txt");
            fos = new FileOutputStream(reportFile);
            osw = new OutputStreamWriter(fos);
            bw = new BufferedWriter(osw);

            DeployPropsBean deployProps = FileUtils.readProps(currentPath+"/conf/"+DEPLOY_PROS_NAME,bw);
            String basePath =deployProps.getCanalInstallPath();



            bw.write("************ CANAL DEPLOY BEGIN! ************");
            bw.newLine();
            printDeployProps(deployProps,bw);

            //修改canal.properties文件
            String canalProperties = "canal.properties";
            bw.write("------------ update canal.properties begin ------------ ");
            bw.newLine();
            WriteProperties(basePath+"/conf/"+canalProperties,
                    "canal.port",String.valueOf(getAvailablePort()),bw);
            WriteProperties(basePath+"/conf/"+canalProperties,
                    "canal.zkServers", deployProps.getZkPath()+"/DBus/Canal/"+deployProps.getDsName(),bw);
            bw.write("------------ update canal.properties end ------------ ");
            bw.newLine();

            //创建canal目录下dsName文件夹
            checkExist(basePath,deployProps.getDsName(),bw);
            //instance文件修改
            String instancePropsPath = basePath+"/conf/"+deployProps.getDsName()+"/"+"instance.properties";
            bw.write("------------ update instance.properties begin ------------ ");
            bw.newLine();
            bw.write("instance file path "+instancePropsPath);
            bw.newLine();
            WriteProperties(instancePropsPath, "canal.instance.master.address",deployProps.getSlavePath(),bw);
            WriteProperties(instancePropsPath, "canal.instance.dbUsername",deployProps.getCanalUser(),bw);
            WriteProperties(instancePropsPath, "canal.instance.dbPassword ",deployProps.getCanalPwd(),bw);
            WriteProperties(instancePropsPath, "canal.instance.connectionCharset "," UTF-8",bw);
            bw.write("------------ update canal.properties end ------------ ");
            bw.newLine();
            //创建canal节点
            ZKUtils.checkZKNode(deployProps,bw);

            bw.write("************ CANAL DEPLOY SCCESS! ************");


        }catch (Exception e){
            bw.write("************ CANAL DEPLOY ERROR! ************");
        }finally {
            if(bw!=null){
                bw.close();
            }
            if(osw != null){
                osw.close();
            }
            if(fos != null){
                fos.close();
            }
            if(CuratorContainer.getInstance().getCurator() != null){
                CuratorContainer.getInstance().close();
            }
        }
    }


    private static void checkExist(String currentPath,String dsName,BufferedWriter bw) throws Exception{
        File instanceDirectory = new File(currentPath+"/conf/"+dsName);
        if(!instanceDirectory.exists()){
            //canal/conf/example
            File exampleDirectory = new File(currentPath+"/conf/"+"example");
            String cmd = MessageFormat.format("cp -r {0} {1}",exampleDirectory,instanceDirectory);
            Process process = Runtime.getRuntime().exec(cmd);
            int exitValue = process.waitFor();
            if (0 != exitValue) {
                bw.write("cp instance.properties error. from: "+exampleDirectory+" to "
                        +instanceDirectory);
                bw.newLine();
                throw new RuntimeException("cp instance.properties error,from: "+exampleDirectory+" to "
                        +instanceDirectory);
            }

        }

    }

    private static int getAvailablePort(){
        int startPort=10000;
        int endPort=40000;
        for(int port = startPort; port<= endPort;port++){
            if(isPortAvailable(port)){
                return port;
            }
        }
        System.out.println("canal端口自动分配失败");
        return -1;
    }
    private static boolean isPortAvailable(int port){
        try {
            bindPort("0.0.0.0", port);
            bindPort(InetAddress.getLocalHost().getHostAddress(), port);

            return true;
        }catch (IOException e){
            return false;
        }

    }
    private static void bindPort(String host,int port) throws IOException {
        Socket s = new Socket();
        s.bind(new InetSocketAddress(host,port));
        s.close();
    }

}
