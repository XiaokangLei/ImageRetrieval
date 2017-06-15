package image.sharpening;

/**
 * 一阶微分Sobel算子
 * @author 
 *
 */
public class Sobel extends Sharpening {

	@SuppressWarnings("unused")
	@Override
	public int[] processing(int[] pix, int w, int h) {
		//System.out.print("sobel processing  \t");
		//ColorModel cm = ColorModel.getRGBdefault();
		int g1, g2, r;
		int[] newpix = new int[w * h];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (x != 0 && x != w - 1 && y != 0 && y != h - 1) {
					// G
					g1 = pix[x - 1 + (y - 1) * w] + 2
							* pix[x + (y - 1) * w]
							+ pix[x + 1 + (y - 1) * w]
							- pix[x - 1 + (y + 1) * w] - 2
							* pix[x + (y + 1) * w]
							- pix[x + 1 + (y + 1) * w];
					g2 = pix[x - 1 + (y - 1) * w] + 2
							* pix[x - 1 + (y) * w]
							+ pix[x - 1 + (y + 1) * w]
							- pix[x + 1 + (y - 1) * w] - 2
							* pix[x + 1 + (y) * w]
							- pix[x + 1 + (y + 1) * w];
					newpix[x + y * w] = (int) Math.round(Math.sqrt(g1 * g1 + g2 * g2));
					//newpix[x + y * w] = 255 << 24 | r << 16 | r << 8 | r;
				} /*else {
					if (x == w - 1) {
						newpix[x + y * w] = pix[x - 1 + y * w];
					}
					if (y == h - 1) {
						newpix[x + y * w] = pix[x + (y - 1) * w];
					}0
				}*/
			}
		}
		return super.processing(newpix, w, h);
	}

}
