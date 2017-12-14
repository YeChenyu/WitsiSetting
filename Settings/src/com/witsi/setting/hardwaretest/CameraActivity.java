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
	// ����ϵͳ���õ������
	private Camera camera;
		
	private boolean hasCamera = false;
	private int screenWidth, screenHeight;
	// �Ƿ���Ԥ����
	private boolean isPreview = false;

	private SharedPreferences config;
	private Editor editor;

	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// ����ȫ��
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// �ر�����
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
		//������Ŀȥ���ò���
		if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3029){
			cameraTest.setText("");
		}else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3025){
			cameraTest.setText("�������");
			cameraTest.setOnClickListener(CameraActivity.this);
		}
		else if(ProjectConfig.getTheMechineType() == ProjectConfig.PROG_3027R){
			cameraTest.setText("");
		}
				
		// ��ȡ���ڹ�����
		WindowManager wm = getWindowManager();
		Display display = wm.getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		// ��ȡ��Ļ�Ŀ�͸�
		display.getMetrics(metrics);
		screenWidth = metrics.widthPixels;
		screenHeight = metrics.heightPixels;
		// ��ȡ������SurfaceView���
		sView = (SurfaceView) findViewById(R.id.sView);
		// ���SurfaceView��SurfaceHolder
		sView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				if(camera != null){
					Toast.makeText(context, "�Զ��Խ�", Toast.LENGTH_SHORT).show();
					camera.autoFocus(new AutoFocusCallback() {
						
						@Override
						public void onAutoFocus(boolean success, Camera camera) {
							// TODO Auto-generated method stub
							Parameters parameters = camera.getParameters();
					        parameters.setPictureFormat(PixelFormat.JPEG);
					        //parameters.setPictureSize(surfaceView.getWidth(), surfaceView.getHeight());  // ���ֶ����ֻ����޷�����ʶ��÷�����
							parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);	
							parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1�����Խ�
							camera.setParameters(parameters);
							camera.startPreview();
						}
					});
				}
				return false;
			}
		});
		// ���ø�Surface����Ҫ�Լ�ά��������
		surfaceHolder = sView.getHolder();
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		// ΪsurfaceHolder���һ���ص�������
		surfaceHolder.addCallback(new Callback() {
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
			}
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// ������ͷ
				initCamera();
				FyLog.d(TAG, "init the camera success");
			}
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// ���camera��Ϊnull ,�ͷ�����ͷ
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
		//���ÿɼ������ٴε���surfaceViewCreate
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
			// �˴�Ĭ�ϴ򿪺�������ͷ��
			// ͨ������������Դ�ǰ������ͷ
			try{
				camera = Camera.open(0); // ��
				hasCamera = true;
			}catch(Exception e){
				hasCamera = false;
				ifHasCamera.setText("�������");
				FyLog.i(TAG, "�������");
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
				// ����Ԥ����Ƭ�Ĵ�С
				parameters.setPreviewSize(screenWidth, screenHeight);
				// ����Ԥ����Ƭʱÿ����ʾ����֡����Сֵ�����ֵ
				parameters.setPreviewFpsRange(4, 10);
				// ����ͼƬ��ʽ
				parameters.setPictureFormat(ImageFormat.JPEG);
				// ����JPG��Ƭ������
				parameters.set("jpeg-quality", 85);
				// ������Ƭ�Ĵ�С
				parameters.setPictureSize(screenWidth, screenHeight);
				// ͨ��SurfaceView��ʾȡ������
				camera.setPreviewDisplay(surfaceHolder); // ��
				// ��ʼԤ��
				FyLog.d(TAG, "start preview");
				camera.startPreview(); // ��
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
			//�л�ǰ������ͷ
            changeTheCamera();
			break;
		default:
			break;
		}
	}

	/**
	 * �л�ǰ������ͷ
	 */
	private int cameraPosition = 1;//0����ǰ������ͷ��1�����������ͷ
	@SuppressLint("NewApi")
	private void changeTheCamera() {
		int cameraCount = 0;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();//�õ�����ͷ�ĸ���

		for(int i = 0; i < cameraCount; i++ ) {
		    Camera.getCameraInfo(i, cameraInfo);//�õ�ÿһ������ͷ����Ϣ
		    if(cameraPosition == 1) {
		        //�����Ǻ��ã����Ϊǰ��
		        if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//��������ͷ�ķ�λ��CAMERA_FACING_FRONTǰ��      CAMERA_FACING_BACK����  
					cameraTest.setText("ǰ�����");
		        	camera.stopPreview();//ͣ��ԭ������ͷ��Ԥ��
		            camera.release();//�ͷ���Դ
		            camera = null;//ȡ��ԭ������ͷ
		            camera = Camera.open(i);//�򿪵�ǰѡ�е�����ͷ
		            try {
		                camera.setPreviewDisplay(surfaceHolder);//ͨ��surfaceview��ʾȡ������
		            } catch (IOException e) {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            }
		            camera.startPreview();//��ʼԤ��
		            cameraPosition = 0;
		            break;
		        }
		    } else {
		        //������ǰ�ã� ���Ϊ����
		        if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//��������ͷ�ķ�λ��CAMERA_FACING_FRONTǰ��      CAMERA_FACING_BACK����  
		        	cameraTest.setText("�������");
		        	camera.stopPreview();//ͣ��ԭ������ͷ��Ԥ��
		            camera.release();//�ͷ���Դ
		            camera = null;//ȡ��ԭ������ͷ
		            camera = Camera.open(i);//�򿪵�ǰѡ�е�����ͷ
		            try {
		                camera.setPreviewDisplay(surfaceHolder);//ͨ��surfaceview��ʾȡ������
		            } catch (IOException e) {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            }
		            camera.startPreview();//��ʼԤ��
		            cameraPosition = 1;
		            break;
		        }
		    }
		}
	}
	
	
	/**
	 * ����
	 * @param source
	 */
	public void capture(View source) {
		if (camera != null) {
			// ��������ͷ�Զ��Խ��������
			camera.autoFocus(autoFocusCallback); // ��
			tv_foreground.setVisibility(View.VISIBLE);
		}
	}

	AutoFocusCallback autoFocusCallback = new AutoFocusCallback() {
		// ���Զ��Խ�ʱ�����÷���
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			if (success) {
				// takePicture()������Ҫ����3������������
				// ��1�������������û����¿���ʱ�����ü�����
				// ��2�����������������ȡԭʼ��Ƭʱ�����ü�����
				// ��3�����������������ȡJPG��Ƭʱ�����ü�����
				camera.takePicture(new ShutterCallback() {
					public void onShutter() {
						// ���¿���˲���ִ�д˴�����
					}
				}, new PictureCallback() {
					public void onPictureTaken(byte[] data, Camera c) {
						// �˴�������Ծ����Ƿ���Ҫ����ԭʼ��Ƭ��Ϣ
					}
				}, myJpegCallback); // ��
			}
		}
	};

	private Bitmap bm = null;
	PictureCallback myJpegCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			tv_foreground.setVisibility(View.INVISIBLE);
			// �����������õ����ݴ���λͼ
			BitmapFactory.Options op = new BitmapFactory.Options();
			//����С����Ϊԭ��������
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
			// ����/layout/save.xml�ļ���Ӧ�Ĳ�����Դ
			View saveDialog = getLayoutInflater().inflate(R.layout.hardware_camera_save, null);
			final EditText photoName = (EditText) saveDialog
					.findViewById(R.id.phone_name);
			// ��ȡsaveDialog�Ի����ϵ�ImageView���
			ImageView show = (ImageView) saveDialog.findViewById(R.id.show);
			// ��ʾ�ո��ĵõ���Ƭ
			show.setImageBitmap(bm);
			// ʹ�öԻ�����ʾsaveDialog���
			new AlertDialog.Builder(CameraActivity.this).setView(saveDialog)
					.setPositiveButton("����", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// ����һ��λ��SD���ϵ��ļ�
							File file = new File(Environment
									.getExternalStorageDirectory(), photoName
									.getText().toString() + ".jpg");
							FileOutputStream outStream = null;
							try {
								// ��ָ���ļ���Ӧ�������
								outStream = new FileOutputStream(file);
								// ��λͼ�����ָ���ļ���
								bm.compress(CompressFormat.JPEG, 100, outStream);
								outStream.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}).setNegativeButton("ȡ��", null).show();
			// �������
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
	/************************** �¼����������� ***************************/
	@SuppressWarnings("deprecation")
	//������ؼ������ʾ����
	@Override  
    public boolean onKeyDown(int keyCode, KeyEvent event)  
    {  
        if (keyCode == KeyEvent.KEYCODE_BACK )  
        {  
            // �����˳��Ի���  
            AlertDialog isExit = new AlertDialog.Builder(this).create();  
            // ���öԻ������  
            isExit.setTitle("ϵͳ��ʾ");  
            // ���öԻ�����Ϣ  
            isExit.setMessage("ȷ��Ҫ�˳���");  
            // ���ѡ��ť��ע�����  
            isExit.setButton("ȷ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					ActivityManagers.clearActivity();
					finish();
				}
			});  
            isExit.setButton2("ȡ��", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
				}
			});  
            // ��ʾ�Ի���  
            isExit.show();  
        }  
        return false;  
    }  
}
