package image.identification;

import image.ImageDigital;

import java.awt.image.BufferedImage;
import java.util.Arrays;

public class CannyEdgeFilter extends AbstractBufferedImageOp {
	private float gaussianKernelRadius = 2f;
	private int gaussianKernelWidth = 16;
	private float lowThreshold;
	private float highThreshold;
	// image width, height
	private int width;
	private int height;
	private float[] data;
	private float[] magnitudes;
	public double[] hist;
	public static int count = 0;

	public CannyEdgeFilter() {
		lowThreshold = 2.5f;
		highThreshold = 7.5f;
		gaussianKernelRadius = 2f;
		gaussianKernelWidth = 16;
	}

	public float getGaussianKernelRadius() {
		return gaussianKernelRadius;
	}

	public void setGaussianKernelRadius(float gaussianKernelRadius) {
		this.gaussianKernelRadius = gaussianKernelRadius;
	}

	public int getGaussianKernelWidth() {
		return gaussianKernelWidth;
	}

	public void setGaussianKernelWidth(int gaussianKernelWidth) {
		this.gaussianKernelWidth = gaussianKernelWidth;
	}

	public float getLowThreshold() {
		return lowThreshold;
	}

	public void setLowThreshold(float lowThreshold) {
		this.lowThreshold = lowThreshold;
	}

	public float getHighThreshold() {
		return highThreshold;
	}

	public void setHighThreshold(float highThreshold) {
		this.highThreshold = highThreshold;
	}

	public double[] filter(BufferedImage src) {
		width = src.getWidth();
		height = src.getHeight();
		// if (dest == null)
		// dest = createCompatibleDestImage(src, null);
		// 图像灰度化
		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		getRGB(src, 0, 0, width, height, inPixels);
		int index = 0;
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				ta = (inPixels[index] >> 24) & 0xff;
				tr = (inPixels[index] >> 16) & 0xff;
				tg = (inPixels[index] >> 8) & 0xff;
				tb = inPixels[index] & 0xff;
				int gray = (int) (0.299 * tr + 0.587 * tg + 0.114 * tb);
				inPixels[index] = (ta << 24) | (gray << 16) | (gray << 8)
						| gray;
			}
		}

		// 计算高斯卷积核
		float kernel[][] = new float[gaussianKernelWidth][gaussianKernelWidth];
		for (int x = 0; x < gaussianKernelWidth; x++) {
			for (int y = 0; y < gaussianKernelWidth; y++) {
				kernel[x][y] = gaussian(x, y, gaussianKernelRadius);
			}
		}
		// 高斯模糊 -灰度图像
		int krr = (int) gaussianKernelRadius;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				double weightSum = 0.0;
				double redSum = 0;
				for (int subRow = -krr; subRow <= krr; subRow++) {
					int nrow = row + subRow;
					if (nrow >= height || nrow < 0) {
						nrow = 0;
					}
					for (int subCol = -krr; subCol <= krr; subCol++) {
						int ncol = col + subCol;
						if (ncol >= width || ncol <= 0) {
							ncol = 0;
						}
						int index2 = nrow * width + ncol;
						int tr1 = (inPixels[index2] >> 16) & 0xff;
						redSum += tr1 * kernel[subRow + krr][subCol + krr];
						weightSum += kernel[subRow + krr][subCol + krr];
					}
				}
				int gray = (int) (redSum / weightSum);
				outPixels[index] = gray;
			}
		}

		// 计算梯度-gradient, X放与Y方向
		data = new float[width * height];

		magnitudes = new float[width * height];
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				// System.out.println(outPixels[index]);
				// 计算X方向梯度
				float xg = (getPixel(outPixels, width, height, col, row + 1)
						- getPixel(outPixels, width, height, col, row)
						+ getPixel(outPixels, width, height, col + 1, row + 1) - getPixel(
						outPixels, width, height, col + 1, row)) / 2.0f;
				float yg = (getPixel(outPixels, width, height, col, row)
						- getPixel(outPixels, width, height, col + 1, row)
						+ getPixel(outPixels, width, height, col, row + 1) - getPixel(
						outPixels, width, height, col + 1, row + 1)) / 2.0f;
				// 计算振幅与角度
				data[index] = hypot(xg, yg);
				if (xg == 0) {
					if (yg > 0) {
						magnitudes[index] = 90;
					}
					if (yg < 0) {
						magnitudes[index] = -90;
					}
				} else if (yg == 0) {
					magnitudes[index] = 0;
				} else {
					magnitudes[index] = (float) ((Math.atan(yg / xg) * 180) / Math.PI);
				}
				// make it 0 ~ 180
				magnitudes[index] += 90;
				// System.out.println(magnitudes[index]);
			}
		}

		// 非最大信号压制算法 3x3
		Arrays.fill(magnitudes, 0);
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				float angle = magnitudes[index];
				float m0 = data[index];
				magnitudes[index] = m0;
				if (angle >= 0 && angle < 22.5) // angle 0
				{
					float m1 = getPixel(data, width, height, col - 1, row);
					float m2 = getPixel(data, width, height, col + 1, row);
					if (m0 < m1 || m0 < m2) {
						magnitudes[index] = 0;
					}
				} else if (angle >= 22.5 && angle < 67.5) // angle +45
				{
					float m1 = getPixel(data, width, height, col + 1, row - 1);
					float m2 = getPixel(data, width, height, col - 1, row + 1);
					if (m0 < m1 || m0 < m2) {
						magnitudes[index] = 0;
					}
				} else if (angle >= 67.5 && angle < 112.5) // angle 90
				{
					float m1 = getPixel(data, width, height, col, row + 1);
					float m2 = getPixel(data, width, height, col, row - 1);
					if (m0 < m1 || m0 < m2) {
						magnitudes[index] = 0;
					}
				} else if (angle >= 112.5 && angle < 157.5) // angle 135 / -45
				{
					float m1 = getPixel(data, width, height, col - 1, row - 1);
					float m2 = getPixel(data, width, height, col + 1, row + 1);
					if (m0 < m1 || m0 < m2) {
						magnitudes[index] = 0;
					}
				} else if (angle >= 157.5) // angle 0
				{
					float m1 = getPixel(data, width, height, col, row + 1);
					float m2 = getPixel(data, width, height, col, row - 1);
					if (m0 < m1 || m0 < m2) {
						magnitudes[index] = 0;
					}
				}
			}
		}
		// 寻找最大与最小值
		float min = 255;
		float max = 0;
		for (int i = 0; i < magnitudes.length; i++) {
			if (magnitudes[i] == 0)
				continue;
			min = Math.min(min, magnitudes[i]);
			max = Math.max(max, magnitudes[i]);
		}
		// System.out.println("Image Max Gradient = " + max + " Mix Gradient = "
		// + min);

		// 通常比值为 TL : TH = 1 : 3， 根据两个阈值完成二值化边缘连接
		// 边缘连接-link edges
		Arrays.fill(data, 0);
		int offset = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (magnitudes[offset] >= highThreshold && data[offset] == 0) {
					edgeLink(col, row, offset, lowThreshold);
				}
				offset++;
			}
		}

		// 二值化显示并计算边缘直方图
		hist = new double[180];
		for (int i = 0; i < inPixels.length; i++) {
			int gray = clamp((int) data[i]);
			// System.out.println(gray);
			outPixels[i] = gray > 0 ? -1 : 0xff000000;
			if (outPixels[i] == -1) {
				// System.out.println(magnitudes[i]);
				for (int j = 0; j < 180; j++) {
					if ((int) (magnitudes[i]) == j)
						hist[j]++;
				}
				count++;
			}
		}
		// int k = 1;
		double[] tephist = new double[182];
		for (int j = 0; j < 180; j++) {
			hist[j] = hist[j] / count;
			tephist[j] = hist[j];
			// System.out.println(j + "---" + hist[j]);
		}

		hist[0] = (tephist[178] + tephist[2]) * (1.f / 16.f)
				+ (tephist[179] + tephist[1]) * (4.f / 16.f) + tephist[0]
				* (6.f / 16.f);
		hist[1] = (tephist[179] + tephist[3]) * (1.f / 16.f)
				+ (tephist[0] + tephist[2]) * (4.f / 16.f) + tephist[1]
				* (6.f / 16.f);
		hist[178] = (tephist[176] + tephist[0]) * (1.f / 16.f)
				+ (tephist[177] + tephist[179]) * (4.f / 16.f) + tephist[178]
				* (6.f / 16.f);
		hist[179] = (tephist[177] + tephist[1]) * (1.f / 16.f)
				+ (tephist[178] + tephist[0]) * (4.f / 16.f) + tephist[179]
				* (6.f / 16.f);
		for (int i = 2; i < 178; i++) {
			hist[i] = (tephist[i - 2] + tephist[i + 2]) * (1.f / 16.f)
					+ (tephist[i - 1] + tephist[i + 1]) * (4.f / 16.f)
					+ tephist[i] * (6.f / 16.f);
		}
		// System.out.println(count);

		// String destPath =
		// "D:\\Program Files\\apache-tomcat-7.0.77\\me-webapps\\ImageRetrieval\\images\\testeee.gif";
		// BufferedImage dest = ImageDigital.readImg(destPath);
		// setRGB(dest, 0, 0, width, height, outPixels);

		// ImageDigital.writeImg(dest, "jpg", destPath);

		return hist;
	}

	// 欧式距离求图片的相似度
	public double GetSimilarity(String srcPath, String destPath) {
		BufferedImage srcImg = ImageDigital.readImg(srcPath);
		BufferedImage destImg = ImageDigital.readImg(destPath);
		return GetSimilarity(srcImg, destImg);
	}

	public double GetSimilarity(BufferedImage srcImg, BufferedImage destImg) {
		double[] histR = filter(srcImg);
		double[] histD = filter(destImg);
		return GetSimilarity(histR, histD);
	}

	public static double GetSimilarity(double[] histR, double[] histD) {
		double similar = (double) 0.0;// 相似度
		for (int j = 0; j < histR.length; j++) {
			similar += (histR[j] - histD[j]) * (histR[j] - histD[j]);
		}
		similar = similar / 6;
		similar = Math.sqrt(similar);
		// similar=similar/3;
		// System.out.println(similar);
		return similar;
	}

	public static double GetSimilarity1(double[] histR, String str2) {
		double p = 0.0;
		// double[] histR = HistogramIdentification.getHistgram(str1);
		double[] histD = null;
		BufferedImage src1 = ImageDigital.readImg(str2);
		CannyEdgeFilter test2 = new CannyEdgeFilter();
		histD = test2.filter(src1);
		p = GetSimilarity(histR, histD);
		// System.out.println("   相似度指数--" + p);
		return p;
	}

	public static int clamp(int value) {
		return value > 255 ? 255 : (value < 0 ? 0 : value);
	}

	private void edgeLink(int x1, int y1, int index, float threshold) {
		int x0 = (x1 == 0) ? x1 : x1 - 1;
		int x2 = (x1 == width - 1) ? x1 : x1 + 1;
		int y0 = y1 == 0 ? y1 : y1 - 1;
		int y2 = y1 == height - 1 ? y1 : y1 + 1;

		data[index] = magnitudes[index];
		for (int x = x0; x <= x2; x++) {
			for (int y = y0; y <= y2; y++) {
				int i2 = x + y * width;
				if ((y != y1 || x != x1) && data[i2] == 0
						&& magnitudes[i2] >= threshold) {
					edgeLink(x, y, i2, threshold);
					return;
				}
			}
		}
	}

	private static float getPixel(float[] input, int width, int height,
			int col, int row) {
		if (col < 0 || col >= width)
			col = 0;
		if (row < 0 || row >= height)
			row = 0;
		int index = row * width + col;
		return input[index];
	}

	private static float hypot(float x, float y) {
		return (float) Math.hypot(x, y);
	}

	private static int getPixel(int[] inPixels, int width, int height, int col,
			int row) {
		if (col < 0 || col >= width)
			col = 0;
		if (row < 0 || row >= height)
			row = 0;
		int index = row * width + col;
		return inPixels[index];
	}

	private static float gaussian(float x, float y, float sigma) {
		float xxDistance = x * x;
		float yyDistance = y * y;
		float sigma22 = 2 * sigma * sigma;
		float sigma22PI = (float) Math.PI * sigma22;
		return (float) Math.exp(-(xxDistance + yyDistance) / sigma22)
				/ sigma22PI;
	}

	public static void main(String args[]) {
		String srcPath = "D:\\Program Files\\apache-tomcat-7.0.77\\me-webapps\\ImageRetrieval\\images\\image.jpg";
		String srcPath1 = "D:\\Program Files\\apache-tomcat-7.0.77\\me-webapps\\ImageRetrieval\\images\\image1.jpg";
		BufferedImage img1 = ImageDigital.readImg(srcPath);
		BufferedImage img2 = ImageDigital.readImg(srcPath1);
		CannyEdgeFilter test = new CannyEdgeFilter();
		test.filter(img1);
		CannyEdgeFilter test1 = new CannyEdgeFilter();
		test1.filter(img1, img2);

		/*
		 * String src =
		 * "D:\\Program Files\\apache-tomcat-7.0.77\\me-webapps\\ImageRetrieval\\images\\Testpictures\\apple-13.gif"
		 * ; String path =
		 * "D:\\Program Files\\apache-tomcat-7.0.77\\me-webapps\\ImageRetrieval\\images\\Testpictures"
		 * ; File file = new File(path); int i = 0; ArrayList<String> strArray =
		 * new ArrayList<String>(); ArrayList<String> strArray2 = new
		 * ArrayList<String>(); // map为[相识度-图片路径] TreeMap<Double, String> map2 =
		 * new TreeMap<Double, String>(); File[] tempList = file.listFiles(); //
		 * 相似度samlist List<Double> samelist2 = new ArrayList<Double>();
		 * 
		 * // 图像库中的图像信息 ArrayList<String> strArrayName = new
		 * ArrayList<String>(); // 实例化UserDao对象 // PictureDataDao picturesDao =
		 * new PictureDataDao(); System.out.println("该目录下对象个数：" +
		 * tempList.length); BufferedImage src1 = ImageDigital.readImg(src);
		 * CannyEdgeFilter test1 = new CannyEdgeFilter(); double[] histR =
		 * test1.filter(src1); // System.out.println(histR[0]); for (i = 0; i <
		 * tempList.length; i++) { if (tempList[i].isFile()) { //
		 * System.out.print("文     件：" + tempList[i]);
		 * strArrayName.add(tempList[i].toString().split("\\\\")[7]);
		 * strArray.add(tempList[i].toString());
		 * 
		 * System.out.println("name：" + strArrayName.get(i) + "\tpath：" +
		 * strArray.get(i)); samelist2.add(GetSimilarity1(histR,
		 * tempList[i].toString())); map2.put(samelist2.get(i),
		 * tempList[i].toString()); } } int k2 = 0; String[] samerateStrings2 =
		 * new String[2000]; NumberFormat fmt =
		 * NumberFormat.getPercentInstance(); fmt.setMinimumFractionDigits(4);//
		 * 最多两位百分小数，如25.23% for (Double key2 : map2.keySet()) { String value =
		 * map2.get(key2); samerateStrings2[k2] = fmt.format(1 - key2);
		 * System.out.println(samerateStrings2[k2] + "形状--一维  " + value); k2++;
		 * }
		 * 
		 * Set<Double> key3 = map2.keySet(); // 输出key值 //
		 * System.out.println(map.keySet()); // 遍历key值 Iterator<Double> int3 =
		 * key3.iterator();
		 * 
		 * // 根据遍历的key值依次输出前三个value值 for (int j = 0; j < 12; j++) {
		 * strArray2.add(map2.get(int3.next())); //
		 * strArrayName.add(strArray.get(j).split("\\\\")[7]);
		 * System.out.println(strArray2.get(j)); }
		 */
		return;
	}

	@Override
	public BufferedImage filter(BufferedImage src, BufferedImage dest) {
		width = src.getWidth();
		height = src.getHeight();
		if (dest == null)
			dest = createCompatibleDestImage(src, null);
		// 图像灰度化
		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		getRGB(src, 0, 0, width, height, inPixels);
		int index = 0;
		for (int row = 0; row < height; row++) {
			int ta = 0, tr = 0, tg = 0, tb = 0;
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				ta = (inPixels[index] >> 24) & 0xff;
				tr = (inPixels[index] >> 16) & 0xff;
				tg = (inPixels[index] >> 8) & 0xff;
				tb = inPixels[index] & 0xff;
				int gray = (int) (0.299 * tr + 0.587 * tg + 0.114 * tb);
				inPixels[index] = (ta << 24) | (gray << 16) | (gray << 8)
						| gray;
			}
		}

		// 计算高斯卷积核
		float kernel[][] = new float[gaussianKernelWidth][gaussianKernelWidth];
		for (int x = 0; x < gaussianKernelWidth; x++) {
			for (int y = 0; y < gaussianKernelWidth; y++) {
				kernel[x][y] = gaussian(x, y, gaussianKernelRadius);
			}
		}
		// 高斯模糊 -灰度图像
		int krr = (int) gaussianKernelRadius;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				double weightSum = 0.0;
				double redSum = 0;
				for (int subRow = -krr; subRow <= krr; subRow++) {
					int nrow = row + subRow;
					if (nrow >= height || nrow < 0) {
						nrow = 0;
					}
					for (int subCol = -krr; subCol <= krr; subCol++) {
						int ncol = col + subCol;
						if (ncol >= width || ncol <= 0) {
							ncol = 0;
						}
						int index2 = nrow * width + ncol;
						int tr1 = (inPixels[index2] >> 16) & 0xff;
						redSum += tr1 * kernel[subRow + krr][subCol + krr];
						weightSum += kernel[subRow + krr][subCol + krr];
					}
				}
				int gray = (int) (redSum / weightSum);
				outPixels[index] = gray;
			}
		}

		// 计算梯度-gradient, X放与Y方向
		data = new float[width * height];
		magnitudes = new float[width * height];
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				// 计算X方向梯度
				float xg = (getPixel(outPixels, width, height, col, row + 1)
						- getPixel(outPixels, width, height, col, row)
						+ getPixel(outPixels, width, height, col + 1, row + 1) - getPixel(
						outPixels, width, height, col + 1, row)) / 2.0f;
				float yg = (getPixel(outPixels, width, height, col, row)
						- getPixel(outPixels, width, height, col + 1, row)
						+ getPixel(outPixels, width, height, col, row + 1) - getPixel(
						outPixels, width, height, col + 1, row + 1)) / 2.0f;
				// 计算振幅与角度
				data[index] = hypot(xg, yg);
				if (xg == 0) {
					if (yg > 0) {
						magnitudes[index] = 90;
					}
					if (yg < 0) {
						magnitudes[index] = -90;
					}
				} else if (yg == 0) {
					magnitudes[index] = 0;
				} else {
					magnitudes[index] = (float) ((Math.atan(yg / xg) * 180) / Math.PI);
				}
				// make it 0 ~ 180
				magnitudes[index] += 90;
			}
		}

		// 非最大信号压制算法 3x3
		Arrays.fill(magnitudes, 0);
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				index = row * width + col;
				float angle = magnitudes[index];
				float m0 = data[index];
				magnitudes[index] = m0;
				if (angle >= 0 && angle < 22.5) // angle 0
				{
					float m1 = getPixel(data, width, height, col - 1, row);
					float m2 = getPixel(data, width, height, col + 1, row);
					if (m0 < m1 || m0 < m2) {
						magnitudes[index] = 0;
					}
				} else if (angle >= 22.5 && angle < 67.5) // angle +45
				{
					float m1 = getPixel(data, width, height, col + 1, row - 1);
					float m2 = getPixel(data, width, height, col - 1, row + 1);
					if (m0 < m1 || m0 < m2) {
						magnitudes[index] = 0;
					}
				} else if (angle >= 67.5 && angle < 112.5) // angle 90
				{
					float m1 = getPixel(data, width, height, col, row + 1);
					float m2 = getPixel(data, width, height, col, row - 1);
					if (m0 < m1 || m0 < m2) {
						magnitudes[index] = 0;
					}
				} else if (angle >= 112.5 && angle < 157.5) // angle 135 / -45
				{
					float m1 = getPixel(data, width, height, col - 1, row - 1);
					float m2 = getPixel(data, width, height, col + 1, row + 1);
					if (m0 < m1 || m0 < m2) {
						magnitudes[index] = 0;
					}
				} else if (angle >= 157.5) // angle 0
				{
					float m1 = getPixel(data, width, height, col, row + 1);
					float m2 = getPixel(data, width, height, col, row - 1);
					if (m0 < m1 || m0 < m2) {
						magnitudes[index] = 0;
					}
				}
			}
		}
		// 寻找最大与最小值
		float min = 255;
		float max = 0;
		for (int i = 0; i < magnitudes.length; i++) {
			if (magnitudes[i] == 0)
				continue;
			min = Math.min(min, magnitudes[i]);
			max = Math.max(max, magnitudes[i]);
		}
		System.out.println("Image Max Gradient = " + max + " Mix Gradient = "
				+ min);

		// 通常比值为 TL : TH = 1 : 3， 根据两个阈值完成二值化边缘连接
		// 边缘连接-link edges
		Arrays.fill(data, 0);
		int offset = 0;
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				if (magnitudes[offset] >= highThreshold && data[offset] == 0) {
					edgeLink(col, row, offset, lowThreshold);
				}
				offset++;
			}
		}

		// 二值化显示
		for (int i = 0; i < inPixels.length; i++) {
			int gray = clamp((int) data[i]);
			outPixels[i] = gray > 0 ? -1 : 0xff000000;
		}
		setRGB(dest, 0, 0, width, height, outPixels);
		String destPath = "D:\\Program Files\\apache-tomcat-7.0.77\\me-webapps\\ImageRetrieval\\images\\testeee.jpg";
		ImageDigital.writeImg(dest, "jpg", destPath);

		return dest;
	}

}