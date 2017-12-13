package com.witsi.setting.manager;




import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.setting.manager.luncher.AppInfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint({ "NewApi", "ResourceAsColor" })
public class TipView2 extends LinearLayout{

	private String TAG = TipView2.class.getSimpleName();
	private Context context;
//	xml �����õ�����
	private Drawable d_tip = null;
	private String str_tip = null;
	private int layout_width = 0;
	private int layout_height = 0;
	private int tip_text_size = 0;
	private int tip_text_color = 0;
	private int height = 0;
	private int width = 0;
//	�Զ�������Ҫ�Ŀؼ�
	private ImageView iv_tip = null; 
	private TextView tv_tip = null;
	private ImageButton deleteView;
	private LinearLayout ll_grid_item;
	private FrameLayout fl_item_layout;
	private TableRow.LayoutParams mParams = null; 
	private TextView tv_click_block;
//	����
	private Animation anim_scale_down;
	private Animation anim_scale_up;
	private LinearLayout ll_item;
	private View v;
	
	private boolean isShowDelete = false;

//	�¼�����
	private OnPressScreenListener listener;
	
	public TipView2(Context context) {
		// TODO Auto-generated constructor stub
		super(context);
		this.context = context;
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.manager_tip_view, this);
        iv_tip = (ImageView) v.findViewById(R.id.iv_app);
        tv_tip = (TextView) v.findViewById(R.id.tv_app);
        ll_grid_item = (LinearLayout) v.findViewById(R.id.ll_grid_item);
        fl_item_layout = (FrameLayout) v.findViewById(R.id.starred_item_layout);
        ll_item = (LinearLayout) findViewById(R.id.item);
        tv_click_block = (TextView) findViewById(R.id.click_block);
        deleteView = (ImageButton )v.findViewById(R.id.delete_markView);
        deleteView.setVisibility(View.GONE);
        if(isShowDelete){
        	deleteView.setOnClickListener(new OnClickListener(){
    			@Override
    			public void onClick(View arg0) {
    				// TODO Auto-generated method stub
    				TipView2.this.listener.onDelPess(arg0);
    			}
    		});
    		deleteView.setOnLongClickListener(new OnLongClickListener(){

    			@Override
    			public boolean onLongClick(View v) {
    				// TODO Auto-generated method stub
    		        deleteView.setVisibility(View.GONE);
    				return true;
    			}
    		});
        }
        
		//����ë����Ч��
//		setBackground(this, R.drawable.header);
		this.setBackgroundColor(R.color.black);
		setFocusable(false);
		setFocusableInTouchMode(false);
	}
	
	@SuppressLint("ResourceAsColor")
	public TipView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		this.context = context;
		TypedArray a = context.obtainStyledAttributes(attrs, 
				R.styleable.tip_view); 
//		��ȡxml�����õ� attrs ����ֵ		
		d_tip = a.getDrawable(R.styleable.tip_view_tip_src); 
		str_tip = a.getString(R.styleable.tip_view_tip_text);
		layout_width = a.getLayoutDimension(R.styleable.tip_view_width, 200);
		layout_height = a.getLayoutDimension(R.styleable.tip_view_height, 100);
		tip_text_size = a.getLayoutDimension(R.styleable.tip_view_tip_text_size, 15);
		tip_text_color = a.getColor(R.styleable.tip_view_tip_text_color, Color.BLACK);
		this.setLayoutParams(new LayoutParams(layout_width, layout_height));
//		�����Զ���ؼ� ����
		iv_tip = new ImageView(context); 
		iv_tip.setId(1);
		iv_tip.setImageDrawable(d_tip);
//		iv_panel.setLayoutParams(new LayoutParams(
//				layout_width, layout_height));
//		���ܶ���
		tv_tip = new TextView(context);
		tv_tip.setId(2);
		tv_tip.setText(str_tip);
		tv_tip.setTextSize(tip_text_size);
		tv_tip.setTextColor(tip_text_color);
		tv_tip.setLayoutParams(new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		
		this.addView(iv_tip); 
		this.addView(tv_tip);
		a.recycle(); 
	}

	//��дView���onDraw()����
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
//		FyLog.v("FyLog.v", "onDraw()..");
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//		FyLog.v("FyLog.v", "onMeasure()..");
	}
	
	@Override 
	protected void onAttachedToWindow() { 
		super.onAttachedToWindow(); 
//		FyLog.e("FyLog.v", "onAttachedToWindow()..");
		TableRow.LayoutParams tip_lp = 
	    		   (TableRow.LayoutParams)getLayoutParams();
		LinearLayout.LayoutParams iv_lp = 
				(LayoutParams) iv_tip.getLayoutParams(); 
		LinearLayout.LayoutParams tv_lp =
				new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
				LayoutParams.WRAP_CONTENT);
		/**
         * tip_view ��������
         */
		width = tip_lp.width;
		height = tip_lp.height;
		FyLog.v(TAG, "the tip width is: " + tip_lp.width + 
				"the height is: " + tip_lp.height);
		tip_lp.gravity = Gravity.CENTER_VERTICAL;
		this.setLayoutParams(tip_lp);
		
		
		
		
		/**
         * iv_app_icon ��������
         */
		if (iv_lp != null) { 
			iv_lp.height = (int) (tip_lp.height * 0.4);
			iv_lp.width = iv_lp.height;
			iv_tip.setLayoutParams(iv_lp); 
		} 
		
		/**
         * tv_app_text ��������
         */
		tv_lp.gravity = Gravity.CENTER;
		tv_lp.height = (int) (tip_lp.height / 3.5);
//		tv_lp.width = LayoutParams.WRAP_CONTENT;
		tv_lp.width = (int) (tip_lp.height*0.9);
        tv_tip.setLayoutParams(tv_lp); 
        
        /**
         * tv_app_text ��������
         */
        FrameLayout.LayoutParams lp =
				new FrameLayout.LayoutParams(
						FrameLayout.LayoutParams.MATCH_PARENT, 
						FrameLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        lp.height = (int) (tip_lp.height / 1.5);
//		lp.width = LayoutParams.WRAP_CONTENT;
        lp.width = (int) (tip_lp.height / 1.5);
        
        tv_click_block.setLayoutParams(lp); 
        
	} 
	
	
	public interface OnPressScreenListener{
		
		public void setAppInfo(AppInfo info);
		public void onShortClick(View v);
		public void onDelPess(View v);
	}
	
	public void setOnPressScreenListener(OnPressScreenListener listener){
		this.listener = listener;
		v.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(deleteView.getVisibility() == View.VISIBLE)
				{
					Toast.makeText( context, "����ȡ��ж��",
			                Toast.LENGTH_SHORT).show();   
				}else
				{
					FyLog.v(TAG, "onclick tipView");
					v.startAnimation(panelScaleDown(R.anim.bottom_bac_scale_down,
							arg0));
				}
			}
		});
		v.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View arg0) {
				// TODO Auto-generated method stub
			   Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);//���ض�����Դ�ļ�  
			   v.startAnimation(shake);     
				if(isShowDelete){
					if(deleteView.getVisibility() == View.GONE)
						deleteView.setVisibility(View.VISIBLE);
					else
						deleteView.setVisibility(View.GONE);
				}else{
					TipView2.this.listener.onDelPess(arg0);
				}
				return true;
			}
		});
	}
	public void shake(){
		Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);//���ض�����Դ�ļ�  
		   this.startAnimation(shake);     
	}
	public void panelScaleDown(){
		anim_scale_down = AnimationUtils.loadAnimation(context, R.anim.bottom_bac_scale_down);
		anim_scale_down.setDuration(LONG_PRESS);
		anim_scale_down.setFillAfter(false);
		anim_scale_down.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		this.startAnimation(anim_scale_down);
	}
	
	private boolean isLongPress = true;
	private boolean isTimeOut = false;
	private Runnable long_press = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			isTimeOut = true;
			if(listener != null)
//				listener.onLongPress(v);
			FyLog.v("Listener", "onLongPress");
		}
	}; 
	/**
	 * 
	 *  �������� : panelScaleDown
	 *  �������� :   �Ǳ�������С
	 *  ����������ֵ˵����
	 *  	@param id  ��������id
	 *
	 *  �޸ļ�¼��
	 *  	���� ��2015-3-28 ����7:48:02	�޸��ˣ�gy
	 *  	����	��
	 *
	 */
	private final static int LONG_PRESS = 50;
	private Animation panelScaleDown(int id, final View v){
		anim_scale_down = AnimationUtils.loadAnimation(context, id);
		anim_scale_down.setDuration(LONG_PRESS);
		anim_scale_down.setFillAfter(false);
		anim_scale_down.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				// TODO Auto-generated method stub
				TipView2.this.listener.onShortClick(v);
			}
			@Override
			public void onAnimationRepeat(Animation arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onAnimationStart(Animation arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		
		return anim_scale_down;
	}
	/**
	 * 
	 *  �������� : panelScaleUp
	 *  �������� :   �Ǳ�����Ŵ�
	 *  ����������ֵ˵���� �Ŵ󶯻���id
	 *  	@param id
	 *
	 *  �޸ļ�¼��
	 *  	���� ��2015-3-28 ����7:46:35	�޸��ˣ�gy
	 *  	����	��
	 *
	 */
	private void panelScaleUp(int id){
		
		anim_scale_up = AnimationUtils.loadAnimation(context, id);
		anim_scale_up.setDuration(70);
		anim_scale_down.setFillAfter(true);
		this.startAnimation(anim_scale_up);
	}
	
	/**
	 * ����TipViewֻռ�ط�������ʹ�ã���ʾ��ɫ
	 */
	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		tv_tip.setVisibility(View.INVISIBLE);
		iv_tip.setVisibility(View.INVISIBLE);
		fl_item_layout.setBackgroundResource(R.color.gray_light);
		super.setEnabled(false);
	}
	@Override
	public void setBackgroundColor(int color) {
		// TODO Auto-generated method stub
		fl_item_layout.setBackgroundColor(color);
		super.setBackgroundColor(color);
	}
	
	@Override
	public void setBackgroundResource(int resid) {
		// TODO Auto-generated method stub
		fl_item_layout.setBackgroundResource(resid);
		super.setBackgroundResource(resid);
	}
	/**
	 * ���ñ߿��С
	 * @param boundary
	 */
	public void setBoundary(int boundary){
		isSetBoundary = true;
		TableRow.LayoutParams mParams = new TableRow.LayoutParams(
				TableRow.LayoutParams.MATCH_PARENT, 
				TableRow.LayoutParams.MATCH_PARENT);
		int margin = height / 60; 
//		FyLog.v("df", "the margin is; " + margin);
		if(margin < 2){
			margin = 2;
		}
		mParams.topMargin = margin;
		mParams.bottomMargin = margin;
		mParams.leftMargin = margin;
		mParams.rightMargin = margin;
		mParams.width = width;
		mParams.height = height;
		mParams.weight = weight;
		super.setLayoutParams(mParams);
		
		if(fl_item_layout != null){
			fl_item_layout.setBackgroundColor(Color.WHITE);
		}
		if(ll_grid_item != null){
			ll_grid_item.setBackgroundColor(Color.GRAY);
			ll_grid_item.setPadding(boundary, boundary, boundary, boundary);
		}
		if(tv_tip != null){
			tv_tip.setBackgroundColor(Color.TRANSPARENT);
		}
	}
	private boolean isSetBoundary = false;
	private int BOUNDARY = 2;
	public void setHeight(int height){
		TableRow.LayoutParams mParams = new TableRow.LayoutParams(
				android.widget.TableRow.LayoutParams.WRAP_CONTENT, 
				android.widget.TableRow.LayoutParams.WRAP_CONTENT);
		mParams.width = width;
		mParams.height = height;
		mParams.weight = weight;
		if(!isSetBoundary){
			int margin = height / 60; 
//			FyLog.v("df", "the margin is; " + margin);
			if(margin < BOUNDARY){
				margin = BOUNDARY;
			}
			mParams.topMargin = margin;
			mParams.bottomMargin = margin;
			mParams.leftMargin = margin;
			mParams.rightMargin = margin;
		}
		this.height = height;
		super.setLayoutParams(mParams);
	}
	
	public void setWidth(int width){
		TableRow.LayoutParams mParams = new TableRow.LayoutParams(
				android.widget.TableRow.LayoutParams.WRAP_CONTENT, 
				android.widget.TableRow.LayoutParams.WRAP_CONTENT);
		mParams.width = width;
		mParams.height = height;
		mParams.weight = weight;
		if(!isSetBoundary){
			int margin = height / 60; 
//			FyLog.v("df", "the margin is; " + margin);
			if(margin < BOUNDARY){
				margin = BOUNDARY;
			}
			mParams.topMargin = margin;
			mParams.bottomMargin = margin;
			mParams.leftMargin = margin;
			mParams.rightMargin = margin;
		}
		this.width = width;
		super.setLayoutParams(mParams);
	}
	
	private float weight = 1;
	/**
	 * ������ռ�б�����С
	 * @param weight
	 */
	public void setWeightType(float weight){
		TableRow.LayoutParams mParams = new TableRow.LayoutParams(
				android.widget.TableRow.LayoutParams.WRAP_CONTENT, 
				android.widget.TableRow.LayoutParams.WRAP_CONTENT);
		mParams.weight = weight;
		mParams.height = height;
		if(!isSetBoundary){
			int margin = height / 60; 
//			FyLog.v("df", "the margin is; " + margin);
			if(margin < BOUNDARY){
				margin = BOUNDARY;
			}
			mParams.topMargin = margin;
			mParams.bottomMargin = margin;
			mParams.leftMargin = margin;
			mParams.rightMargin = margin;
		}
		this.weight = weight;
		if(weight != 1)
			super.setLayoutParams(mParams);
		else{
			LinearLayout.LayoutParams tv_lp =
					new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, 
					LayoutParams.WRAP_CONTENT);
			tv_lp.gravity = Gravity.CENTER;
//			tv_lp.weight = (int) (width / 3.5);
			tv_lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
	        tv_tip.setLayoutParams(tv_lp); 
			ll_item.setOrientation(LinearLayout.HORIZONTAL);
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		width = w;
		FyLog.v(TAG, "w: " + w + " h: " + h + " oldw: " + oldw + " oldh: " + oldh );
		super.onSizeChanged(w, h, oldw, oldh);
	}
	public final static int HORIZONTAL = 0;
	public final static int VERTICAL = 1;
	public void setImageOrientation(int orientation){
//		ll_item.setOrientation(LinearLayout.HORIZONTAL);
//		FrameLayout.LayoutParams mParams = new FrameLayout.LayoutParams(
//				FrameLayout.LayoutParams.WRAP_CONTENT, 
//				FrameLayout.LayoutParams.WRAP_CONTENT);
//		mParams.gravity = Gravity.CENTER;
//		ll_item.setLayoutParams(mParams);
//		
//		LinearLayout.LayoutParams tv_lp =
//				new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 
//				LayoutParams.WRAP_CONTENT);
//		tv_lp.gravity = Gravity.CENTER;
//		tv_lp.leftMargin = width / 10;
//		tv_lp.height = (int) (height / 3.5);
//        tv_tip.setLayoutParams(tv_lp); 
	}
	/**
	 * ����Ӧ�ñ�ǩ�ı�
	 * @param text
	 */
	public void setText(String text){
		if(text!=null)
		tv_tip.setText(text);
	}
	
	public CharSequence getText(){
		return tv_tip.getText();
	}
	/**
	 * ����Ӧ�ñ�ǩ�����С
	 * @param size
	 */
	public void setTextSize(float size){
		tv_tip.setTextSize(size);
	}
	
	public void setLabelWidth(int width){
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT, 
				LinearLayout.LayoutParams.FILL_PARENT);
		lp.width = 0;
		lp.height = 0;
		tv_tip.setLayoutParams(lp);
	}
	public void setImageResource(int resId){
		iv_tip.setImageResource(resId);
	}
	/**
	 * ����Ӧ��ͼ��
	 * @param d
	 */
	public void setImageDrawable(Drawable d){
		iv_tip.setImageDrawable(d);
	}
	/**
	 * ����Ӧ��ͼ��λͼ
	 * @param d λͼ��Ϣ
	 */
	public void setImageBitmap(Bitmap d){
		if(d!=null)
		iv_tip.setImageBitmap(d);
	}
	
	private float weightNum = -1;
	public float getWeightNum() {
		return weightNum;
	}
	/**
	 * ���������и���
	 * @param weightNum
	 */
	public void setWeightNum(float weightNum) {
		
		this.weightNum = weightNum;
	}

	private int position_x = -1;
	private int position_y = -1;
	public int getPositionX() {
		return position_x;
	}
	/**
	 * ���ú���λ��
	 * @param position_x ������
	 */
	public void setPositionX(int position_x) {
		this.position_x = position_x;
	}
	
	public int getPositionY() {
		return position_y;
	}
	/**
	 * ��������λ��
	 * @param position_y ������
	 */
	public void setPositionY(int position_y) {
		this.position_y = position_y;
	}
	
	public void setBlurBackground(){
		
//		setBackground(this, R.drawable.btn_check_label_background);
		ll_grid_item.setBackgroundColor(R.color.black);
	}
//	/**
//     * ����ë��������
//     * @param id ����ͼƬid
//     */
//    @SuppressLint("ResourceAsColor")
//	private void setBackground(final View layout, int id){    
//    	Bitmap bmp = BitmapFactory.decodeResource(getResources(),id);//����Դ�ļ��еõ�ͼƬ��������BitmapͼƬ                
//        final Bitmap blurBmp = BlurUtil.fastblur(context, bmp, 50);//0-25����ʾģ��ֵ        
//        final Drawable newBitmapDrawable = new BitmapDrawable(blurBmp); // ��Bitmapת��ΪDrawable 
//        layout.post(new Runnable(){  //����UI�߳�                        
//        	@Override                        
//            public void run() {                                
//        		layout.setBackgroundDrawable(newBitmapDrawable);//���ñ���
//            }                
//        });        
//    }
}
