package com.jim.weather.utiles;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import android.content.Context;

/**
 * ��ת��
 * @author Administrator
 *
 */
public class StringUtils {
	/**
	 * ͨ����������ȡ�ַ���
	 * @param is
	 * @return �쳣�ͷ���null
	 */
	public static String getStringByInputStream(InputStream is) {
		// TODO Auto-generated method stub
		try {
			ByteArrayOutputStream bao = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int len = 0;
			while((len = is.read(b))!=-1){
				bao.write(b);
			}
			is.close();
			return bao.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
/**
 * �����ݱ��浽���أ���cityid����
 * @param context
 * @param cityid
 * @param result
 */
	public static void saveJson(Context context, String cityid,
			String result) {
		// TODO Auto-generated method stub
		File file = new File(context.getFilesDir(), cityid+".json");
		FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file);
				byte[] resultB = result.getBytes();
				fos.write(resultB);
				fos.flush();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
}
