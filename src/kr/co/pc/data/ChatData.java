package kr.co.pc.data;

import java.io.Serializable;

/**
 * ä�ý� �޽��� ������ ���� ������ Ŭ����
 * 
 * <ul>
 * <li>�����̸�</li>
 * <li>�޼���</li>
 * <li>�����ð�</li>
 * </ul>
 */
public class ChatData implements Serializable {
	private static final long serialVersionUID = 7045108025817756246L;

	private String userName;
	private String message;
	private String date;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
