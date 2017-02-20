package com.tianyou.sdk.activity;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.tianyou.sdk.base.BaseActivity;
import com.tianyou.sdk.fragment.pay.FailedFragment;
import com.tianyou.sdk.fragment.pay.HomeFragment;
import com.tianyou.sdk.fragment.pay.NetworkFragment;
import com.tianyou.sdk.fragment.pay.RemitFragment;
import com.tianyou.sdk.fragment.pay.ServiceFragment;
import com.tianyou.sdk.fragment.pay.SuccessFragment;
import com.tianyou.sdk.fragment.pay.WalletFragment;
import com.tianyou.sdk.fragment.pay.WxScanFragment;
import com.tianyou.sdk.holder.ConfigHolder;
import com.tianyou.sdk.holder.PaymentHandler;
import com.tianyou.sdk.holder.URLHolder;
import com.tianyou.sdk.utils.AppUtils;
import com.tianyou.sdk.utils.HttpUtils;
import com.tianyou.sdk.utils.HttpUtils.HttpCallback;
import com.tianyou.sdk.utils.LogUtils;
import com.tianyou.sdk.utils.PayResult;
import com.tianyou.sdk.utils.ResUtils;

/**
 * 支付Activity
 * @author itstrong
 *
 */
public class PayActivity extends BaseActivity {

	public PaymentHandler mPayHandler;
	
	public  int ACTIVITY_FINISH = 1;
	
	public IInAppBillingService mBillingService;
	protected ServiceConnection mServiceConn;
	
	private static final int REQUEST_CODE_PAYMENT = 1;
    private static PayPalConfiguration config = new PayPalConfiguration()
            // 沙盒测试(ENVIRONMENT_SANDBOX)，生产环境(ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            //你创建的测试应用Client ID
            //.clientId("AY0e-gWL3dZS4lNWlnJsq1nDgXAgV5WA_cpLrrnnxzVt9IbsMWl9-BelxC1sTlHAiGmk8dpT2Nda172n");
            .clientId("AdNWcWrNwNjGM2qeP4fuddkj9apsDWEvlmuJD6h-Sf0ZgD7wqJQIbzBCMp05mJBcJt29-RAaYjb-D-J2");

	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				PayResult payResult = new PayResult((String) msg.obj);
		        String resultStatus = payResult.getResultStatus();
		        if (TextUtils.equals(resultStatus, "9000")) {
		        	PaymentHandler.getInstance(mActivity, mHandler).doQueryOrder();
		        	mPayHandler.doQueryOrder();
		        } else {
		            if (TextUtils.equals(resultStatus, "8000")) {
		            	switchFragment(new SuccessFragment(), "SuccessFragment");
		                Toast.makeText(mActivity, "支付结果确认中", Toast.LENGTH_SHORT).show();
		            } else {
		            	switchFragment(new FailedFragment(), "FailedFragment");
		            }
		        }
				break;
			case 2:
				switchFragment(new WalletFragment(), "WalletFragment");
				break;
			case 3:
				switchFragment(new SuccessFragment(), "SuccessFragment");
				break;
			case 4:
				switchFragment(new FailedFragment(), "FailedFragment");
				break;
			case 5:
				switchFragment(new NetworkFragment(), "NetworkFragment");
				break;
			case 6:
				switchFragment(new RemitFragment(), "RemitFragment");
				break;
			case 7:
				switchFragment(new WxScanFragment(), "WxScanFragment");
				break;
			case 8:
				try {
//                	LogUtils.d("packageName= "+mActivity.getPackageName()+",ggproductid= "+getGoogleProductID()+",orderid= "+mPayInfo.getOrderId());
//                	LogUtils.d("mBillingService= "+mBillingService);
                    Bundle buyIntentBundle = mBillingService.getBuyIntent(3,mActivity.getPackageName(),
                    		mPayHandler.mPayInfo.getGoogleProductID(),"inapp",mPayHandler.mPayInfo.getOrderId());
                    int code = buyIntentBundle.getInt("RESPONSE_CODE");
                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                    startIntentSenderForResult(pendingIntent.getIntentSender(),
                            1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0),
                            Integer.valueOf(0));
                } catch (Exception e) { 
                	e.printStackTrace();
                	LogUtils.d(e.getMessage());
            	}
				break;
			}
		};
	};
	
	@Override
	protected int setContentView() {
		return ResUtils.getResById(this, "activity_pay", "layout");
	}
	
	@Override
	protected void initView() {
		findViewById(ResUtils.getResById(this, "img_pay_last", "id")).setOnClickListener(this);
        findViewById(ResUtils.getResById(this, "img_pay_question", "id")).setOnClickListener(this);
        findViewById(ResUtils.getResById(this, "img_pay_close", "id")).setOnClickListener(this);
	}

	@Override
	protected void initData() {
		
		// 谷歌支付
		mServiceConn = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName componentName, android.os.IBinder service) {
				mBillingService = IInAppBillingService.Stub.asInterface(service);
			}
			@Override
			public void onServiceDisconnected(ComponentName componentName) { mBillingService = null; }
		};
		Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
		serviceIntent.setPackage("com.android.vending");
		mActivity.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
		
		 // PayPal支付
        Intent paypalIntent = new Intent(this, PayPalService.class);
        paypalIntent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(paypalIntent);
		
		Intent intent = getIntent();
		mPayHandler = PaymentHandler.getInstance(mActivity, mHandler);
		mPayHandler.receivePayParam(intent.getStringExtra("payInfo"));
		int money = intent.getIntExtra("money", -1);
		mPayHandler.mIsShowChoose = money == -1;
		LogUtils.d("isChooseMoney:" + mPayHandler.mIsShowChoose);
		if (!mPayHandler.mIsShowChoose) {
			mPayHandler.mPayInfo.setMoney(money + "");
			mPayHandler.mPayInfo.setProductDesc(intent.getStringExtra("productDesc"));
		}
		switchFragment(new HomeFragment(), "HomeFragment");
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == ResUtils.getResById(this, "img_pay_question", "id")) {
			if (!"ServiceFragment".equals(mFragmentTag)) {				
        		switchFragment(new ServiceFragment(), "ServiceFragment");
			}
        } else if (v.getId() == ResUtils.getResById(this, "img_pay_close", "id")) {
        	if ("FailedFragment".equals(mFragmentTag)){
        		AppUtils.showFinishPayDialog(this,true);
        	} else {
        		finish();
        	}
        } else if (v.getId() == ResUtils.getResById(this, "img_pay_last", "id"))
        	if ("FailedFragment".equals(mFragmentTag)){
        		AppUtils.showFinishPayDialog(this, false);
        	} else {
        		onBackPressed();
        	}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data == null) return;
        String respCode = data.getExtras().getString("resultCode");
		if (!TextUtils.isEmpty(respCode) && respCode.equalsIgnoreCase("success")) {
			mPayHandler.doQueryOrder();
		} else {
			switchFragment(new FailedFragment(), "FailedFragment");
		}
		
		if (requestCode == ACTIVITY_FINISH && resultCode == MenuActivity.REQUEST_OK){
			finish();
		}
		
		// 谷歌支付结果回调
		if (requestCode == 1001) {
            int responseCode = data.getIntExtra("RESPONSE_CODE", 0);
            LogUtils.d("pay responsecode= " + responseCode);
            final String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
            LogUtils.d("purchaseData= " + purchaseData + ",dataSignature= " + dataSignature);
            LogUtils.d("result code= " + resultCode);
            if (resultCode == mActivity.RESULT_OK) {
            	new Thread(new Runnable() {
        			@Override
        			public void run() {
        				// 消耗谷歌商品
        				try {
        					JSONObject dataObject = new JSONObject(purchaseData);
        					String purchaseToken = dataObject.getString("purchaseToken");
        					LogUtils.d("purchaseToken= "+purchaseToken);
        					int response = mBillingService.consumePurchase(3, mActivity.getPackageName(), purchaseToken);
        					LogUtils.d("packageName= "+mActivity.getPackageName()+",purchaseToken= "+purchaseToken);
        					LogUtils.d("onActivityRestul response= "+response);
        				} catch (Exception e) {
        					LogUtils.d("consumePurchase= "+e.getMessage());
        					e.printStackTrace();
        				}
        			}
        		}).start();
            	// 校验谷歌订单
            	checkGoogleOrder(purchaseData, dataSignature);
            }
		} else { LogUtils.d("Google pay resultCode != 1001"); mHandler.sendEmptyMessage(4); }
		
		// PayPal支付回调
		if (resultCode == Activity.RESULT_OK) {
            Log.d("TAG","activity result_ok--------------");
            PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
            if (confirm != null) {
                try {
                    String paymentId = confirm.toJSONObject().getJSONObject("response").getString("id");
                    Log.d("TAG","paymentId= "+paymentId);
                    checkPaypalOrder(paymentId);
                } catch (JSONException e) {
                    Log.e("paymentExample", "an extremely unlikely failure occurred: ", e);
                }
            }
        } 
		else if (resultCode == Activity.RESULT_CANCELED) { Log.i("paymentExample", "The user canceled.");} 
		else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {Log.i("paymentExample", "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");} 
		else { Toast.makeText(this,"unkown error",Toast.LENGTH_LONG).show();}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mFragmentTag.equals("FailedFragment")){
			FailedFragment.onKeyDown(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void checkPaypalOrder(String paymentId) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("orderid",mPayHandler.mPayInfo.getOrderId());
		param.put("appID", ConfigHolder.GAME_ID);
		param.put("sign", AppUtils.MD5(ConfigHolder.USER_ACCOUNT+ConfigHolder.GAME_ID+mPayHandler.mPayInfo.getServerId()));
		param.put("payment_id", paymentId);
		HttpUtils.post(mActivity, URLHolder.URL_CHECK_ORDER_PAYPAL, param, new HttpUtils.HttpsCallback() {
			
			@Override
			public void onSuccess(String response) {
				Log.d("TAG", "paypal success = "+response);
				try {
					JSONObject jsonObject = new JSONObject(response);
					JSONObject result = jsonObject.getJSONObject("result");
					if ("200".equals(result.getString("code"))) {
						mHandler.sendEmptyMessage(3);
					} else {
						mHandler.sendEmptyMessage(4);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void checkGoogleOrder(String purchaseData,String dataSignature){
		Map<String, String> param = new HashMap<String, String>();
		param.put("appID", ConfigHolder.GAME_ID);
		param.put("sign",AppUtils.MD5(ConfigHolder.GAME_ID+mPayHandler.mPayInfo.getOrderId()));
		param.put("inapp_purchase_data",purchaseData);
		param.put("inapp_data_signature",dataSignature);
		LogUtils.d("google pay param= "+param);

		HttpUtils.post(mActivity, URLHolder.URL_CHECK_ORDER_GOOGLE, param, new HttpUtils.HttpsCallback() {
			@Override
			public void onSuccess(String data) {
				Log.d("TAG","pay success data= "+data);
				try {
					JSONObject jsonObject = new JSONObject(data);
					JSONObject result = jsonObject.getJSONObject("result");
					if ("200".equals(result.getString("code"))) {
						mHandler.sendEmptyMessage(3);
					} else {
						mHandler.sendEmptyMessage(4);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		});
	}

}