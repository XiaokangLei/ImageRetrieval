/**

 * 
 */
package image.transformation;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import javax.imageio.ImageIO;

/**
 * @author 小康
 * @date： 日期：Apr 17, 2017 时间：9:22:27 AM
 */
public class UniformLBP {

	public static void main(String[] args) {
		String src = "F:\\image processing\\测试图片素材\\测试图片3\\Timeages.jpg";
		String path = "D:\\Program Files\\Apache Software Foundation\\Tomcat 7.0\\me-webapps\\ImageRetrieval\\images\\Testpictures";
		int[] vector = getFeatureVector(src);
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
		ArrayList<String> strArrayName = new ArrayList<String>();
		ArrayList<String> strArraySimPath = new ArrayList<String>();
		System.out.println("该目录下对象个数：" + tempList.length);
		for (i = 0; i < tempList.length; i++) {
			if (tempList[i].isFile()) {
				// System.out.print("文     件：" + tempList[i]);
				strArrayName.add(tempList[i].toString().split("\\\\")[8]);
				strArray.add(tempList[i].toString());
				// System.out.println("name：" + strArrayName.get(i) + "\tpath："
				// + strArray.get(i));
				int[] vector1 = getFeatureVector(tempList[i].toString());
				samelist.add(calculateSimilarity(vector, vector1));
				map.put(samelist.get(i), tempList[i].toString());
			}
		}
		int k = 0;
		String[] samerateStrings = new String[200];
		NumberFormat fmt = NumberFormat.getPercentInstance();
		fmt.setMinimumFractionDigits(4);// 最多两位百分小数，如25.23%
		for (Double key : map.keySet()) {
			String value = map.get(key);
			strArraySimPath.add(map.get(key));
			samerateStrings[k] = fmt.format(key);
			System.out.println(samerateStrings[k] + " 均值纹理  " + value);

			k++;
		}

		// 根据遍历的key值依次输出前12个value值
		for (int j = 0; j < 12; j++) {
			System.out.println(strArraySimPath.get(j));
		}
	}

	public static int[] getFeatureVector(String imagePath) {
		// 获取灰度矩阵
		int[][] grayMatrix = getGrayPixel(imagePath, 200, 200);

		// 为特征值(0-255)分组(降维)
		int[] groupNums = groupFeatureValue();

		// 遍历像素点，计算其特征值，并确定其分组，并进行分组统计
		int[] vector = new int[59];
		for (int i = 1; i < grayMatrix.length - 1; i++) {
			for (int j = 1; j < grayMatrix[0].length - 1; j++) {
				int center = grayMatrix[i][j];
				int feature = 0;
				feature = grayMatrix[i - 1][j - 1] >= center ? (feature << 1) + 1
						: (feature << 1);
				feature = grayMatrix[i][j - 1] >= center ? (feature << 1) + 1
						: (feature << 1);
				feature = grayMatrix[i + 1][j - 1] >= center ? (feature << 1) + 1
						: (feature << 1);
				feature = grayMatrix[i + 1][j] >= center ? (feature << 1) + 1
						: (feature << 1);
				feature = grayMatrix[i + 1][j + 1] >= center ? (feature << 1) + 1
						: (feature << 1);
				feature = grayMatrix[i][j + 1] >= center ? (feature << 1) + 1
						: (feature << 1);
				feature = grayMatrix[i - 1][j + 1] >= center ? (feature << 1) + 1
						: (feature << 1);
				feature = grayMatrix[i - 1][j] >= center ? (feature << 1) + 1
						: (feature << 1);
				feature = getMinFeature(feature);
				vector[groupNums[feature]]++;
			}
		}

		return vector;
	}

	/**
	 * 对256个特征值进行分组<br>
	 * groupNums的下标为特征值，value为组号
	 * 
	 * @return
	 */
	private static int[] groupFeatureValue() {
		int[] groupNums = new int[256];
		int num = 1;
		for (int i = 0; i <= 255; i++) {
			if (getHopCount(i) <= 2) {
				groupNums[i] = num;
				num++;
			}
		}
		return groupNums;
	}

	/**
	 * 计算跳变次数
	 * 
	 * @param i
	 * @return
	 */
	private static int getHopCount(int i) {
		int[] a = new int[8];
		int cnt = 0;
		int k = 7;
		while (i > 0) {
			a[k] = i & 1;
			i = i >> 1;
			--k;
		}
		for (k = 0; k < 7; k++) {
			if (a[k] != a[k + 1]) {
				++cnt;
			}
		}
		if (a[0] != a[7]) {
			++cnt;
		}
		return cnt;
	}

	/**
	 * 旋转不变性<br>
	 * 此处的feature的二进制位数固定为8
	 * 
	 * @param feature
	 * @return
	 */
	private static int getMinFeature(int feature) {
		int minFeature = feature;
		for (int i = 0; i < 7; i++) {
			// 循环右移一位
			feature = (feature >> 1 | feature << 7) & 0xff;
			if (feature < minFeature)
				minFeature = feature;
		}

		return minFeature;
	}

	public static double calculateSimilarity(int[] vector, int[] vector1) {
		double len = 0, len1 = 0, numerator = 0;
		for (int i = 0; i < vector.length; i++) {
			len += Math.pow(vector[i], 2);
			len1 += Math.pow(vector1[i], 2);
			numerator += vector[i] * vector1[i];
		}
		len = Math.sqrt(len);
		len1 = Math.sqrt(len1);

		return numerator / (len * len1);
	}

	public static int[][] getGrayPixel(String imagePath, int width, int height) {
		BufferedImage bi = null;
		try {
			bi = resizeImage(imagePath, width, height,
					BufferedImage.TYPE_INT_RGB);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		int minx = bi.getMinX();
		int miny = bi.getMinY();
		int[][] matrix = new int[width - minx][height - miny];
		for (int i = minx; i < width; i++) {
			for (int j = miny; j < height; j++) {
				int pixel = bi.getRGB(i, j);
				int red = (pixel & 0xff0000) >> 16;
				int green = (pixel & 0xff00) >> 8;
				int blue = (pixel & 0xff);
				int gray = (int) (red * 0.3 + green * 0.59 + blue * 0.11);
				matrix[i][j] = gray;
			}
		}
		return matrix;
	}

	public static BufferedImage resizeImage(String srcImgPath, int width,
			int height, int imageType) throws IOException {
		File srcFile = new File(srcImgPath);
		BufferedImage srcImg = ImageIO.read(srcFile);
		BufferedImage buffImg = null;
		buffImg = new BufferedImage(width, height, imageType);
		buffImg.getGraphics().drawImage(
				srcImg.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0,
				0, null);
		return buffImg;
	}
}