package kr.co.pc;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.oroinc.net.ftp.FTP;
import com.oroinc.net.ftp.FTPClient;
import com.oroinc.net.ftp.FTPReply;

/**
 * Ftp class
 * NetComponents-1.3.8.jar 사용
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
     * ftp 연결 설정
     * @return
     * 	연결 성공 여부
     */
    protected boolean connect(){
        try{
            ftpClient.connect(server, port);
            int reply;

            reply = ftpClient.getReplyCode();	// 응답
            if(!FTPReply.isPositiveCompletion(reply)){	// 정상 응답이 아니면 끊고 종료
                ftpClient.disconnect();	
                return false;		
            }else{
            	return true;
            }
        }catch(IOException ioe){
            if(ftpClient.isConnected()){	// 연결되어 있으면
                try{
                    ftpClient.disconnect();	// 끊자
                }catch(IOException e){
                	
                }
            }
        }
        
        return false;
    }
    
    /**
     * 연결상태 체크 
     * @return
     * 		연결상태 여부
     */
    protected boolean isConnected() {
    	return ftpClient.isConnected();
	}
    
    /**
     * ftp 로그인
     * @return
     * 		로그인 성공여부
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
     * ftp 로그 아웃
     * @return
     * 		로그아웃 성공여부
     */
    protected boolean logout(){
    	try{
    		return ftpClient.logout();
    	}catch(IOException e){
    		
    	}
    	
    	return false;
    	
    }     
    
    /**
     * path 디렉토리 바꾸기
     */
    protected void cd(String path){
        try{
            ftpClient.changeWorkingDirectory(path);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }   
    }    
    
    /**
     * 업로드 처리
     * @param upFile
     * 		업로드할 파일이름
     * @param renameFile
     * 		서버에 저장될 파일이름
     * @return
     * 		파일 전송 성공 여부
     */
    protected boolean upload(String upFile, String renameFile){
        File uploadFile = new File(upFile);
        FileInputStream fis = null;
        
        boolean flag = false;
        try{
            fis = new FileInputStream(uploadFile);
            //바이너리 모드로 전송
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);	// 이미지파일이기 때문에 바이너리로
            boolean isSuccess = ftpClient.storeFile( renameFile , fis);	// 파일을 저장하자
   
            if(isSuccess){	// 업로드 성공
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