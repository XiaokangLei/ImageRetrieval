/**
 * 
 */
package image.identification;

import java.awt.Color;
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
 * @date： 日期：Apr 17, 2017 时间：2:33:04 PM
 * @category 该颜色直方图更接近于人类对图片的识别程度
 */
public class HSVColorHistogram {

	public static void main(String[] args) {
		String src = "F:\\image processing\\测试图片素材\\测试图片3\\Timeages.jpg";
		String path = "D:\\Program Files\\Apache Software Foundation\\Tomcat 7.0\\me-webapps\\ImageRetrieval\\images\\Testpictures";
		Pixel[][] matrix = getImagePixel(src, 200, 200);
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
				// String hash1 = getFeatureValue(tempList[i].toString());
				Pixel[][] matrix1 = getImagePixel(tempList[i].toString(), 200,
						200);
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
				samelist.add(calculateSimilarity(vector, vector1));
				map.put(samelist.get(i), tempList[i].toString());
			}
		}
		int k = 0;
		String[] samerateStrings = new String[1000];
		NumberFormat fmt = NumberFormat.getPercentInstance();
		fmt.setMinimumFractionDigits(4);// 最多两位百分小数，如25.23%
		for (Double key : map.keySet()) {
			String value = map.get(key);
			strArraySimPath.add(map.get(key));
			samerateStrings[k] = fmt.format(key);
			System.out.println(samerateStrings[k] + " HSV  " + value);

			k++;
		}

		// 根据遍历的key值依次输出前12个value值
		for (int j = 0; j < 12; j++) {
			System.out.println(strArraySimPath.get(j));
		}

		// System.out.println(calculateSimilarity(vector, vector1));
	}

	public static Pixel[][] getImagePixel(String imagePath, int width,
			int height) {
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
		Pixel[][] rgbMatrix = new Pixel[width - minx][height - miny];
		for (int i = minx; i < width; i++) {
			for (int j = miny; j < height; j++) {
				int pixel = bi.getRGB(i, j);
				int red = (pixel & 0xff0000) >> 16;
				int green = (pixel & 0xff00) >> 8;
				int blue = (pixel & 0xff);
				Pixel p = new Pixel();
				p.red = red;
				p.green = green;
				p.blue = blue;
				rgbMatrix[i - minx][j - miny] = p;
			}
		}
		return rgbMatrix;
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

	public static int[] matrix2vector(int[][] matrix) {
		if (matrix.length <= 0 || matrix[0].length <= 0) {
			return null;
		}
		int[] vector = new int[matrix.length * matrix[0].length];
		int index = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++, index++) {
				vector[index] = matrix[i][j];
			}
		}
		return vector;
	}
}
