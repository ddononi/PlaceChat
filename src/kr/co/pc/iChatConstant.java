package kr.co.pc;


/**
 *	��� ���� �������̽��� ���� , FTP,��Ű���� �����Ѵ�.
 *	<p>�ֿ� ������� ������ ����.</p>
 *	<ul>
 *		<li>���� �ּ�</li>
 *		<li>���� ���������</li>
 *		<li>FTP����</li>
 *		<li>����Ű</li>
 *		<li>���䰪</li>
 *	</ul>
 *
 */
public interface iChatConstant {
	/* app setting */
	public final static String  PREFER = "placeChat preference";


    /* Server setting */
    public static final String SERVER_URL = "ddononi.cafe24.com";	// ************�ش� ftp ip �� ����***********************//
    public static final int SERVER_FTP_PORT = 21;
   // public static final int SERVER_TCP_PORT = 5379;
    public static final String FTP_NAME = "ddononi";			// ************�ش� ftp id �� ����***********************//
    public static final String FTP_PASSWORD = "goqkfkrl01";		// ************�ش� ftp pass �� ����***********************//
    public static final int MAX_SERVER_CONNECT_COUNT = 5;		//	�ִ� ���� ���� ȸ��
    public static final String FTP_PATH = "/www/placeChat/user_images/";	//	ftp path
    public static final int MAX_FILE_NAME_LENGTH = 100;			// �ִ� ���� �̸�
    public static final int MAX_FILE_SIZE = 5242880;			// �ִ� ���� ���� ���� ������ 5 Mb
    public static final String UPLOAD_URL = "/placeChat/insert.php";	// ���� ��� url
    public static final String LOGIN_URL = "/placeChat/login.php";		// �α��� ��� url
    public static final String LOGOUT_URL = "/placeChat/logout.php"; 	// �α׾ƿ� url
    public static final String UPDATE_URL = "/placeChat/update.php";	// ������ ������Ʈ url
    public static final String FRIEND_URL = "/placeChat/friends.php";	// ģ�� ã��  url
    public static final String CHECK_MSG = "/placeChat/checkMessage.php";	// �޽����� �ִ��� üũ
    public static final String IMAGE_DIR = "http://" + SERVER_URL + "/placeChat/user_images/";
	// �޴�
	public static final int MENU_CUSTOMER = 1;
	public static final int MENU_MAP = 0;

	// msg
	public static final int OK = 1;
    public static final int ACTION_RESULT = 0;

    /* map setting */
    public final static String DAUM_MMAPS_ANDROID_APIKEY = "9849a71475154c6a7a7e42778072ee226e741c9b";	// ���� Ű ����
    public final static int API_RESULT_OK = 200;	// ���� api Ű ���� ������
    public final static int MESSAGE_LOOP_TIME = 5000;	// �޽����� ������ �ֱ�

}
