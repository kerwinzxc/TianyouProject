package com.tianyou.channel.business;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.eFlag;
import com.tencent.ysdk.framework.common.ePlatform;
import com.tencent.ysdk.module.bugly.BuglyListener;
import com.tencent.ysdk.module.pay.PayListener;
import com.tencent.ysdk.module.pay.PayRet;
import com.tencent.ysdk.module.user.UserListener;
import com.tencent.ysdk.module.user.UserLoginRet;
import com.tencent.ysdk.module.user.UserRelationRet;
import com.tencent.ysdk.module.user.WakeupRet;
import com.tianyou.channel.bean.OrderInfo.ResultBean.OrderinfoBean;
import com.tianyou.channel.bean.PayParam;
import com.tianyou.channel.interfaces.BaseSdkService;
import com.tianyou.channel.interfaces.TianyouCallback;
import com.tianyou.channel.utils.AppUtils;
import com.tianyou.channel.utils.HttpUtils;
import com.tianyou.channel.utils.HttpUtils.HttpCallback;
import com.tianyou.channel.utils.LogUtils;
import com.tianyou.channel.utils.ResUtils;
import com.tianyou.channel.utils.URLHolder;

public class YingyongbaoSdkService extends BaseSdkService {
	/**
	 * openid
	 * openkey
	 * pay_token
	 * pf
	 * pfkey
	 * orderid
	 * zoneid
	 */
	private String openID;
	private String token;
	private String payToken;
	private String pf;
	private String pfKey;
	private int platForm;
	
	private String tyUserId;
	
	@Override
	public void doActivityInit(Activity activity,
			TianyouCallback tianyouCallback) {
		super.doActivityInit(activity, tianyouCallback);
		YSDKApi.onCreate(mActivity);
		YSDKApi.handleIntent(mActivity.getIntent());
		YSDKApi.setUserListener(new MyUserListener());
		YSDKApi.setBuglyListener(new MyBugliListener());
		mTianyouCallback.onResult(TianyouCallback.CODE_INIT, "");
	}
	
	@Override
	public void doLogin() {
		if (tyUserId == null) {
			YSDKApi.login(ePlatform.QQ);
		} else {
			mTianyouCallback.onResult(TianyouCallback.CODE_LOGIN_SUCCESS, tyUserId);
		}
	}
	
	@Override
	public void doLoginWechat() {
		if (tyUserId == null) {
			YSDKApi.login(ePlatform.WX);
		} else {
			mTianyouCallback.onResult(TianyouCallback.CODE_LOGIN_SUCCESS, tyUserId);
		}
	}
	
	@Override
	public boolean isShowLogout() {
		return true;
	}
	
	@Override
	public boolean isShowExitGame() {
		return true;
	}
	
	@Override
	public void doChannelPay(PayParam payInfo, OrderinfoBean orderInfo) {
		Log.d("TAG", "channel pay-----------");
		String zoneId = mRoleInfo.getServerId();
		String saveValue = orderInfo.getMoNey();
		boolean isCanChange = false;
		Bitmap bmp = BitmapFactory.decodeResource(mActivity.getResources(), ResUtils.getResById(mActivity, "sample_yuanbao", "drawable"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
		byte[] appResData = baos.toByteArray();
		Log.d("TAG", appResData.length+"");
		final String extrolInfo = orderInfo.getOrderID();
		Log.d("TAG", "zoneID= "+zoneId);
		YSDKApi.recharge(zoneId, saveValue, false, appResData, extrolInfo, new PayListener() {
			@Override
			public void OnPayNotify(PayRet payRet) {
				Log.d("TAG", "yyb pay result payret= "+payRet);
				if (payRet.ret == 0) {
					Log.d("TAG", "success--------------1");
					checkYybOrder(extrolInfo);
					Log.d("TAG", "success--------------2");
				} else {
					mTianyouCallback.onResult(TianyouCallback.CODE_PAY_FAILED, "支付失败");
					Log.d("TAG", "failed------------");
				}
			}
		});
	}
	
	@Override
	public void doLogout() {
		tyUserId = null;
		YSDKApi.logout();
		super.doLogout();
	}
	
	private void checkYybOrder (String orderID){
		/**
		 * openid
		 * openkey
		 * pay_token
		 * pf
		 * pfkey
		 * orderid
		 * zoneid
		 */
		Map<String, String> checkParam = new HashMap<String, String>();
		checkParam.put("openid", openID);
		checkParam.put("openkey", token);
		checkParam.put("pay_token", payToken);
		checkParam.put("pf", pf);
		checkParam.put("pfkey", pfKey);
		checkParam.put("orderid", orderID);
		checkParam.put("platform", platForm+"");
		checkParam.put("zoneid", mRoleInfo.getServerId());
		Log.d("TAG", "checkParam= "+checkParam);
		HttpUtils.post(mActivity, URLHolder.CHECK_ORDER_ML_LY_YYB,checkParam, new HttpCallback() {
					@Override
					public void onSuccess(String data) {
						Log.d("TAG", "yyb check success data= "+data);
						try {
							JSONObject jsonObject = new JSONObject(data);
							JSONObject result = jsonObject.getJSONObject("result");
							String code = result.getString("code");
							String msg = result.getString("msg");
							if ("200".equals(code)) {
								mTianyouCallback.onResult(TianyouCallback.CODE_PAY_SUCCESS, msg);
							} else {
								mTianyouCallback.onResult(TianyouCallback.CODE_PAY_FAILED, data);
								Log.d("TAG", "pay failed----------data= "+data);
							}
						} catch (JSONException e) {
							e.printStackTrace();
							mTianyouCallback.onResult(TianyouCallback.CODE_PAY_FAILED, e.getMessage());
							Log.d("TAG", "pay failed------------excption= "+e.getMessage());
						}
					}
					
					@Override
					public void onFailed(String code) {
						Log.d("TAG", "yyb check failed code= "+code);
						mTianyouCallback.onResult(TianyouCallback.CODE_PAY_FAILED, code);
					}
				});
	}
	
	@Override
	public void doResume() {
		YSDKApi.onResume(mActivity);
	}
	
	@Override
	public void doPause() {
		YSDKApi.onPause(mActivity);
	}
	
	@Override
	public void doStop() {
		YSDKApi.onStop(mActivity);
	}
	
	@Override
	public void doDestory() {
		YSDKApi.onDestroy(mActivity);
	}
	
	@Override
	public void doRestart() {
		YSDKApi.onRestart(mActivity);
	}
	
	@Override
	public void doNewIntent(Intent intent) {
		YSDKApi.handleIntent(intent);
	}
	
	@Override
	public void doActivityResult(int requestCode, int resultCode, Intent data) {
		YSDKApi.onActivityResult(requestCode, resultCode, data);
	}
	
	
	private class MyUserListener implements UserListener {
		@Override
		public void OnLoginNotify( UserLoginRet loginRet) {
			Log.d("TAG", "loginRet= "+loginRet.toString());
			if (loginRet.flag == 2000) {
				Toast.makeText(mActivity, "请先安装手机微信", Toast.LENGTH_SHORT).show();
				return;
			}
			openID = loginRet.open_id;
			token = loginRet.getAccessToken();
			if (!TextUtils.isEmpty(openID) && !TextUtils.isEmpty(token)) {
				payToken = loginRet.getPayToken();
				pf = loginRet.pf;
				pfKey = loginRet.pf_key;
				platForm = loginRet.platform;
				checkLogin(openID, token, platForm);
			}
		}

		@Override
		public void OnRelationNotify(UserRelationRet relationRet) {
			Log.d("TAG", "OnRelationNotify relationRet= "+relationRet.toString());
		}

		@Override
		public void OnWakeupNotify(WakeupRet wakeupRet) {
			Log.d("TAG", "OnWakeupNotify wakeupRet= "+wakeupRet.toString());
		}
	}
	
	private class MyBugliListener implements BuglyListener {

		@Override
		public byte[] OnCrashExtDataNotify() {
			Log.d("TAG", "OnCrashExtDataNotify----------------");
			return null;
		}

		@Override
		public String OnCrashExtMessageNotify() {
			Log.d("TAG", "OnCrashExtMessageNotify-------------");
			return null;
		}
	}
	
	private void checkLogin(String uid,String token,int loginType) {
		String gameId = mChannelInfo.getGameId();
		Map<String, String> param = new HashMap<String, String>();
		param.put("uid", uid);
		param.put("session", token);
		param.put("imei", AppUtils.getPhoeIMEI(mActivity));
		param.put("appid", gameId);
		param.put("logintype", loginType+"");
		param.put("promotion", mChannelInfo.getChannelId());
		param.put("signature", AppUtils.MD5("session=" + token + "&uid=" + uid + "&appid=" + gameId));
		LogUtils.d("loginParam:" + param);
		HttpUtils.post(mActivity, URLHolder.URL_BASE+URLHolder.CHECK_LOGIN_URL, param, new HttpCallback() {
			@Override
			public void onSuccess(String data) {
				try {
					JSONObject jsonObject = new JSONObject(data);
					JSONObject result = (JSONObject) jsonObject.get("result");
					String code = result.getString("code");
					if ("200".equals(code)) {
						tyUserId = result.getString("uid");
						mTianyouCallback.onResult(TianyouCallback.CODE_LOGIN_SUCCESS, tyUserId);
						Log.d("TAG", "登录成功");
					} else {
						mTianyouCallback.onResult(TianyouCallback.CODE_LOGIN_FAILED, "登陆失败");
					}
				} catch (JSONException e) {
					e.printStackTrace();
					mTianyouCallback.onResult(TianyouCallback.CODE_LOGIN_FAILED, e.getMessage());
				}
			}
			@Override
			public void onFailed(String code) {
				mTianyouCallback.onResult(TianyouCallback.CODE_LOGIN_FAILED, code);
			}
		});
	}
	
	public ePlatform getPlatform() {
        UserLoginRet ret = new UserLoginRet();
        YSDKApi.getLoginRecord(ret);
        if (ret.flag == eFlag.Succ) {
            return ePlatform.getEnum(ret.platform);
        }
        return ePlatform.None;
    }
	
	private void showDialog(Activity activity){
		View dialogView = View.inflate(activity, ResUtils.getResById(activity, "dialog_exit_pay", "layout"), null);
		final AlertDialog dialog = new AlertDialog.Builder(activity)
        .create();
		dialog.setView(dialogView);
		dialog.show();
		
		dialog.setCanceledOnTouchOutside(false);
	}
}
