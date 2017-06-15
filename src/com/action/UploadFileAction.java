package com.action;

import image.ImageDecorator;
import image.ImageDigital;
import image.ImageProcessing;
import image.sharpening.EdgeDetection;
import image.sharpening.Sharpening;
import image.sharpening.Sobel;

import java.awt.image.BufferedImage;
import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;
import com.sun.jmx.snmp.Timestamp;

public class UploadFileAction extends ActionSupport {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// 用户上传的文件
	private File uploadFile;
	// 上传文件的类型
	private String uploadFileContentType;
	// 上传文件的文件名
	private String uploadFileName;
	// 灰度处理后的文件名
	private String grayuploadFileName;

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

	// grayuploadFileName 属性getter方法
	public String getGrayUploadFileName() {
		return grayuploadFileName;
	}

	// grayuploadFileName 属性setter
	public void setGrayUploadFileName(String grayuploadFileName) {
		this.grayuploadFileName = grayuploadFileName;
	}

	// 上传文件
	public String upload() throws Exception {
		// 获取CLASSPATH路径,得到项目下webroot/
		String realpath1 = ServletActionContext.getServletContext()
				.getRealPath("/images");
		// 创建file对象 realpath文件夹
		File dir1 = new File(realpath1);
		if (!dir1.exists()) {
			dir1.mkdir();
		}
		if (uploadFileContentType.equals("image/jpeg")) {
			uploadFileContentType = ".jpg";
		} else if (uploadFileContentType.equals("image/png")) {
			uploadFileContentType = ".png";
		} else if (uploadFileContentType.equals("image/gif")) {
			uploadFileContentType = ".gif";
		} else if (uploadFileContentType.equals("image/bmp")) {
			uploadFileContentType = ".bmp";
		}

		System.out.println("原文件名：" + uploadFileName);
		// 重新命名uploadFileName
		uploadFileName = "image" + uploadFileName
				+ new Timestamp().getDateTime() + uploadFileContentType;
		System.out.println("处理后文件名：" + uploadFileName);
		// 复制文件到指定新目录
		FileUtils.copyFile(uploadFile, new File(dir1, uploadFileName));

		String s1 = realpath1 + "\\" + uploadFileName;
		String s2 = realpath1 + "\\" + "Test" + uploadFileName;
		// 图像锐化
		Sharpening sharp = null;
		sharp = new Sobel();
		ImageProcessing imgPro = new ImageProcessing();
		sharp.decorate(imgPro);
		sharp.processing(s1, s2, "jpg");

		System.out.println(s2);
		return SUCCESS;

	}

	// 上传文件
	public String sharpening() throws Exception {
		// 获取CLASSPATH路径,得到项目下webroot/
		String realpath = ServletActionContext.getServletContext().getRealPath(
				"/images");
		// 创建file对象 realpath文件夹
		File dir = new File(realpath);
		if (!dir.exists()) {
			dir.mkdir();
		}
		if (uploadFileContentType.equals("image/jpeg")) {
			uploadFileContentType = ".jpg";
		} else if (uploadFileContentType.equals("image/png")) {
			uploadFileContentType = ".png";
		} else if (uploadFileContentType.equals("image/gif")) {
			uploadFileContentType = ".gif";
		} else if (uploadFileContentType.equals("image/bmp")) {
			uploadFileContentType = ".bmp";
		}

		System.out.println("原文件名：" + uploadFileName);
		// 重新命名uploadFileName
		uploadFileName = "image" + new Timestamp().getDateTime()
				+ uploadFileContentType;
		System.out.println("处理后文件名：" + uploadFileName);
		// 复制文件到指定新目录
		FileUtils.copyFile(uploadFile, new File(dir, uploadFileName));

		String s1 = realpath + "\\" + uploadFileName;
		String s2 = realpath + "\\" + "Test" + uploadFileName;
		BufferedImage img = ImageDigital.readImg(s1);
		// 提取图像像素值
		int w = img.getWidth();
		int h = img.getHeight();
		int[] pix = new int[w * h];
		img.getRGB(0, 0, w, h, pix, 0, w);
		pix = ImageDigital.grayImage(pix, w, h);
		//
		ImageProcessing imgPro = new ImageProcessing();
		ImageDecorator sobel = new Sobel();
		EdgeDetection ed = new EdgeDetection();
		ed.decorate(imgPro);
		sobel.decorate(sobel);
		ed.processing(pix, w, h);
		for (int i = 0; i < w * h; i++) {
			// 左移运算相当于除以2的xx次方，|为或运算，就是把四个八位二进制数（颜色的RGB三个值和透明度A）拼接成一个二进制数（32位）。前八位表示透明度，往后的三个八位分别代表RGB。有点像字符串相连。
			pix[i] = 255 << 24 | pix[i] << 16 | pix[i] << 8 | pix[i];
		}
		img.setRGB(0, 0, w, h, pix, 0, w);
		ImageDigital.writeImg(img, "jpg", s2);
		System.out.println(s2);
		return SUCCESS;

	}
}
