package image.identification;

import image.ImageDigital;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import com.dao.ConnectDB;
import com.user.PicturesData;

//import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;
/**
 * 
 * @author 
 *
 */
public class HistogramIdentification implements Identification {
	/**
	 * 表示R、G、B的位数
	 */
	public static final int GRAYBIT = 4;
	static ArrayList<String> Hisdata = new ArrayList<String>();
	static int count = 0;

	public HistogramIdentification() {

	}

	public static double[] getHistgram(String srcPath) {
		BufferedImage img = ImageDigital.readImg(srcPath);
		return getHistogram(img);
	}

	/**
	 * 求一维的灰度直方图
	 * 
	 * @param img
	 * @return
	 */
	public static double[] getHistogram(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		int series = (int) Math.pow(2, GRAYBIT); // GRAYBIT=4;用12位的int表示灰度值，前4位表示red,中间4们表示green,后面4位表示blue
		int greyScope = 256 / series;
		double[] hist = new double[series * series * series];
		int r, g, b, index;
		int pix[] = new int[w * h];
		pix = img.getRGB(0, 0, w, h, pix, 0, w);
		for (int i = 0; i < w * h; i++) {
			r = pix[i] >> 16 & 0xff;
			r = r / greyScope;
			g = pix[i] >> 8 & 0xff;
			g = g / greyScope;
			b = pix[i] & 0xff;
			b = b / greyScope;
			index = r << (2 * GRAYBIT) | g << GRAYBIT | b;
			hist[index]++;

		}
		// StringBuilder strBuilder = new StringBuilder();
		for (int i = 0; i < hist.length; i++) {
			// sSystem.out.println(hist[i]);
			hist[i] = hist[i] / (w * h);
			// strBuilder.append(hist[i]);
			// if (!(i == hist.length - 1))
			// strBuilder.append(",");
			// System.out.println("长度：" + hist.length + hist[i] + "  ");
		}
		// Hisdata.add(strBuilder.toString());
		return hist;
	}

	public static ArrayList<PicturesData> getPicDatabase() {
		// 获取数据库连接Connection对象
		Connection conn = ConnectDB.getConnection();
		String sql = "select * from tb_picturesdata";
		ArrayList<PicturesData> Inf = new ArrayList<PicturesData>();
		try {
			// 获取PreparedStatement对象
			PreparedStatement ps = conn.prepareStatement(sql);
			// 执行查询获取结果集
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				PicturesData datainf = new PicturesData();
				datainf.setName(rs.getString("name"));
				datainf.setAddress(rs.getString("address"));
				datainf.setHistogramdata(rs.getString("histogramdata"));
				Inf.add(datainf);
				count++;
			}
			// 释放此 ResultSet 对象的数据库和 JDBC 资源
			rs.close();
			// 释放此 PreparedStatement 对象的数据库和 JDBC 资源
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return Inf;
	}

	public static double[] getPicDatabase1(int k, ArrayList<PicturesData> Inf) {
		// = getPicDatabase();
		double[] histD = new double[Inf.get(k).getHistogramdata().split(",").length];
		// System.out.println(Inf.get(k).getHistogramdata().split(",").length);
		for (int i = 0; i < Inf.get(k).getHistogramdata().split(",").length; i++) {
			histD[i] = Double.valueOf(
					Inf.get(k).getHistogramdata().split(",")[i]).doubleValue();
		}
		// System.out.println(Inf.size() + "--" + hisR[0]);
		return histD;
	}

	public double indentification(String srcPath, String destPath) {
		BufferedImage srcImg = ImageDigital.readImg(srcPath);
		BufferedImage destImg = ImageDigital.readImg(destPath);
		return indentification(srcImg, destImg);
	}

	public double indentification(BufferedImage srcImg, BufferedImage destImg) {
		double[] histR = getHistogram(srcImg);
		double[] histD = getHistogram(destImg);
		return indentification(histR, histD);
	}

	public static double indentification(double[] histR, double[] histD) {
		double p = (double) 0.0;
		for (int i = 0; i < histR.length; i++) {
			// System.out.println(histR[i] + "--" + histD[i]);
			p += Math.sqrt(histR[i] * histD[i]);
		}
		return p;
	}

	/**
	 * 用一维直方图求图像的相似度
	 * 
	 * @param n
	 * @param str1
	 * @param str2
	 */
	public static double histogramIditification(double[] histR, String str2) {
		double p = 0;
		// double[] histR = HistogramIdentification.getHistgram(str1);
		double[] histD = null;
		histD = HistogramIdentification.getHistgram(str2);
		p = HistogramIdentification.indentification(histR, histD);
		// System.out.println("   相似度指数--" + p);
		return p;
	}

	public static double histogramIditificationTest(double[] histR,
			double[] histD) {
		double p = 0;
		p = HistogramIdentification.indentification(histR, histD);
		// System.out.println("   相似度指数--" + p);
		return p;
	}

	// 欧式距离求图片的相似度
	public double GetSimilarity(String srcPath, String destPath) {
		BufferedImage srcImg = ImageDigital.readImg(srcPath);
		BufferedImage destImg = ImageDigital.readImg(destPath);
		return GetSimilarity(srcImg, destImg);
	}

	public double GetSimilarity(BufferedImage srcImg, BufferedImage destImg) {
		double[] histR = getHistogram(srcImg);
		double[] histD = getHistogram(destImg);
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
		double p = 0;
		// double[] histR = HistogramIdentification.getHistgram(str1);
		double[] histD = null;
		histD = HistogramIdentification.getHistgram(str2);
		p = HistogramIdentification.GetSimilarity(histR, histD);
		// System.out.println("   相似度指数--" + p);
		return p;
	}

	/**
	 * 求三维的灰度直方图
	 * 
	 * @param srcPath
	 * @return
	 */
	public static double[][] getHistgram2(String srcPath) {
		BufferedImage img = ImageDigital.readImg(srcPath);
		return getHistogram2(img);
	}

	/**
	 * hist[0][]red的直方图，hist[1][]green的直方图，hist[2][]blue的直方图
	 * 
	 * @param img
	 *            要获取直方图的图像
	 * @return 返回r,g,b的三维直方图
	 */
	public static double[][] getHistogram2(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		double[][] hist = new double[3][256];
		int r, g, b;
		int pix[] = new int[w * h];
		pix = img.getRGB(0, 0, w, h, pix, 0, w);
		for (int i = 0; i < w * h; i++) {
			r = pix[i] >> 16 & 0xff;
			g = pix[i] >> 8 & 0xff;
			b = pix[i] & 0xff;
			/*
			 * hr[r] ++; hg[g] ++; hb[b] ++;
			 */
			hist[0][r]++;
			hist[1][g]++;
			hist[2][b]++;
		}
		for (int j = 0; j < 256; j++) {
			for (int i = 0; i < 3; i++) {
				hist[i][j] = hist[i][j] / (w * h);
				// System.out.println("三维长度：" + 3 * 256 + hist[i][j] + "  ");
			}
		}
		return hist;
	}

	public double indentification2(String srcPath, String destPath) {
		BufferedImage srcImg = ImageDigital.readImg(srcPath);
		BufferedImage destImg = ImageDigital.readImg(destPath);
		return indentification(srcImg, destImg);
	}

	public double indentification2(BufferedImage srcImg, BufferedImage destImg) {
		double[][] histR = getHistogram2(srcImg);
		double[][] histD = getHistogram2(destImg);
		return indentification2(histR, histD);
	}

	public static double indentification2(double[][] histR, double[][] histD) {
		double p = (double) 0.0;
		for (int i = 0; i < histR.length; i++) {
			for (int j = 0; j < histR[0].length; j++) {
				p += Math.sqrt(histR[i][j] * histD[i][j]);
			}
		}
		return p / 3;
	}

	// 欧式距离求图片的相似度
	public double GetSimilarity2(String srcPath, String destPath) {
		BufferedImage srcImg = ImageDigital.readImg(srcPath);
		BufferedImage destImg = ImageDigital.readImg(destPath);
		return indentification(srcImg, destImg);
	}

	public double GetSimilarity2(BufferedImage srcImg, BufferedImage destImg) {
		double[][] histR = getHistogram2(srcImg);
		double[][] histD = getHistogram2(destImg);
		return GetSimilarity2(histR, histD);
	}

	public static double GetSimilarity2(double[][] histR, double[][] histD) {
		double similar = (double) 0.0;// 相似度
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < histR[i].length; j++) {
				similar += (histR[i][j] - histD[i][j])
						* (histR[i][j] - histD[i][j]);
			}
		}
		similar = similar / 6;
		similar = Math.sqrt(similar);
		// similar=similar/3;
		// System.out.println(similar);
		return similar;
	}

	public static double GetSimilarity3(double[][] histR, String str2) {
		double p = 0;
		// double[][] histR = HistogramIdentification.getHistgram2(str1);
		double[][] histD = null;
		histD = HistogramIdentification.getHistgram2(str2);
		p = HistogramIdentification.GetSimilarity2(histR, histD);
		// System.out.println("   相似度指数--" + p);
		return p;
	}

	/**
	 * 用三维灰度直方图求图像的相似度
	 * 
	 * @param n
	 * @param str1
	 * @param str2
	 */
	public static double histogramIditification2(String str1, String str2) {
		double p = 0;
		double[][] histR = HistogramIdentification.getHistgram2(str1);
		double[][] histD = null;
		histD = HistogramIdentification.getHistgram2(str2);
		p = HistogramIdentification.indentification2(histR, histD);
		// System.out.println("   直方图信息--" + histR);
		return p;
	}

	public static void main(String args[]) {
		String src = "D:\\Program Files\\apache-tomcat-7.0.77\\me-webapps\\ImageRetrieval\\images\\apple-13.gif";
		String path = "D:\\Program Files\\apache-tomcat-7.0.77\\me-webapps\\ImageRetrieval\\images\\Testpictures";
		File file = new File(path);
		int i = 0;
		ArrayList<String> strArray = new ArrayList<String>();
		ArrayList<String> strArray1 = new ArrayList<String>();
		ArrayList<String> strArray2 = new ArrayList<String>();
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
		TreeMap<Double, String> map1 = new TreeMap<Double, String>();
		TreeMap<Double, String> map2 = new TreeMap<Double, String>();
		File[] tempList = file.listFiles();
		// 相似度samlist
		List<Double> samelist = new ArrayList<Double>();
		List<Double> samelist1 = new ArrayList<Double>();
		List<Double> samelist2 = new ArrayList<Double>();
		// List<Double> samelistTest = new ArrayList<Double>();
		// 必须先执行一下获取数据库中的信息
		// ArrayList<PicturesData> Inf = getPicDatabase();
		// double[] histR = HistogramIdentification.getHistgram(src);
		// double[] histD = null;
		// System.out.println(count);
		// for (int m = 0; m < count; m++) {
		// histD = getPicDatabase1(m, Inf);
		// System.out.println(histD[0]);
		// samelistTest.add(histogramIditificationTest(histR, histD));
		// System.out.println(samelistTest.get(m));
		// }

		// 图像库中的图像信息
		ArrayList<String> strArrayName = new ArrayList<String>();
		// 实例化UserDao对象
		// PictureDataDao picturesDao = new PictureDataDao();
		System.out.println("该目录下对象个数：" + tempList.length);
		double[] histR = HistogramIdentification.getHistgram(src);
		// System.out.println(histR[0]);
		double[][] histR3 = HistogramIdentification.getHistgram2(src);
		for (i = 0; i < tempList.length; i++) {
			if (tempList[i].isFile()) {
				// System.out.print("文     件：" + tempList[i]);
				strArrayName.add(tempList[i].toString().split("\\\\")[7]);
				strArray.add(tempList[i].toString());

				System.out.println("name：" + strArrayName.get(i) + "\tpath："
						+ strArray.get(i));
				samelist.add(histogramIditification(histR,
						tempList[i].toString()));
				samelist1.add(GetSimilarity3(histR3, tempList[i].toString()));
				samelist2.add(GetSimilarity1(histR, tempList[i].toString()));
				/*
				 * if (strArrayName.get(i) != null &&
				 * !strArrayName.get(i).isEmpty()) { if
				 * (picturesDao.userIsExist(strArrayName.get(i))) { //
				 * 实例化一个User对象PicturesData PicturesData data = new
				 * PicturesData(); // 对用户对象中的属性赋值
				 * data.setName(strArrayName.get(i));
				 * data.setAddress(strArray.get(i));
				 * data.setHistogramdata(Hisdata.get(i)); // 保存信息
				 * picturesDao.savePicture(data); // System.out.println("直方图信息："
				 * + Hisdata.get(j)); } else {
				 * System.out.println("Please rename the file!"); } }
				 */

				map1.put(samelist1.get(i), tempList[i].toString());
				map.put(samelist.get(i), tempList[i].toString());
				map2.put(samelist2.get(i), tempList[i].toString());
			}
		}
		// System.out.println(samelist1);
		// System.out.println(map);
		int k = 0;
		int k1 = 0;
		int k2 = 0;
		String[] samerateStrings = new String[200];
		String[] samerateStrings1 = new String[200];
		String[] samerateStrings2 = new String[200];
		NumberFormat fmt = NumberFormat.getPercentInstance();
		fmt.setMinimumFractionDigits(4);// 最多两位百分小数，如25.23%
		for (Double key : map.keySet()) {
			String value = map.get(key);
			samerateStrings[k] = fmt.format(key);
			System.out.println(samerateStrings[k] + " 巴氏  " + value);
			k++;
		}
		for (Double key1 : map1.keySet()) {
			String value = map1.get(key1);
			samerateStrings1[k1] = fmt.format(1 - key1);
			System.out.println(samerateStrings1[k1] + " 欧式--三维  " + value);
			k1++;
		}
		for (Double key2 : map2.keySet()) {
			String value = map2.get(key2);
			samerateStrings2[k2] = fmt.format(1 - key2);
			System.out.println(samerateStrings2[k2] + " 欧式--一维  " + value);
			k2++;
		}

		Set<Double> key1 = map.keySet();
		Set<Double> key2 = map1.keySet();
		Set<Double> key3 = map1.keySet();
		// 输出key值
		// System.out.println(map.keySet());
		// 遍历key值
		Iterator<Double> int1 = key1.iterator();
		Iterator<Double> int2 = key2.iterator();
		Iterator<Double> int3 = key3.iterator();

		// 根据遍历的key值依次输出前三个value值
		for (int j = 0; j < 12; j++) {
			strArray.add(map.get(int1.next()));
			strArray1.add(map1.get(int2.next()));
			strArray2.add(map1.get(int3.next()));
			// strArrayName.add(strArray.get(j).split("\\\\")[7]);
		}
		// System.out.println(strArray);
		// System.out.println(strArray1);

		for (int j = 0; j < strArrayName.size(); j++) {

		}

	}

	@Override
	public String getCharacteristic(String srcPath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float identification(String charac1, String charac2) {
		// TODO Auto-generated method stub
		return 0;
	}
	/*
	 * @Override public String getCharacteristic2(Object target) { return
	 * (String)target; }
	 */
}
