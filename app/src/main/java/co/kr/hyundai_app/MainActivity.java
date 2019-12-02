package co.kr.hyundai_app;

import kr.co.parser.HtmlParser;


import co.kr.network.NetActivity;

import com.google.android.gcm.GCMRegistrar;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.app.Activity;

import android.content.Context;

import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.view.Menu;

import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;

import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends NetActivity {
	String regId;
	String SENDER_ID;
	Context mContext;
	String url;

	final int requestCode = 0;
	private boolean net_chk;
	public static Activity main_act;
	private static final int APP_PERMISSION_STORAGE = 9787;
	private final int APPS_PERMISSION_REQUEST = 1000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		net_chk = networkCheck();//네트워크 상태체크
		main_act = this;
		//GCM알림 메세지 기능
		url = this.getString(R.string.json_setup).toString();
		mContext = this;
		GCMRegistrar.checkDevice(this);//디바이스가 작동이 되는지 안 되는지 체크
		GCMRegistrar.checkManifest(this);//메니페스트에 권한설정이 되어있는지 체크
		regId = GCMRegistrar.getRegistrationId(this);//GCM고유키값
		SENDER_ID = "879391621165";//프로젝트 번호

		//버전별 체크를 한 후 마시멜로 이상이면 퍼미션 체크 여부
		try {
			if (Build.VERSION.SDK_INT >= 23) {
				checkPermission();
			} else {
				checkGCM();
			}
		} catch (Exception e) {
		}


	}

	public void checkGCM() {
		TelephonyManager tpm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		if ( ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			Toast.makeText(mContext, "권한설정을 하신 후에 이용이 가능합니다.", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
			startActivityForResult(intent, 0);
			finish();
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		final String myNumber = tpm.getLine1Number();

		//GCM 아이디 값과 휴대폰을 서버에 보내기
		HtmlParser hp = new HtmlParser(url + "?regid=" + regId + "&phone=" + myNumber);
		String content = hp.singleHtmlParser();
		final String jsonContent = hp.singleJsonParser(content);
		Log.d("json", jsonContent);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String mb_id = settings.getString("ss_mb_id", "").toString();

		if (net_chk) {
			boolean isLogin = true;

			if (mb_id.equals("")) {
				isLogin = false;
			}
			if (jsonContent.equals("not")) {
				isLogin = false;
			}

			if (isLogin == false) {
				Intent intent = new Intent(this, LoginActivity.class);
				startActivity(intent);
				finish();

			} else {
				goHandler();
			}
			if (regId.equals("")) {
				//Log.d("gcmOk","ok");
				GCMRegistrar.register(this, SENDER_ID);
			} else {
				Log.v("send", "Already registered");
			}
		} else {
			Toast.makeText(this, "인터넷상태가 양호하지 않습니다.", Toast.LENGTH_SHORT).show();
		}
	}

	@TargetApi(Build.VERSION_CODES.M)
	public void checkPermission() {
		try {

			//권한이 없는 경우

				checkGCM();

				//writeFile();

		} catch (Exception e) {
			Log.d("error",e.toString());
		}
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onResume() {
		super.onResume();

	}

	public void onPause() {
		super.onPause();
	}

	public void onDestroy() {
		super.onDestroy();
		try {
			//종료한 후 다시 어플키면 세션값은 초기화
			GCMRegistrar.onDestroy(this);
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	//핸들러로 이용해서 3초간 머물고 이동이 됨
	public void goHandler() {
		Handler mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {

				startActivity(new Intent(getApplicationContext(), MenuActivity.class));
				finish();

			}
		}, 3000);
	}

}
