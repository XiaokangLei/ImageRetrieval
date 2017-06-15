package image.sharpening;
/**
 * 垂直一阶微分
 * @author
 *
 */
public class FirstMomentY extends FirstOrder{

	FirstMomentY() {
		int coefficient[] = {
				1,  0, -1, 
				2,  0, -2, 
				1,  0, -1};
		this.template = coefficient;
	}

	@Override
	public int[] processing(int[] pix, int w, int h) {
		return super.processing(pix, w, h);
	}
}
