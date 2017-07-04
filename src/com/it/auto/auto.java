package com.it.auto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * ��ɱ����������Ҫ���±������ֶ�����android��Ŀ��AndroidManifest�ļ���packageΪcom.example.ant0��
 * ����MainActivity�е����Ӧ�İ�
 * @author ZHOUMQ
 *
 */

public class auto {

	//android��Ŀģ��͵�ǰ��Ŀ��·��
	final static String path = "D:/Program Files/Eclipse/JSE/project/";
	//android��Ŀģ��·��
	final static String apkPath = path+"AntApkTest/";
	//Auto��Ŀ��·��
	final static String autoPath = path+"Auto/";
	
	final static int n = 10;

	private volatile static String[] packageNames = new String[n];
	private volatile static String[] keystores = new String[n];

	public static void main(String[] args) throws Exception {

		System.out.println("��������keystoreǩ���ļ�");
		int index = 0;
		while (index < n) {
			String cmd = "keytool -genkey -keystore " + index + ".keystore -alias " + "ant -storepass android -keypass "
					+ "android -keyalg RSA -keysize 2048 -validity 10000 " + "-dname \"CN='Linux', OU='tecentbaidu', "
					+ "O='alibaba', L='SZ', ST='GD', C='CN'\"";
			Runtime.getRuntime().exec(cmd);
			index++;
		}
		
		/**
		 * ����keystore��Ҫʱ�䣬����nԽ�󣬶�Ӧ��˯��ʱ��Ӧ���ʵ��ӳ�
		 */
		Thread.sleep(30*1000);
		
		System.out.println("�ƶ�keystore��"+apkPath+"keystores");
		for (int i = 0; i < n; i++) {
			packageNames[i] = "com.example.ant" + i;
			keystores[i] = "" + i + ".keystore";
			//�ƶ�keystore��android��Ŀ�ļ���
			new File(autoPath+keystores[i]).renameTo(new File(apkPath+"keystores/"+keystores[i]));
		}
		System.err.println("�������ǩ���ļ����ɣ�����keystore�ļ���"+apkPath+"keystoresĿ¼��");
		System.out.println("��ʼ����apk,������ǩ��");
		for (int i = 0; i < n-1; i++) {
			start(packageNames, keystores, i);
		}
		System.err.println("������б��룬����apk��"+apkPath+"apksĿ¼��");
	}

	public static void start(String[] packNames, String[] keystores, int i) throws Exception {
		SAXBuilder sb = new SAXBuilder();
		Document doc = null;
		try {
			InputStream in = new FileInputStream(apkPath+"build.xml");
			InputStreamReader isr = new InputStreamReader(in, "UTF-8");
			doc = sb.build(isr);
			Element root = doc.getRootElement();
			List<Element> list = root.getChildren("property");
			for (Element el : list) {
				if ("old_package_name".equals(el.getAttribute("name").getValue())) {
					el.getAttribute("value").setValue(packNames[i]);
				} else if ("new_package_name".equals(el.getAttribute("name").getValue())) {
					el.getAttribute("value").setValue(packNames[i + 1]);
				} else if ("keystore".equals(el.getAttribute("name").getValue())) {
					el.getAttribute("value").setValue(keystores[i]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		saveXML(doc);

		System.out.println("��ʼ����----" + i+1);
		String buildApk = autoPath+"ant.cmd";
		Process exec = Runtime.getRuntime().exec(buildApk);
		exec.waitFor();
		System.out.println("���");
	}

	public static void saveXML(Document doc) {
		// ��doc����������ļ�
		try {
			// ����xml�ļ������
			XMLOutputter xmlopt = new XMLOutputter();

			// �����ļ������
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(apkPath+"build.xml"), "UTF-8");

			// ָ���ĵ���ʽ
			Format fm = Format.getPrettyFormat();
			fm.setEncoding("GBk");
			xmlopt.setFormat(fm);

			// ��docд�뵽ָ�����ļ���
			xmlopt.output(doc, writer);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
