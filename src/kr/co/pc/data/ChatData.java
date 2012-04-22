package kr.co.pc.data;

import java.io.Serializable;

/**
 * 채팅시 메시지 정보를 담을 데이터 클랙스
 * 
 * <ul>
 * <li>유저이름</li>
 * <li>메세지</li>
 * <li>보낸시각</li>
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
