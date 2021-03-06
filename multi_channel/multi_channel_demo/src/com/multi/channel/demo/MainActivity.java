package com.multi.channel.demo;

import com.tianyou.channel.bean.PayParam;
import com.tianyou.channel.bean.RoleInfo;
import com.tianyou.channel.interfaces.BaseSdkService;
import com.tianyou.channel.interfaces.TianyouCallback;
import com.tianyou.channel.interfaces.TianyouSdk;
import com.tianyou.channel.utils.LogUtils;
import com.tianyou.channel.utils.ToastUtils;
import com.tygame.yjqk.huawei.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {

	private Activity mActivity;
	private BaseSdkService mTianyouSdk;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.mActivity = this;
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_logout).setOnClickListener(this);
		findViewById(R.id.btn_create_role).setOnClickListener(this);
		findViewById(R.id.btn_entry_game).setOnClickListener(this);
		findViewById(R.id.btn_send_role_info).setOnClickListener(this);
		findViewById(R.id.btn_update_role_info).setOnClickListener(this);
		findViewById(R.id.btn_pay_1).setOnClickListener(this);
		mTianyouSdk = TianyouSdk.getInstance(mActivity);
		mTianyouSdk.doActivityInit(mActivity, mTianyouCallback);
		LogUtils.d("平台名称：" + TianyouSdk.getChannelName(this));
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mTianyouSdk.doSaveInstanceState(outState);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			mTianyouSdk.doLogin();
			break;
		case R.id.btn_logout:
			mTianyouSdk.doLogout();
			break;
		case R.id.btn_create_role:
			mTianyouSdk.doCreateRole(getRoleInfo());
			break;
		case R.id.btn_send_role_info:
			mTianyouSdk.doUploadRoleInfo(getRoleInfo());	//在游戏有了角色信息后第一时间调用，只需要调用一次
			break;
		case R.id.btn_entry_game:
			mTianyouSdk.doEntryGame();
			break;
		case R.id.btn_update_role_info:
			mTianyouSdk.doUpdateRoleInfo(getRoleInfo());	//在角色信息发生变化后（比如升级）调用，许多次调用
			break;
		case R.id.btn_pay_1:
			new Thread(new Runnable() {
				@Override
				public void run() {
					mTianyouSdk.doPay(getPayParam());
				}
			}).start();
			break;
		}
	}
	
	private TianyouCallback mTianyouCallback = new TianyouCallback() {
		@Override
		public void onResult(int code, String msg) {
			switch (code) {
			case TianyouCallback.CODE_INIT:
				ToastUtils.show(mActivity, "初始成功");
				break;
			case TianyouCallback.CODE_LOGIN_SUCCESS:
				ToastUtils.show(mActivity, "登录成功：uid=" + msg);
				break;
			case TianyouCallback.CODE_LOGIN_FAILED:
				break;
			case TianyouCallback.CODE_LOGIN_CANCEL:
				ToastUtils.show(mActivity, "登录取消：" + msg);
				break;
			case TianyouCallback.CODE_LOGOUT:
				ToastUtils.show(mActivity, "注销：" + msg);
				break;
			case TianyouCallback.CODE_PAY_SUCCESS:
				ToastUtils.show(mActivity, "支付成功：" + msg);
				break;
			case TianyouCallback.CODE_PAY_FAILED:
				ToastUtils.show(mActivity, "支付失败：" + msg);
				break;
			case TianyouCallback.CODE_PAY_CANCEL:
				ToastUtils.show(mActivity, "支付取消：" + msg);
				break;
			case TianyouCallback.CODE_QUIT_SUCCESS:
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
				break;
			case TianyouCallback.CODE_QUIT_CANCEL:
				ToastUtils.show(mActivity, "退出游戏取消：" + msg);
				break;
			}
		}
	};
	
	private PayParam getPayParam() {
		PayParam payParam = new PayParam();
		payParam.setPayCode("pay_code_0");
		payParam.setCustomInfo("");
		payParam.setAmount("1");
		return payParam;
	}
	
	private RoleInfo getRoleInfo() {
		RoleInfo roleInfo = new RoleInfo();
		roleInfo.setBalance("100");
		roleInfo.setProfession("法师");
		roleInfo.setRoleId("13141654");
		roleInfo.setServerId("99990");
		roleInfo.setServerName("国内Android测试服");
		roleInfo.setGameName("剑与魔法");
		roleInfo.setParty("五号特工组");
		roleInfo.setRoleLevel("100");
		roleInfo.setRoleName("卢锡安");
		roleInfo.setVipLevel("12");
		roleInfo.setCreateTime(System.currentTimeMillis()+"");
		roleInfo.setRoleLevelUpTime(System.currentTimeMillis()+"");
		roleInfo.setRoleId("1001");
		return roleInfo;
	}

	@Override
	public void onBackPressed() {	//退出游戏接口
		mTianyouSdk.doExitGame();
	}
	
	@Override
	protected void onResume() {
		mTianyouSdk.doResume();
		super.onResume();
	};

	@Override
	protected void onPause() {
		mTianyouSdk.doPause();
		super.onPause();
	}
	
	@Override
	protected void onStop() {
		mTianyouSdk.doStop();
		super.onStop();
	}
	
	@Override
	protected void onRestart() {
		mTianyouSdk.doRestart();
		super.onRestart();
	}
	
	@Override
	protected void onStart() {
		mTianyouSdk.doStart();
		super.onStart();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		mTianyouSdk.doNewIntent(intent);
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onDestroy() {
		mTianyouSdk.doDestory();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mTianyouSdk.doActivityResult(requestCode, resultCode, data);
		super.onActivityResult(requestCode, resultCode, data);
	}
}
