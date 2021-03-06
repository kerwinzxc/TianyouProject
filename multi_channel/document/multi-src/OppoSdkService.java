package com.tianyou.channel.business;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.nearme.game.sdk.callback.GameExitCallback;
import com.nearme.game.sdk.common.model.biz.PayInfo;
import com.nearme.game.sdk.common.model.biz.ReportUserGameInfoParam;
import com.nearme.game.sdk.common.util.AppUtil;
import com.nearme.platform.opensdk.pay.PayResponse;
import com.tianyou.channel.bean.ChannelInfo;
import com.tianyou.channel.bean.LoginInfo;
import com.tianyou.channel.bean.PayParam;
import com.tianyou.channel.bean.RoleInfo;
import com.tianyou.channel.bean.OrderInfo.ResultBean.OrderinfoBean;
import com.tianyou.channel.interfaces.BaseSdkService;
import com.tianyou.channel.interfaces.TianyouCallback;
import com.tianyou.channel.utils.ConfigHolder;
import com.tianyou.channel.utils.LogUtils;

/**
 * oppo渠道接入
 * @author itstrong
 *
 */
public class OppoSdkService extends BaseSdkService {
	
	private String appSecret;

	@Override
	public void doApplicationCreate(Context context, boolean island) {
		ChannelInfo channelInfo = ConfigHolder.getChannelInfo(mActivity);
		appSecret = channelInfo.getAppSecret();
		if (context.getApplicationInfo().packageName.equals(AppUtil.getCurrentProcessName(context))) {
			Log.d("TAG","appSecret= "+appSecret);
			GameCenterSDK.init(appSecret, context);
		}
	}
	
	@Override
	public void doActivityInit(Activity activity, TianyouCallback tianyouCallback) {
		super.doActivityInit(activity, tianyouCallback);
		mTianyouCallback.onResult(TianyouCallback.CODE_INIT, "初始化成功");
	}
	
	@Override
	public void doLogin() {
		GameCenterSDK.getInstance().doLogin(mActivity, new ApiCallback() {
			@Override
			public void onSuccess(String resultMsg) {
				getTokenInfo();
			}

			@Override
			public void onFailure(String resultMsg, int resultCode) {
				LogUtils.d("onFailure:resultMsg=" + resultMsg + ",resultCode=" + resultCode);
				mTianyouCallback.onResult(TianyouCallback.CODE_LOGIN_FAILED, "");
			}
		});
	}
	
	@Override
	public boolean isShowExitGame() { return true; }
	
	@Override
	public void doChannelPay(PayParam payInfo, final OrderinfoBean orderInfo) {
		super.doChannelPay(payInfo, orderInfo);
		PayInfo oppoPayInfo = new PayInfo(orderInfo.getOrderID(), "自定义字段", Integer.parseInt(orderInfo.getMoNey()));
		oppoPayInfo.setProductDesc(mPayInfo.getProductDesc());
		oppoPayInfo.setProductName(mPayInfo.getProductName());
		oppoPayInfo.setCallbackUrl(orderInfo.getNotifyurl());
		
		GameCenterSDK.getInstance().doPay(mActivity, oppoPayInfo, new ApiCallback() {
			@Override
			public void onSuccess(String resultMsg) {
				checkOrder(orderInfo.getOrderID());
			}

			@Override
			public void onFailure(String resultMsg, int resultCode) {
				if (PayResponse.CODE_CANCEL != resultCode) {
					mTianyouCallback.onResult(TianyouCallback.CODE_PAY_FAILED, "");
				} else {
					mTianyouCallback.onResult(TianyouCallback.CODE_PAY_CANCEL, "");
				}
			}
		});
		
	}
	
	private void getTokenInfo() {
		GameCenterSDK.getInstance().doGetTokenAndSsoid(new ApiCallback() {
			@Override
			public void onSuccess(String resultMsg) {
				try {
					LogUtils.d("getTokenInfo,resultMsg:" + resultMsg);
					JSONObject json = new JSONObject(resultMsg);
					LoginInfo loginParam = new LoginInfo();
					loginParam.setChannelUserId(json.getString("ssoid"));
					loginParam.setUserToken(json.getString("token"));
					checkLogin(loginParam);
//					checkLogin(json.getString("ssoid"), json.getString("token"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(String content, int resultCode) {
				mTianyouCallback.onResult(TianyouCallback.CODE_LOGIN_FAILED, content + resultCode);
			}
		});
	}
	
	@Override
	public void doVerifiedInfo() {
		super.doVerifiedInfo();
		GameCenterSDK.getInstance().doGetVerifiedInfo(new ApiCallback() {
			
			@Override
			public void onSuccess(String msg) {
				JSONObject jsonObject = new JSONObject();
				try {
					jsonObject.put("age", msg);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				mTianyouCallback.onResult(TianyouCallback.CODE_VERIFIEDINFO_SUCCESS, jsonObject.toString());
			}
			
			@Override
			public void onFailure(String msg, int code) {
				mTianyouCallback.onResult(TianyouCallback.CODE_VERIFIEDINFO_FAILED, "实名认证失败");
			}
		});
	}

	@Override
	public void doExitGame() {
		GameCenterSDK.getInstance().onExit((Activity) mActivity, new GameExitCallback() {
			@Override
			public void exitGame() {
				mTianyouCallback.onResult(TianyouCallback.CODE_QUIT_SUCCESS, "");
			}
		});
	}

	@Override
	public void doResume() {
		GameCenterSDK.getInstance().onResume(mActivity);
	}
	
	@Override
	public void doPause() {
		GameCenterSDK.getInstance().onPause();
	}
}
