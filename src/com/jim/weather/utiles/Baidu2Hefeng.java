package com.jim.weather.utiles;
/**
 * 城市、地区名称的转化，主要是要去掉'县'、'市'、'区'
 * @author Administrator
 *
 */
public class Baidu2Hefeng {
	/**
	 * 
	 * @param city
	 * @param county
	 * @return 第一个是市，第二个是县
	 */
	public static String[] convert(String city, String county){
		if(city==null||county==null){
			return null;
		}
		String[] s = new String[2];
		s[0] = city.replace("市", "");
		if(county.length()==2){
			s[1] = county;
		}else{
			s[1] = county.replace("县", "").replace("区", "");
		}
		return s;
	}
}
