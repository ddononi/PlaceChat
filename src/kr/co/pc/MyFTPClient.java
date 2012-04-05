package kr.co.pc;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.oroinc.net.ftp.FTP;
import com.oroinc.net.ftp.FTPClient;
import com.oroinc.net.ftp.FTPReply;

/**
 * Ftp class
 * NetComponents-1.3.8.jar ���
 *
 */
public class MyFTPClient {
	FTPClient ftpClient;
    String server = null;
    int port = 0;
    String id = null;
    String password = null;
    
    public MyFTPClient(String server, int port, String id, String password){
        this.server = server;
        this.port = port;
        this.id = id;
        this.password = password;	
        ftpClient = new FTPClient();	//NetComponents-1.3.8.jar
    }
 

    
    /**
     * ftp ���� ����
     * @return
     * 	���� ���� ����
     */
    protected boolean connect(){
        try{
            ftpClient.connect(server, port);
            int reply;

            reply = ftpClient.getReplyCode();	// ����
            if(!FTPReply.isPositiveCompletion(reply)){	// ���� ������ �ƴϸ� ���� ����
                ftpClient.disconnect();	
                return false;		
            }else{
            	return true;
            }
        }catch(IOException ioe){
            if(ftpClient.isConnected()){	// ����Ǿ� ������
                try{
                    ftpClient.disconnect();	// ����
                }catch(IOException e){
                	
                }
            }
        }
        
        return false;
    }
    
    /**
     * ������� üũ 
     * @return
     * 		������� ����
     */
    protected boolean isConnected() {
    	return ftpClient.isConnected();
	}
    
    /**
     * ftp �α���
     * @return
     * 		�α��� ��������
     */
    protected boolean login(){
        try{
            this.connect();
            //System.out.println("login ==> " + ftpClient.getReplyString());
            return ftpClient.login(id, password);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }  
        return false;
    }   
    
    /**
     * ftp �α� �ƿ�
     * @return
     * 		�α׾ƿ� ��������
     */
    protected boolean logout(){
    	try{
    		return ftpClient.logout();
    	}catch(IOException e){
    		
    	}
    	
    	return false;
    	
    }     
    
    /**
     * path ���丮 �ٲٱ�
     */
    protected void cd(String path){
        try{
            ftpClient.changeWorkingDirectory(path);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }   
    }    
    
    /**
     * ���ε� ó��
     * @param upFile
     * 		���ε��� �����̸�
     * @param renameFile
     * 		������ ����� �����̸�
     * @return
     * 		���� ���� ���� ����
     */
    protected boolean upload(String upFile, String renameFile){
        File uploadFile = new File(upFile);
        FileInputStream fis = null;
        
        boolean flag = false;
        try{
            fis = new FileInputStream(uploadFile);
            //���̳ʸ� ���� ����
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);	// �̹��������̱� ������ ���̳ʸ���
            boolean isSuccess = ftpClient.storeFile( renameFile , fis);	// ������ ��������
   
            if(isSuccess){	// ���ε� ����
            	flag = true;
            }
        }catch(IOException ioe){
            System.out.println(ioe.getMessage());
        }catch(Exception e){
            System.out.println(e.getMessage());
        }finally{
            if(fis != null){
                try{
                    fis.close();
                }catch(IOException ioe){
     
                }
            }
        }
        return flag;
    }   
}