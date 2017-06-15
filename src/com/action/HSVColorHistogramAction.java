/**
 * 
 */
package com.action;

import image.identification.HSV;
import image.identification.Pixel;
import image.identification.Util;

import java.awt.Color;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.sun.jmx.snmp.Timestamp;

/**
 * @author 小康
 * @date： 日期：Apr 17, 2017 时间：9:29:34 PM
 */
public class HSVColorHistogramAction extends ActionSupport {
	private static final long serialVersionUID = 1L;

	// 用户上传的文件
	private File uploadFile;
	// 上传文件的类型
	private String uploadFileContentType;
	// 上传文件的文件名
	private String uploadFileName;
	// 相似的图片路径
	private ArrayList<String> strArray = new ArrayList<String>();
	// 相似的图片名称
	private ArrayList<String> strArrayName = new ArrayList<String>();
	// 图片相似度
	private ArrayList<String> samerateStrings = new ArrayList<String>();

	public ArrayList<String> getSamerateStrings() {
		return samerateStrings;
	}

	public void setSamerateStrings(ArrayList<String> samerateStrings) {
		this.samerateStrings = samerateStrings;
	}

	// strArray属性getter方法
	public ArrayList<String> getStrArray() {
		return strArray;
	}

	// strArray属性setter方法
	public void setStrArray(ArrayList<String> strArray) {
		this.strArray = strArray;
	}

	// strArrayName属性getter方法
	public ArrayList<String> getStrArrayName() {
		return strArrayName;
	}

	// strArrayName属性setter方法
	public void setName1(ArrayList<String> strArrayName) {
		this.strArrayName = strArrayName;
	}

	// uploadFile 属性getter方法
	public File getUploadFile() {
		return uploadFile;
	}

	// uploadFile 属性setter方法
	public void setUploadFile(File uploadFile) {
		this.uploadFile = uploadFile;
	}

	// uploadFileContentType 属性getter方法
	public String getUploadFileContentType() {
		return uploadFileContentType;
	}

	// ploadFileContentType 属性setter方法
	public void setUploadFileContentType(String uploadFileContentType) {
		this.uploadFileContentType = uploadFileContentType;
	}

	// uploadFileName 属性getter方法
	public String getUploadFileName() {
		return uploadFileName;
	}

	// uploadFileName 属性setter方法
	public void setUploadFileName(String uploadFileName) {
		this.uploadFileName = uploadFileName;
	}

	public String HSVIdentification() throws Exception {
		// 获取CLASSPATH路径,得到项目下webroot/
		String realpath1 = ServletActionContext.getServletContext()
				.getRealPath("/images");
		// 创建file对象 realpath文件夹
		File dir1 = new File(realpath1);
		// System.out.println(realpath1);
		if (!dir1.exists()) {
			dir1.mkdir();
		}
		// 设置上传图片格式
		if (uploadFileContentType.equals("image/jpeg")) {
			uploadFileContentType = ".jpg";
		} else if (uploadFileContentType.equals("image/png")) {
			uploadFileContentType = ".png";
		} else if (uploadFileContentType.equals("image/gif")) {
			uploadFileContentType = ".gif";
		} else if (uploadFileContentType.equals("image/bmp")) {
			uploadFileContentType = ".bmp";
		}
		// 重新命名uploadFileName
		uploadFileName = "image" + uploadFileName
				+ new Timestamp().getDateTime() + uploadFileContentType;
		// 复制文件到指定新目录
		FileUtils.copyFile(uploadFile, new File(dir1, uploadFileName));
		// 上传的图片存到的服务器路径
		String src = realpath1 + "\\" + uploadFileName;
		// path为检索的图片库
		String path = realpath1 + "\\" + "Testpictures" + "\\";
		Pixel[][] matrix = image.identification.HSVColorHistogram
				.getImagePixel(src, 200, 200);
		HSV[][] hsvMatrix = new HSV[matrix.length][];
		File file = new File(path);
		int i = 0;
		ArrayList<String> strArray = new ArrayList<String>();
		// map为[相识度-图片路径]
		TreeMap<Double, String> map = new TreeMap<Double, String>(
				new Comparator<Double>() {
					/*
					 * int compare(Object o1, Object o2) 返回一个基本类型的整型， 返回负数表示：o1
					 * 小于o2， 返回0 表示：o1和o2相等， 返回正数表示：o1大于o2。
					 */
					public int compare(Double o1, Double o2) {
						// 指定排序器按照降序排列
						return o2.compareTo(o1);
					}
				});
		File[] tempList = file.listFiles();
		// 相似度samlist
		List<Double> samelist = new ArrayList<Double>();
		// 图像库中的图像信息
		// ArrayList<String> strArrayName1 = new ArrayList<String>();
		System.out.println("该目录下对象个数：" + tempList.length);
		for (i = 0; i < tempList.length; i++) {
			if (tempList[i].isFile()) {
				// System.out.print("文     件：" + tempList[i]);
				// strArrayName.add(tempList[i].toString().split("\\\\")[8]);
				// strArray.add(tempList[i].toString());
				// System.out.println("name：" + strArrayName.get(i) + "\tpath："
				// + strArray.get(i));
				Pixel[][] matrix1 = image.identification.HSVColorHistogram
						.getImagePixel(tempList[i].toString(), 200, 200);
				HSV[][] hsvMatrix1 = new HSV[matrix1.length][];

				for (int k1 = 0; k1 < matrix.length; k1++) {
					hsvMatrix[k1] = new HSV[matrix[k1].length];
					hsvMatrix1[k1] = new HSV[matrix1[k1].length];
					for (int j = 0; j < matrix[k1].length; j++) {
						float[] fs = Color.RGBtoHSB(matrix[k1][j].red,
								matrix[k1][j].green, matrix[k1][j].blue, null);
						HSV hsv = new HSV();
						hsv.h = (int) (fs[0] * 255);
						hsv.s = (int) (fs[1] * 255);
						hsv.v = (int) (fs[2] * 255);
						hsvMatrix[k1][j] = hsv;
						fs = Color
								.RGBtoHSB(matrix1[k1][j].red,
										matrix1[k1][j].green,
										matrix1[k1][j].blue, null);
						hsv = new HSV();
						hsv.h = (int) (fs[0] * 255);
						hsv.s = (int) (fs[1] * 255);
						hsv.v = (int) (fs[2] * 255);
						hsvMatrix1[k1][j] = hsv;
					}
				}
				int[][] histogram = new int[3][256];
				int[][] histogram1 = new int[3][256];
				for (int k2 = 0; k2 < hsvMatrix.length; k2++) {
					for (int j = 0; j < hsvMatrix[0].length; j++) {
						histogram[0][hsvMatrix[k2][j].h]++;
						histogram[1][hsvMatrix[k2][j].s]++;
						histogram[2][hsvMatrix[k2][j].v]++;
						histogram1[0][hsvMatrix1[k2][j].h]++;
						histogram1[1][hsvMatrix1[k2][j].s]++;
						histogram1[2][hsvMatrix1[k2][j].v]++;
					}
				}

				int[] vector = Util.matrix2vector(histogram);
				int[] vector1 = Util.matrix2vector(histogram1);
				samelist.add(image.identification.HSVColorHistogram
						.calculateSimilarity(vector, vector1));
				map.put(samelist.get(i), tempList[i].toString());
			}
		}
		int k = 0;
		Set<Double> key1 = map.keySet();
		NumberFormat fmt = NumberFormat.getPercentInstance();
		fmt.setMinimumFractionDigits(4);// 最多两位百分小数，如25.23%
		for (Double key : map.keySet()) {
			String value = map.get(key);
			samerateStrings.add(fmt.format(key));
			System.out.println(samerateStrings.get(k) + " HSV  " + value);
			k++;
		}
		Iterator<Double> int1 = key1.iterator();
		// 根据遍历的key值依次输出前12个value值
		for (int j = 0; j < 20; j++) {
			strArray.add(map.get(int1.next()));
			strArrayName.add(strArray.get(j).split("\\\\")[7]);
			System.out.println(strArray.get(j) + "--" + strArrayName.get(j));
		}
		return SUCCESS;

	}
}
