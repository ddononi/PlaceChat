package kr.co.pc;


/**
 *	상수 정의 인터페이스로 서버 , FTP,맴키등을 정의한다.
 *	<p>주요 상수들은 다음과 같다.</p>
 *	<ul>
 *		<li>서버 주소</li>
 *		<li>서버 사용자정보</li>
 *		<li>FTP정보</li>
 *		<li>지도키</li>
 *		<li>응답값</li>
 *	</ul>
 *
 */
public interface iChatConstant {
	/* app setting */
	public final static String  PREFER = "placeChat preference";


    /* Server setting */
    public static final String SERVER_URL = "ddononi.cafe24.com";	// ************해당 ftp ip 로 수정***********************//
    public static final int SERVER_FTP_PORT = 21;
   // public static final int SERVER_TCP_PORT = 5379;
    public static final String FTP_NAME = "ddononi";			// ************해당 ftp id 로 수정***********************//
    public static final String FTP_PASSWORD = "goqkfkrl01";		// ************해당 ftp pass 로 수정***********************//
    public static final int MAX_SERVER_CONNECT_COUNT = 5;		//	최대 서버 연결 회수
    public static final String FTP_PATH = "/www/placeChat/user_images/";	//	ftp path
    public static final int MAX_FILE_NAME_LENGTH = 100;			// 최대 파일 이름
    public static final int MAX_FILE_SIZE = 5242880;			// 최대 사진 파일 전송 사이즈 5 Mb
    public static final String UPLOAD_URL = "/placeChat/insert.php";	// 유저 등록 url
    public static final String LOGIN_URL = "/placeChat/login.php";		// 로그인 등록 url
    public static final String LOGOUT_URL = "/placeChat/logout.php"; 	// 로그아웃 url
    public static final String UPDATE_URL = "/placeChat/update.php";	// 내정보 업데이트 url
    public static final String FRIEND_URL = "/placeChat/friends.php";	// 친구 찾기  url
    public static final String CHECK_MSG = "/placeChat/checkMessage.php";	// 메시지가 있는지 체크
    public static final String IMAGE_DIR = "http://" + SERVER_URL + "/placeChat/user_images/";
	// 메뉴
	public static final int MENU_CUSTOMER = 1;
	public static final int MENU_MAP = 0;

	// msg
	public static final int OK = 1;
    public static final int ACTION_RESULT = 0;

    /* map setting */
    public final static String DAUM_MMAPS_ANDROID_APIKEY = "9849a71475154c6a7a7e42778072ee226e741c9b";	// 다음 키 설정
    public final static int API_RESULT_OK = 200;	// 다음 api 키 인증 성공값
    public final static int MESSAGE_LOOP_TIME = 5000;	// 메시지를 가져올 주기

}
