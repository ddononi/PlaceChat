package kr.co.pc;

import android.telephony.TelephonyManager;

/*
 *  디바이스 정보 가져오기
 *  휴대폰 번호, 단말기번호, SIM 정보
 */
public class DeviceInfo{
	private static TelephonyManager _telephony = null;
	private static DeviceInfo di = new DeviceInfo();
	private DeviceInfo(){
		
	}
	
	public static DeviceInfo setDeviceInfo(TelephonyManager telphony){
		_telephony = telphony;
		return di;
	}
	
	public String getDeviceNumber(){
		String phoneNum = (_telephony != null)?_telephony.getLine1Number():"";
		//String cellNum = phoneNum.substring(phoneNum.length()-8, phoneNum.length());
		String cellNum = phoneNum.replace("+82", "0");
		return cellNum;	// 핸드폰 번호에 맞게 넘겨줌 ex) 010,011
	}
	/*
	public String getDeviceNumber2(){
		String phoneNum = (_telephony != null)?_telephony.getLine1Number():"";
		//String cellNum = phoneNum.substring(phoneNum.length()-8, phoneNum.length());
		
		return phoneNum;	// 핸드폰 번호에 맞게 넘겨줌 ex) 010,011
	}	
*/
	public String getMyDeviceID(){	
		return (_telephony != null)? _telephony.getDeviceId():"";
	}
	
	public String myDeviceSIM(){
		return (_telephony != null)? _telephony.getSimSerialNumber():"";
	}

}
