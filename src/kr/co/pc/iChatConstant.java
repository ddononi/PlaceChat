package kr.co.pc;


/**
 *	��� ���� �������̽�
 */
public interface iChatConstant {
	/* app setting */
	public final static String  PREFER = "placeChat preference";


    /* Server setting */
    public static final String SERVER_IP = "ddononi.cafe24.com";	// ************�ش� ftp ip �� ����***********************//
    public static final int SERVER_FTP_PORT = 21;
   // public static final int SERVER_TCP_PORT = 5379;
    public static final String FTP_NAME = "ddononi";			// ************�ش� ftp id �� ����***********************//
    public static final String FTP_PASSWORD = "goqkfkrl01";		// ************�ش� ftp pass �� ����***********************//
    public static final int MAX_SERVER_CONNECT_COUNT = 5;		//	�ִ� ���� ���� ȸ��
    public static final String FTP_PATH = "/www/placeChat/user_images/";	//	ftp path
    public static final int MAX_FILE_NAME_LENGTH = 100;			// �ִ� ���� �̸�
    public static final int MAX_FILE_SIZE = 5242880;			// �ִ� ���� ���� ���� ������ 5 Mb
    public static final String UPLOAD_URL = "/placeChat/insert.php";	// ���� ��� url


	// �޴�
	public static final int MENU_CUSTOMER = 1;
	public static final int MENU_MAP = 0;

	// msg
	public static final int OK = 1;
    public static final int ACTION_RESULT = 0;


}