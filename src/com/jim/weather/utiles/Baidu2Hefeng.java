package com.jim.weather.utiles;
/**
 * ���С��������Ƶ�ת������Ҫ��Ҫȥ��'��'��'��'��'��'
 * @author Administrator
 *
 */
public class Baidu2Hefeng {
	/**
	 * 
	 * @param city
	 * @param county
	 * @return ��һ�����У��ڶ�������
	 */
	public static String[] convert(String city, String county){
		if(city==null||county==null){
			return null;
		}
		String[] s = new String[2];
		s[0] = city.replace("��", "");
		if(county.length()==2){
			s[1] = county;
		}else{
			s[1] = county.replace("��", "").replace("��", "");
		}
		return s;
	}
}
