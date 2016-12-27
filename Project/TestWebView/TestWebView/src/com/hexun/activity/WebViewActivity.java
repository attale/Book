package com.hexun.activity;

import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.hexun.network.DataCleanManager;

public class WebViewActivity extends Activity {

	private WebView webView;
	private ProgressBar pb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.webview);
		webView = (WebView) findViewById(R.id.webView);
		
		pb = (ProgressBar)findViewById(R.id.pb_web);
	}

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		WebSettings webSettings = webView.getSettings();
		webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
		// WebView����web��Դ
		String ul = "http://t.hexunfsd.com/weixin/publicweb/home/index?knowChannel="+ "APP_LCK_ADR_KC" +"&random"+String.valueOf(new Random().nextInt(10000000));
		webView.loadUrl(ul);
		// ��assetsĿ¼����ļ���html
		// webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		webView.addJavascriptInterface(this, "javatojs");
		webView.getSettings().setJavaScriptEnabled(true);
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// ����ֵ��true��ʱ�����ȥWebView�򿪣�Ϊfalse����ϵͳ�����������������
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				// ��д�˷���������webview����https����
				super.onReceivedSslError(view, handler, error);
				handler.proceed();
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// ��ҳ����ؿ�ʼʱ���á�
				super.onPageStarted(view, url, favicon);
				webView.setVisibility(View.GONE);
				pb.setVisibility(View.VISIBLE);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// ��ҳ����ؽ���ʱ���á�
				super.onPageFinished(view, url);
				webView.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				// ���粻ͨ������ʧ��
				super.onReceivedError(view, errorCode, description, failingUrl);
				view.loadData("��������ʧ��,���Ժ�����!", "text/html; charset=utf-8",
						"utf-8");
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				// �ڼ���ҳ����Դʱ����ã�ÿһ����Դ������ͼƬ���ļ��ض������һ�Ρ�
				super.onLoadResource(view, url);
			}

		});

		// ��Ҫ�����������Ⱦ��ҳ��������������� ����WebView����Javascript�ĶԻ�����վͼ�꣬��վtitle�����ؽ��ȵ�
		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				setTitle(title);
			}
		});

	}

	@JavascriptInterface
	public void loginBack(final String custId, final String sessionKey,
			String userName, String phoneNum) {
		Log.i("WebViewActivity", "custId:" + custId + "==sessionKey:" + sessionKey
				+ "==userName:" + userName + "==phoneNum:" + phoneNum);
	}

	@JavascriptInterface
	public void reqDataFromApp() {
		webView.loadUrl("javascript:getDataToH5('" + "sessionKey" + "')");
	}

	@JavascriptInterface
	public void tradeBack() {
		finish();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		DataCleanManager.cleanInternalCache(this);
		DataCleanManager.cleanSharedPreference(this);

		// ���cookie���ɳ����������
		CookieSyncManager.createInstance(this);
		CookieSyncManager.getInstance().startSync();
		CookieManager.getInstance().removeAllCookie();
		Log.i("WebViewActivity", "<<<<<<<<<<<<<<<<<<<<<<<");
		webView.clearCache(true);
		webView.clearHistory();

		System.gc();
		finish();
		super.onStop();
	}

}