package kr.co.pc;


/**
 *	상수 정의 인터페이스
 */
public interface iChatConstant {
	/* app setting */
	public final static String  PREFER = "placeChat preference";


    /* Server setting */
    public static final String SERVER_IP = "ddononi.cafe24.com";	// ************해당 ftp ip 로 수정***********************//
    public static final int SERVER_FTP_PORT = 21;
   // public static final int SERVER_TCP_PORT = 5379;
    public static final String FTP_NAME = "ddononi";			// ************해당 ftp id 로 수정***********************//
    public static final String FTP_PASSWORD = "goqkfkrl01";		// ************해당 ftp pass 로 수정***********************//
    public static final int MAX_SERVER_CONNECT_COUNT = 5;		//	최대 서버 연결 회수
    public static final String FTP_PATH = "/www/placeChat/user_images/";	//	ftp path
    public static final int MAX_FILE_NAME_LENGTH = 100;			// 최대 파일 이름
    public static final int MAX_FILE_SIZE = 5242880;			// 최대 사진 파일 전송 사이즈 5 Mb
    public static final String UPLOAD_URL = "/placeChat/insert.php";	// 유저 등록 url


	// 메뉴
	public static final int MENU_CUSTOMER = 1;
	public static final int MENU_MAP = 0;

	// msg
	public static final int OK = 1;
    public static final int ACTION_RESULT = 0;


}
