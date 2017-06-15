/**
 * 
 */
package image.identification;

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
 * @date： 日期：Apr 17, 2017 时间：1:50:57 PM
 */
public class PHashTest {
	public static void main(String[] args) {
		String src = "F:\\image processing\\测试图片素材\\测试图片3\\Timeages.jpg";
		String path = "D:\\Program Files\\Apache Software Foundation\\Tomcat 7.0\\me-webapps\\ImageRetrieval\\images\\Testpictures";
		String hash = getFeatureValue(src);
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
				String hash1 = getFeatureValue(tempList[i].toString());
				samelist.add(calculateSimilarity(hash, hash1));
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
			System.out.println(samerateStrings[k] + " pHash  " + value);

			k++;
		}

		// 根据遍历的key值依次输出前12个value值
		for (int j = 0; j < 12; j++) {
			System.out.println(strArraySimPath.get(j));
		}
	}

	public static String getFeatureValue(String imagePath) {
		// 缩小尺寸，简化色彩
		int[][] grayMatrix = getGrayPixel(imagePath, 32, 32);
		// 计算DCT
		grayMatrix = DCT(grayMatrix, 32);
		// 缩小DCT，计算平均值
		int[][] newMatrix = new int[8][8];
		double average = 0;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				newMatrix[i][j] = grayMatrix[i][j];
				average += grayMatrix[i][j];
			}
		}
		average /= 64.0;
		// 计算hash值
		String hash = "";
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if (newMatrix[i][j] < average) {
					hash += '0';
				} else {
					hash += '1';
				}
			}
		}
		return hash;
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

	/**
	 * 用于计算pHash的相似度<br>
	 * 相似度为1时，图片最相似
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public static double calculateSimilarity(String str1, String str2) {
		int num = 0;
		for (int i = 0; i < 64; i++) {
			if (str1.charAt(i) == str2.charAt(i)) {
				num++;
			}
		}
		return ((double) num) / 64.0;
	}

	/**
	 * 离散余弦变换
	 * 
	 * @author
	 * 
	 * @param pix
	 *            原图像的数据矩阵
	 * @param n
	 *            原图像(n*n)的高或宽
	 * @return 变换后的矩阵数组
	 */
	public static int[][] DCT(int[][] pix, int n) {
		double[][] iMatrix = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				iMatrix[i][j] = (double) (pix[i][j]);
			}
		}
		double[][] quotient = coefficient(n); // 求系数矩阵
		double[][] quotientT = transposingMatrix(quotient, n); // 转置系数矩阵

		double[][] temp = new double[n][n];
		temp = matrixMultiply(quotient, iMatrix, n);
		iMatrix = matrixMultiply(temp, quotientT, n);

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				pix[i][j] = (int) (iMatrix[i][j]);
			}
		}
		return pix;
	}

	/**
	 * 求离散余弦变换的系数矩阵
	 * 
	 * @author
	 * 
	 * @param n
	 *            n*n矩阵的大小
	 * @return 系数矩阵
	 */
	private static double[][] coefficient(int n) {
		double[][] coeff = new double[n][n];
		double sqrt = 1.0 / Math.sqrt(n);
		for (int i = 0; i < n; i++) {
			coeff[0][i] = sqrt;
		}
		for (int i = 1; i < n; i++) {
			for (int j = 0; j < n; j++) {
				coeff[i][j] = Math.sqrt(2.0 / n)
						* Math.cos(i * Math.PI * (j + 0.5) / (double) n);
			}
		}
		return coeff;
	}

	/**
	 * 矩阵转置
	 * 
	 * @author
	 * 
	 * @param matrix
	 *            原矩阵
	 * @param n
	 *            矩阵(n*n)的高或宽
	 * @return 转置后的矩阵
	 */
	private static double[][] transposingMatrix(double[][] matrix, int n) {
		double nMatrix[][] = new double[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				nMatrix[i][j] = matrix[j][i];
			}
		}
		return nMatrix;
	}

	/**
	 * 矩阵相乘
	 * 
	 * @author
	 * 
	 * @param A
	 *            矩阵A
	 * @param B
	 *            矩阵B
	 * @param n
	 *            矩阵的大小n*n
	 * @return 结果矩阵
	 */
	private static double[][] matrixMultiply(double[][] A, double[][] B, int n) {
		double nMatrix[][] = new double[n][n];
		int t = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				t = 0;
				for (int k = 0; k < n; k++) {
					t += A[i][k] * B[k][j];
				}
				nMatrix[i][j] = t;
			}
		}
		return nMatrix;
	}
}
