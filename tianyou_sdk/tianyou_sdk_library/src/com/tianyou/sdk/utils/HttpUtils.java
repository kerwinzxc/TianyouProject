package com.tianyou.sdk.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.tianyou.sdk.holder.ConstantHolder;

public class HttpUtils {

	private static ProgressBar progressBar;
	private static final OkHttpClient mClient;
	
	static {
		mClient = new OkHttpClient.Builder()
			.connectTimeout(3, TimeUnit.SECONDS)
			.readTimeout(3, TimeUnit.SECONDS)
			.writeTimeout(3, TimeUnit.SECONDS).build();
	}
	
	// 图片加载
	public static void imageLoad(final Activity activity, String url, final ImageView view) {
		createProgress(activity);
		Request request = new Request.Builder().url(url).build();
		mClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Call arg0, Response response) throws IOException {
				final Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progressBar.setVisibility(View.GONE);
						view.setImageBitmap(bitmap);
					}
				});
			}
			
			@Override
			public void onFailure(Call callback, IOException arg1) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ToastUtils.show(activity, "网络连接失败，请检查网络~");
						progressBar.setVisibility(View.GONE);
					}
				});
			}
		});
	}
	
	// 图片下载
	public static void imageDown(final Activity activity, String url, final ImageView view,final String photoName){
		createProgress(activity);
		Request request = new Request.Builder().url(url).build();
		mClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Call arg0, Response response) throws IOException {
				final Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
				// 创建文件对象，用来存储新的图像文件
				File dir = new File(ConstantHolder.PATH_IMAGE);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				File file = new File(ConstantHolder.PATH_IMAGE, photoName + ".png");
				// 创建文件
				file.createNewFile();
				// 定义文件输出流
				FileOutputStream fout = new FileOutputStream(file);
				// 将bitmap存储为jpg格式的图片
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, fout);
				fout.flush();// 刷新文件流
				fout.close();
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						view.setImageBitmap(bitmap);
						Toast.makeText(activity, "二维码已自动保存到相册", Toast.LENGTH_SHORT).show();
						progressBar.setVisibility(View.GONE);
					}
				});
				// 文件存储已经完毕，保存的图片没有加入到系统图库中
				// ，此时需要发送广播，刷新图库，很简单几行代码搞定
				Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
				Uri uri = Uri.fromFile(file);
				intent.setData(uri);
				activity.sendBroadcast(intent);
			}
			
			@Override
			public void onFailure(Call callback, IOException arg1) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ToastUtils.show(activity, "网络连接失败，请检查网络~");
						progressBar.setVisibility(View.GONE);
					}
				});
			}
		});
	}

	// post请求
	public static void post(final Activity activity, final String url, final Map<String, String> map, final HttpCallback callback) {
		LogUtils.d("请求URL:" + url);
//		createProgress(activity);
		LogUtils.d("请求参数:" + map);
		Builder builder = new FormBody.Builder();
		for (Entry<String, String> entry : map.entrySet()) {
			builder.add(entry.getKey(), entry.getValue());
		}
		Request request = new Request.Builder().url(url).post(builder.build()).build();
		mClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Call call, final Response response) throws IOException {
				final String result = response.body().string();
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						LogUtils.d("请求结果:" + result);
						callback.onSuccess(result);
						progressBar.setVisibility(View.GONE);
					}
				});
			}
			
			@Override
			public void onFailure(Call arg0, IOException arg1) {
				callback.onFailed();
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ToastUtils.show(activity, "网络连接失败，请检查网络~");
						progressBar.setVisibility(View.GONE);
					}
				});
			}
		});
	}
	
	// post请求
	public static void post(final Activity activity, final String url, final Map<String, String> map, final HttpsCallback callback) {
		LogUtils.d("请求URL:" + url);
		createProgress(activity);
		Builder builder = new FormBody.Builder();
		if (map != null) {
			LogUtils.d("请求参数:" + map);
			for (Entry<String, String> entry : map.entrySet()) {
				builder.add(entry.getKey(), entry.getValue());
			}
		}
		Request request = new Request.Builder().url(url).post(builder.build()).build();
		mClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Call call, final Response response) throws IOException {
				final String result = response.body().string();
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progressBar.setVisibility(View.GONE);
						LogUtils.d("请求结果:" + result);
						callback.onSuccess(result);
					}
				});
			}
			
			@Override
			public void onFailure(Call arg0, IOException arg1) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						ToastUtils.show(activity, "网络连接失败，请检查网络~");
						progressBar.setVisibility(View.GONE);
					}
				});
			}
		});
	}
	
	// get请求
	public static void get(final Activity activity, final String url, final HttpsCallback callback) {
		LogUtils.d("请求URL:" + url);
		createProgress(activity);
		Request request = new Request.Builder().url(url).build();
		mClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onResponse(Call call, final Response response) throws IOException {
				final String result = response.body().string();
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progressBar.setVisibility(View.GONE);
						LogUtils.d("请求结果:" + result);
						callback.onSuccess(result);
					}
				});
			}
			
			@Override
			public void onFailure(Call arg0, IOException arg1) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						progressBar.setVisibility(View.GONE);
					}
				});
			}
		});
	}

	// 进度条
	private static void createProgress(Activity activity) {
		if (progressBar == null) {
			progressBar = new ProgressBar(activity);
			RelativeLayout layout = new RelativeLayout(activity);
			layout.addView(progressBar);
			layout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
			activity.addContentView(layout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
		progressBar.setVisibility(View.VISIBLE);
	}

	/**
	 * 请求回调接口
	 * @author itstrong
	 *
	 */
	public interface HttpCallback {

		void onSuccess(String response);

		void onFailed();
	}
	
	/**
	 * 只有成功的回调接口
	 * @author itstrong
	 *
	 */
	public interface HttpsCallback {

		void onSuccess(String response);
	}
	
	/**
	 * 检测手机网络是否连接
	 * @param activity
	 * @return
	 */
	public static boolean isNetConnected(Activity activity) {
		ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm != null) {
			NetworkInfo[] infos = cm.getAllNetworkInfo();
			if (infos != null) {
				for (NetworkInfo ni : infos) {
					if (ni.isConnected())
						return true;
				}
			}
		}
		return false;
	}
}