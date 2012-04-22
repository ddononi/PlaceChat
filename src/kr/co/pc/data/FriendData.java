package kr.co.pc.data;
import java.io.Serializable;

/**
 * ä�ý� �޽��� ������ ���� ������ Ŭ����
 * 
 * <ul>
 * <li>�ε���</li>
 * <li>�����̸�</li>
 * <li>�̹�����</li>
 * <li>����</li>
 * <li>�浵</li>
 * <li>�������</li>
 * </ul>
 */
public class FriendData implements Serializable {
	/**
	 * �ø�������� UID��
	 */
	private static final long serialVersionUID = 1L;
	private int index;
	private String userName;
	private String imageFile;
	private String lat;
	private String lng;
	private String lecture;
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public String getLecture() {
		return lecture;
	}

	public void setLecture(String lecture) {
		this.lecture = lecture;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getImageFile() {
		return imageFile;
	}

	public void setImageFile(String imageFile) {
		this.imageFile = imageFile;
	}
	
	

}
