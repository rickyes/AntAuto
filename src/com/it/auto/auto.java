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
 * 完成编译操作，需要重新编译需手动设置android项目的AndroidManifest文件的package为com.example.ant0，
 * 并在MainActivity中导入对应的包
 * @author ZHOUMQ
 *
 */

public class auto {

	//android项目模板和当前项目根路径
	final static String path = "D:/Program Files/Eclipse/JSE/project/";
	//android项目模板路径
	final static String apkPath = path+"AntApkTest/";
	//Auto项目根路径
	final static String autoPath = path+"Auto/";
	
	final static int n = 10;

	private volatile static String[] packageNames = new String[n];
	private volatile static String[] keystores = new String[n];

	public static void main(String[] args) throws Exception {

		System.out.println("批量生成keystore签名文件");
		int index = 0;
		while (index < n) {
			String cmd = "keytool -genkey -keystore " + index + ".keystore -alias " + "ant -storepass android -keypass "
					+ "android -keyalg RSA -keysize 2048 -validity 10000 " + "-dname \"CN='Linux', OU='tecentbaidu', "
					+ "O='alibaba', L='SZ', ST='GD', C='CN'\"";
			Runtime.getRuntime().exec(cmd);
			index++;
		}
		
		/**
		 * 生成keystore需要时间，所以n越大，对应的睡眠时间应当适当加长
		 */
		Thread.sleep(30*1000);
		
		System.out.println("移动keystore到"+apkPath+"keystores");
		for (int i = 0; i < n; i++) {
			packageNames[i] = "com.example.ant" + i;
			keystores[i] = "" + i + ".keystore";
			//移动keystore到android项目文件夹
			new File(autoPath+keystores[i]).renameTo(new File(apkPath+"keystores/"+keystores[i]));
		}
		System.err.println("完成所有签名文件生成，所有keystore文件在"+apkPath+"keystores目录下");
		System.out.println("开始编译apk,并批量签名");
		for (int i = 0; i < n-1; i++) {
			start(packageNames, keystores, i);
		}
		System.err.println("完成所有编译，所有apk在"+apkPath+"apks目录下");
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

		System.out.println("开始编译----" + i+1);
		String buildApk = autoPath+"ant.cmd";
		Process exec = Runtime.getRuntime().exec(buildApk);
		exec.waitFor();
		System.out.println("完成");
	}

	public static void saveXML(Document doc) {
		// 将doc对象输出到文件
		try {
			// 创建xml文件输出流
			XMLOutputter xmlopt = new XMLOutputter();

			// 创建文件输出流
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(apkPath+"build.xml"), "UTF-8");

			// 指定文档格式
			Format fm = Format.getPrettyFormat();
			fm.setEncoding("GBk");
			xmlopt.setFormat(fm);

			// 将doc写入到指定的文件中
			xmlopt.output(doc, writer);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
