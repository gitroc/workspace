package cn.com.aa.common.android.utils;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

public class AnimationUtils {
	private static boolean translatAnimSwitch = false;
	private static boolean sacleAmimSwitch = false;
	
	/**
	 * 沿任意方向移动的动画
	 * 
	 * @param params
	 *            包装动画参数的引用
	 */
	public static void setTranslateAnimation(final AinmParams params) {
		if (params.getView() != null) {
			if (!translatAnimSwitch) {
				final TranslateAnimInterface transslateInterface = params.getTranlateInterface();
				final TranslateAnimation anim = new TranslateAnimation(
						params.getFromXType(), params.getFromX(),
						params.getToXType(), params.getToX(),
						params.getFromYType(), params.getFromY(),
						params.getToYType(), params.getToY());
				anim.setFillAfter(params.getFillAfter());
				anim.setDuration(params.getTime());
				anim.setDuration(params.getTime());
				params.getView().startAnimation(anim);
				anim.setAnimationListener(new AnimationListener() {
					
					public void onAnimationStart(Animation animation) {
						translatAnimSwitch = !translatAnimSwitch;
						transslateInterface.doAnimStart();
					}
					
					public void onAnimationRepeat(Animation animation) {
						transslateInterface.doAnimRepeat();
					}

					
					public void onAnimationEnd(Animation animation) {
						translatAnimSwitch = !translatAnimSwitch;
						transslateInterface.doAnimEnd();
					}
				});
			}
		}
	}

	/**
	 * 缩放动画
	 * @param params
	 */
	public static void setScaleAnimation(final AinmParams params) {
		if (params.getView() != null) {
			if (!sacleAmimSwitch) {
				final ScaleAnimInterface scaleInterface = params.getScaleInterface();
				ScaleAnimation anim = new ScaleAnimation(params.getFromX(),
						params.getToX(), params.getFromY(), params.getToY(),
						params.getPivotXType(), params.getPivotXValue(),
						params.getPivotYType(), params.getPivotYValue());
				anim.setDuration(params.getTime());
				anim.setFillAfter(params.getFillAfter());
				params.getView().startAnimation(anim);
				anim.setAnimationListener(new AnimationListener() {
					
					public void onAnimationStart(Animation animation) {
						sacleAmimSwitch = !sacleAmimSwitch;
						scaleInterface.doAnimStart();
					}

					
					public void onAnimationRepeat(Animation animation) {
						scaleInterface.doAnimRepeat();
					}

					
					public void onAnimationEnd(Animation animation) {
						sacleAmimSwitch = !sacleAmimSwitch;
						params.getView().clearAnimation();
						scaleInterface.doAnimEnd();
					}
				});
			}
		}
	}
	
	/**
	 * 为TranslateAnimation的监听器中各个方法对应的实现留一个接口，让调用动画者具体实现（也可不管）
	 * @author xjzhao
	 *
	 */
	public interface TranslateAnimInterface{
		public void doAnimStart();
		public void doAnimRepeat();
		public void doAnimEnd();
	}
	
	/**
	 * 为ScaleAnimation的监听器中各个方法对应的实现留一个接口，让调用动画者具体实现（也可不管）
	 * @author xjzhao
	 *
	 */
	public interface ScaleAnimInterface{
		public void doAnimStart();
		public void doAnimRepeat();
		public void doAnimEnd();
	}


	/**
	 * 设置动画参数，若不设置起始位置则以传递的view起始位置做默认值， 动画持续默认时间为200ms
	 * 
	 * @author xjzhao
	 * 
	 */
	public static class AinmParams {

		/* 所有用到动画的参数 */
		private View view; // 要进行动画的view
		private float fromX; // X轴起始位置
		private float toX; // X轴到达位置
		private float fromY; // Y轴起始位置
		private float toY; // Y轴到达位置
		private long time; // 动画持续时间
		private int fromXType;// X轴参照物
		private int toXType;
		private int fromYType;// Y轴参照物
		private int toYType;

		/* ScaleAnimation中用到的参数 */
		private int pivotXType;
		private float pivotXValue;
		private int pivotYType;
		private float pivotYValue;
		
		
		private boolean fillAfter;//动画后是否回退到起始位置
		
		private TranslateAnimInterface tranlateInterface;//为TranlateAnimation的AnimationListener中的各个方法做具体实现
		private ScaleAnimInterface scaleInterface;//为ScaleAnimation的AnimationListener中的各个方法做具体实现

		/**
		 * 设置各个参数的默认值
		 */
		private void setDefaultValue() {
			fromX = 0;// 默认起始位置为0
			fromY = 0;
			toX = 0;// 默认起始位置为0
			toY = 0;
			time = 200; // 默认动画持续时间为200ms
			fromXType = Animation.RELATIVE_TO_SELF;// 参照物都默认为view自己
			toXType = Animation.RELATIVE_TO_SELF;
			fromYType = Animation.RELATIVE_TO_SELF;
			fromYType = Animation.RELATIVE_TO_SELF;
			pivotXType = Animation.RELATIVE_TO_SELF;
			pivotYValue = Animation.RELATIVE_TO_SELF;
			
			pivotXValue = 0.5f;//默认为view的中心点
			pivotYValue = 0.5f;
			
			fillAfter = false;
			
			tranlateInterface = new TranslateAnimInterface() {//默认接口中不做任何处理
				
				
				public void doAnimStart() {
					// TODO Auto-generated method stub
					
				}
				
				
				public void doAnimRepeat() {
					// TODO Auto-generated method stub
					
				}
				
				
				public void doAnimEnd() {
					// TODO Auto-generated method stub
					
				}

			};
			
			scaleInterface = new ScaleAnimInterface() {//默认接口中不做任何处理
				
				
				public void doAnimStart() {
					// TODO Auto-generated method stub
					
				}
				
				
				public void doAnimRepeat() {
					// TODO Auto-generated method stub
					
				}
				
				
				public void doAnimEnd() {
					// TODO Auto-generated method stub
					
				}
			};
			
		}

		public boolean getFillAfter() {
			return fillAfter;
		}

		public AinmParams setFillAfter(boolean fillAfter) {
			this.fillAfter = fillAfter;
			return this;
		}

		public View getView() {
			return view;
		}

		public AinmParams setView(View view) {
			if (view != null) {
				this.view = view;
				setDefaultValue();
			}
			return this;
		}

		public float getFromX() {
			return fromX;
		}

		public AinmParams setFromX(float fromX) {
			this.fromX = fromX;
			return this;
		}

		public float getToX() {
			return toX;
		}

		public AinmParams setToX(float toX) {
			this.toX = toX;
			return this;
		}

		public float getFromY() {
			return fromY;
		}

		public AinmParams setFromY(float fromY) {
			this.fromY = fromY;
			return this;
		}

		public float getToY() {
			return toY;
		}

		public AinmParams setToY(float toY) {
			this.toY = toY;
			return this;
		}

		public long getTime() {
			return time;
		}

		public AinmParams setTime(long time) {
			this.time = time;
			return this;
		}

		public int getFromXType() {
			return fromXType;
		}

		public AinmParams setFromXType(int fromXType) {
			this.fromXType = fromXType;
			return this;
		}

		public int getToXType() {
			return toXType;
		}

		public AinmParams setToXType(int toXType) {
			this.toXType = toXType;
			return this;
		}

		public int getFromYType() {
			return fromYType;
		}

		public AinmParams setFromYType(int fromYType) {
			this.fromYType = fromYType;
			return this;
		}

		public int getToYType() {
			return toYType;
		}

		public AinmParams setToYType(int toYType) {
			this.toYType = toYType;
			return this;
		}

		public int getPivotXType() {
			return pivotXType;
		}

		public AinmParams setPivotXType(int pivotXType) {
			this.pivotXType = pivotXType;
			return this;
		}

		public float getPivotXValue() {
			return pivotXValue;
		}

		public AinmParams setPivotXValue(float pivotXValue) {
			this.pivotXValue = pivotXValue;
			return this;
		}

		public int getPivotYType() {
			return pivotYType;
		}

		public AinmParams setPivotYType(int pivotYType) {
			this.pivotYType = pivotYType;
			return this;
		}

		public float getPivotYValue() {
			return pivotYValue;
		}

		public AinmParams setPivotYValue(float pivotYValue) {
			this.pivotYValue = pivotYValue;
			return this;
		}

		public TranslateAnimInterface getTranlateInterface() {
			return tranlateInterface;
		}

		public AinmParams setTranlateInterface(TranslateAnimInterface tranlateInterface) {
			this.tranlateInterface = tranlateInterface;
			return this;
		}

		public ScaleAnimInterface getScaleInterface() {
			return scaleInterface;
		}

		public AinmParams setScaleInterface(ScaleAnimInterface scaleInterface) {
			this.scaleInterface = scaleInterface;
			return this;
		}
		
	}

}
