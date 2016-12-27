package com.hexun.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class MainActivity extends Activity {

	private Button submit;

	private AnimationDrawable fightnimation, fightnimationab;
	private ImageView pb, net, netab;
	private Dialog mLoading;
	private Animation animation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		submit = (Button) findViewById(R.id.btn_login_submit);
		
		pb = (ImageView) findViewById(R.id.iv_failure);
		animation = AnimationUtils.loadAnimation(MainActivity.this,
				R.anim.cirle);
		animation.setInterpolator(new LinearInterpolator());

		netab = (ImageView) findViewById(R.id.iv_netab);
		netab.setBackgroundResource(R.anim.fight);
		fightnimationab = (AnimationDrawable) netab.getBackground();

		net = (ImageView) findViewById(R.id.iv_net);
		net.setBackgroundResource(R.anim.loading);
		fightnimation = (AnimationDrawable) net.getBackground();

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mLoading = createLoadingDialog(MainActivity.this);
				mLoading.show();
//				mLoading.dismiss();

				pb.startAnimation(animation);
				fightnimationab.start();
				fightnimation.start();

			}
		});
	}

	/**
	 * �õ��Զ����progressDialog
	 * 
	 * @param context
	 * @return
	 */
	public static Dialog createLoadingDialog(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.layout_loading_dialog, null); // �õ�����view
		LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view); // ���ز���
		ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
		Animation animation = AnimationUtils.loadAnimation(
				context, R.anim.cirle); // ���ض���
		animation.setInterpolator(new LinearInterpolator());
		spaceshipImage.startAnimation(animation); // ʹ��ImageView��ʾ����
		Dialog loadingDialog = new Dialog(context, R.style.loading_dialog); // �����Զ�����ʽdialog

		// loadingDialog.setCancelable(false);// ��������"���ؼ�"ȡ��
		loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		return loadingDialog;
	}
}