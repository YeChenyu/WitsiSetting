package com.witsi.setting.hardwaretest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.witsi.config.ProjectConfig;
import com.witsi.debug.FyLog;
import com.witsi.setting1.R;
import com.witsi.tools.ConfigSharePaference;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Description: <br/>
 * site: <a href="http://www.crazyit.org">crazyit.org</a> <br/>
 * Copyright (C), 2001-2014, Yeeku.H.Lee <br/>
 * This program is protected by copyright laws. <br/>
 * Program Name: <br/>
 * Date:
 * 
 * @author Yeeku.H.Lee kongyeeku@163.com
 * @version 1.0
 */
public class CameraActivity extends Activity implements
		android.view.View.OnClickListener {
	
	private String TAG = "CameraActivity";
	private Context context = CameraActivity.this;
	
	private SurfaceView sView;
	private TextView ifHasCamera, tv_foreground;
//	private Button canera_return;
	private Button cameraGetBackToMain, camera_test_state, cameraFalse, cameraTest;
	
	private SurfaceHolder surfaceHolder;
	// 定义系统所用的照相机
	private Camera camera;
		
	private boolean hasCamera = false;
	private int screenWidth, screenHeight;
	// 是否在预览中
	private boolean isPreview = false;

	private SharedPreferences config;
	private Editor editor;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置全屏
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 关闭休眠
		setContentView(R.layout.hardware_camera_activity);
		config = ConfigSharePaference.getSharedPreferences(context);
		editor = config.edit();
		
		View v = findViewById(R.id.ll_tool);
		cameraGetBackToMain = (Button)v.findViewById(R.id.back);
		camera_test_state = (Button)v.findViewById(R.id.pass);
		cameraFalse = (Button)v.findViewById(R.id.fail);
		cameraTest = (Button)v.findViewById(R.id.test);
		
		cameraGetBackToMain.setOnClickListener(CameraActivity.this);
		camera_test_state.setOnClickListener(CameraActivity.this);
		cameraFalse.setOnClickListener(CameraActivity.this);
		
		
		ifHasCamera = (TextView)this.findViewById(R.id.ifHasCamera);
		tv_foreground = (TextView)this.findViewById(R.id.tv_froegound);
		//根据项目去配置布局
		if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
			cameraTest.setText("");
		}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025){
			cameraTest.setText("后置相机");
			cameraTest.setOnClickListener(CameraActivity.this);
		}
		else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3027R){
			cameraTest.setText("");
		}
				
		// 获取窗口管理器
		WindowManager wm = getWindowManager();
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		// 获取屏幕的宽和高
		display.getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		// 获取界面中SurfaceView组件
		sView = (SurfaceView) findViewById(R.id.sView);
		// 获得SurfaceView的SurfaceHolder
		sView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(camera != null){
					Toast.makeText(context, "自动对焦", Toast.LENGTH_SHORT).show();
					camera.autoFocus(new AutoFocusCallback() {
						
						@Override
						public void onAutoFocus(boolean success, Camera camera) {
							// TODO Auto-generated method stub
							Parameters parameters = camera.getParameters();
					        parameters.setPictureFormat(PixelFormat.JPEG);
					        //parameters.setPictureSize(surfaceView.getWidth(), surfaceView.getHeight());  // 部分定制手机，无法正常识别该方法。
							parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);	
							parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
							camera.setParameters(parameters);
							camera.startPreview();
						}
					});
				}
				return false;
			}
		});
		// 设置该Surface不需要自己维护缓冲区
		surfaceHolder = sView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// 为surfaceHolder添加一个回调监听器
		surfaceHolder.addCallback(new Callback() {
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// 打开摄像头
				initCamera();
				FyLog.d(TAG, "init the camera success");
			}
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// 如果camera不为null ,释放摄像头
				FyLog.d(TAG, "surfaceDestroyed()");
				if (hasCamera && camera != null) {
					if (isPreview)
						camera.stopPreview();
					camera.release();
					camera = null;
				}
			}
		});

	}

	@SuppressLint("NewApi")
	@Override
	protected void onResume(){
		super.onResume();
		FyLog.i(TAG, "onResume()");
		//设置可见，能再次调用surfaceViewCreate
		if(isInit){
			sView.setVisibility(View.VISIBLE);
		}
	}
	private boolean isInit = false;
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		isPreview = false;
		isInit = true;
	}
	
	@SuppressLint("NewApi")
	private void initCamera() {
		if (!isPreview) {
			// 此处默认打开后置摄像头。
			// 通过传入参数可以打开前置摄像头
			try{
				camera = Camera.open(0); // ①
				hasCamera = true;
			}catch(Exception e){
				hasCamera = false;
				ifHasCamera.setText("无照相机");
				FyLog.i(TAG, "无照相机");
			}
			if(camera != null){
				if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_2102)
					camera.setDisplayOrientation(90);
				else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029)
					camera.setDisplayOrientation(90);
				else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3027R)
					camera.setDisplayOrientation(90);
			}
			
		}
		if (hasCamera && camera != null && !isPreview) {
			FyLog.d(TAG, "set the camera params");
			try {
				Camera.Parameters parameters = camera.getParameters();
				// 设置预览照片的大小
				parameters.setPreviewSize(screenWidth, screenHeight);
				// 设置预览照片时每秒显示多少帧的最小值和最大值
				parameters.setPreviewFpsRange(4, 10);
				// 设置图片格式
				parameters.setPictureFormat(ImageFormat.JPEG);
				// 设置JPG照片的质量
				parameters.set("jpeg-quality", 85);
				// 设置照片的大小
				parameters.setPictureSize(screenWidth, screenHeight);
				// 通过SurfaceView显示取景画面
				camera.setPreviewDisplay(surfaceHolder); // ②
				// 开始预览
				FyLog.d(TAG, "start preview");
				camera.startPreview(); // ③
			} catch (Exception e) {
				e.printStackTrace();
			}
			isPreview = true;
		}
	}
	
	@Override
	public void onClick(View ledClick) {
		FyLog.i(TAG, "singletest = " + config.getBoolean("singletest", false));
		FyLog.i(TAG, "alltest = " + config.getBoolean("alltest", false));
		isSleepExit = false;
		switch (ledClick.getId()) {
		case R.id.back: {
			ActivityManagers.clearActivity();
			if (config.getBoolean("singletest", false) == true) {
				ActivityManagers.trunToSingleTestActivity(CameraActivity.this);
			} else if (config.getBoolean("alltest", false) == true) {
				ActivityManagers.clearActivity();
				ActivityManagers.trunToEntryActivity(CameraActivity.this);
			}else{
				ActivityManagers.trunToBurnStartActivity(CameraActivity.this);
			}  
			break;
		}
		case R.id.pass: {
			if(camera != null){
				if (config.getBoolean("singletest", false) == true) {
					editor.putString("camera", "ok");
					editor.commit();
					ActivityManagers.trunToSingleTestActivity(CameraActivity.this);
				} else if (config.getBoolean("alltest", false) == true) {
					editor.putString("camera", "ok");
					editor.commit();
					ActivityManagers.trunToNextActivity();
					ActivityManagers.startNextActivity(CameraActivity.this);
				}else{
					ActivityManagers.trunToBurnStartActivity(CameraActivity.this);
				} 
			}
			break;
		}
		case R.id.fail: {
			if (config.getBoolean("singletest", false) == true) {
				editor.putString("camera", "ng");
				editor.commit();
				ActivityManagers.trunToSingleTestActivity(CameraActivity.this);
			} else if (config.getBoolean("alltest", false) == true) {
				editor.putString("camera", "ng");
				editor.commit();
				ActivityManagers.trunToNextActivity();
				ActivityManagers.startNextActivity(CameraActivity.this);
			}else{
				ActivityManagers.trunToBurnStartActivity(CameraActivity.this);
			} 
			break;
		}
		case R.id.test:
			//切换前后摄像头
            changeTheCamera();
			break;
		default:
			break;
		}
	}

	/**
	 * 切换前后摄像头
	 */
	private int cameraPosition = 1;//0代表前置摄像头，1代表后置摄像头
	@SuppressLint("NewApi")
	private void changeTheCamera() {
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

		for(int i = 0; i < cameraCount; i++ ) {
		    Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
		    if(cameraPosition == 1) {
		        //现在是后置，变更为前置
		        if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置  
					cameraTest.setText("前置相机");
		        	camera.stopPreview();//停掉原来摄像头的预览
		            camera.release();//释放资源
		            camera = null;//取消原来摄像头
		            camera = Camera.open(i);//打开当前选中的摄像头
		            try {
		                camera.setPreviewDisplay(surfaceHolder);//通过surfaceview显示取景画面
		            } catch (IOException e) {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            }
		            camera.startPreview();//开始预览
		            cameraPosition = 0;
		            break;
		        }
		    } else {
		        //现在是前置， 变更为后置
		        if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置  
		        	cameraTest.setText("后置相机");
		        	camera.stopPreview();//停掉原来摄像头的预览
		            camera.release();//释放资源
		            camera = null;//取消原来摄像头
		            camera = Camera.open(i);//打开当前选中的摄像头
		            try {
		                camera.setPreviewDisplay(surfaceHolder);//通过surfaceview显示取景画面
		            } catch (IOException e) {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            }
		            camera.startPreview();//开始预览
		            cameraPosition = 1;
		            break;
		        }
		    }
		}
	}
	
	
	/**
	 * 拍照
	 * @param source
	 */
	public void capture(View source) {
		if (camera != null) {
			// 控制摄像头自动对焦后才拍照
			camera.autoFocus(autoFocusCallback); // ④
			tv_foreground.setVisibility(View.VISIBLE);
		}
	}

	AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
		// 当自动对焦时激发该方法
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			if (success) {
				// takePicture()方法需要传入3个监听器参数
				// 第1个监听器：当用户按下快门时激发该监听器
				// 第2个监听器：当相机获取原始照片时激发该监听器
				// 第3个监听器：当相机获取JPG照片时激发该监听器
				camera.takePicture(new ShutterCallback() {
					public void onShutter() {
						// 按下快门瞬间会执行此处代码
					}
				}, new PictureCallback() {
					public void onPictureTaken(byte[] data, Camera c) {
						// 此处代码可以决定是否需要保存原始照片信息
					}
				}, myJpegCallback); // ⑤
			}
		}
	};

	private Bitmap bm = null;
	PictureCallback myJpegCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			tv_foreground.setVisibility(View.INVISIBLE);
			// 根据拍照所得的数据创建位图
			BitmapFactory.Options op = new BitmapFactory.Options();
			//将大小缩放为原来的两倍
			op.inSampleSize = 5;
			bm = BitmapFactory.decodeByteArray(data, 0,
					data.length, op);
			Matrix m = new Matrix();
			m.reset();
			m.setScale(0.3f, 0.3f);
			if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_2102){
				m.setRotate(90);
				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
						(int)(bm.getHeight()), m, true);
			}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
				m.setRotate(-90);
				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
						(int)(bm.getHeight()), m, true);
			}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3027R){
				m.setRotate(90);
				bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
						(int)(bm.getHeight()), m, true);
			}
			// 加载/layout/save.xml文件对应的布局资源
			View saveDialog = getLayoutInflater().inflate(R.layout.hardware_camera_save, null);
			final EditText photoName = (EditText) saveDialog
					.findViewById(R.id.phone_name);
			// 获取saveDialog对话框上的ImageView组件
			ImageView show = (ImageView) saveDialog.findViewById(R.id.show);
			// 显示刚刚拍得的照片
			show.setImageBitmap(bm);
			// 使用对话框显示saveDialog组件
			new AlertDialog.Builder(CameraActivity.this).setView(saveDialog)
					.setPositiveButton("保存", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 创建一个位于SD卡上的文件
							File file = new File(Environment
									.getExternalStorageDirectory(), photoName
									.getText().toString() + ".jpg");
							FileOutputStream outStream = null;
							try {
								// 打开指定文件对应的输出流
								outStream = new FileOutputStream(file);
								// 把位图输出到指定文件中
								bm.compress(CompressFormat.JPEG, 100, outStream);
								outStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}).setNegativeButton("取消", null).show();
			// 重新浏览
			camera.stopPreview();
			camera.startPreview();
			isPreview = true;
		}
	};
	
	@Override
	protected void onDestroy(){
		super.onDestroy();
		FyLog.i(TAG, "onDestroy");
	}
	
	private boolean isSleepExit = true;
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		FyLog.i(TAG, "onStop");
		if(!isSleepExit){
			finish();
		}
	}
	/************************** 事件监听申明区 ***************************/
	@SuppressWarnings("deprecation")
	//点击返回键填出提示窗口
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
            // 创建退出对话框  
            AlertDialog isExit = new AlertDialog.Builder(this).create();  
            // 设置对话框标题  
            isExit.setTitle("系统提示");  
            // 设置对话框消息  
            isExit.setMessage("确定要退出吗");  
            // 添加选择按钮并注册监听  
            isExit.setButton("确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					ActivityManagers.clearActivity();
					finish();
				}
			});  
            isExit.setButton2("取消", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			});  
            // 显示对话框  
            isExit.show();  
        }  
        return false;  
    }  
}
