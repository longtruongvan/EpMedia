package com.joe.epmediademo.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.joe.epmediademo.R;
import com.joe.epmediademo.Utils.UriUtils;

import java.util.ArrayList;
import java.util.List;

public class MergeActivity extends AppCompatActivity implements View.OnClickListener {

	private static final int CHOOSE_FILE = 11;
	private TextView tv_add;
	private Button bt_add, bt_merge;
	private List<String> videoList;
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_merge);
		initView();
	}

	private void initView() {
		tv_add = (TextView) findViewById(R.id.tv_add);
		bt_add = (Button) findViewById(R.id.bt_add);
		bt_merge = (Button) findViewById(R.id.bt_merge);
		videoList = new ArrayList<>();
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMax(100);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setTitle("正在处理");
		bt_add.setOnClickListener(this);
		bt_merge.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_add:
				chooseFile();
				break;
			case R.id.bt_merge:
				mergeVideo();
				break;
		}
	}

	/**
	 * 选择文件
	 */
	private void chooseFile() {
		Intent intent = new Intent();
		intent.setType("video/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, CHOOSE_FILE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case CHOOSE_FILE:
				if (resultCode == RESULT_OK) {
					String videoUrl = UriUtils.getPath(MergeActivity.this, data.getData());
					tv_add.setText(tv_add.getText() + videoUrl + "\n");
					videoList.add(videoUrl);
					break;
				}
		}
	}

	/**
	 * 合并视频
	 */
	private void mergeVideo() {
		if (videoList.size() > 1) {
			Intent intent = new Intent(this, ExportActivity.class);
			intent.putStringArrayListExtra("VIDEO_PATHS", new ArrayList<>(videoList));
			startActivity(intent);
		} else {
			Toast.makeText(this, "至少添加两个视频", Toast.LENGTH_SHORT).show();
		}
	}
}
