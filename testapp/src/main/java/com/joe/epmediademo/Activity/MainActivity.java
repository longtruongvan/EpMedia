package com.joe.epmediademo.Activity;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.joe.epmediademo.R;

public class MainActivity extends AppCompatActivity {

	private static final int CHOOSE_VIDEO = 20;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findViewById(R.id.btn_new_project).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				chooseVideo();
			}
		});
	}

	private void chooseVideo() {
		Intent intent = new Intent();
		intent.setType("video/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		startActivityForResult(intent, CHOOSE_VIDEO);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CHOOSE_VIDEO && resultCode == RESULT_OK && data != null) {
			String videoPath = com.joe.epmediademo.Utils.UriUtils.getPath(MainActivity.this, data.getData());
			if (videoPath != null && !videoPath.isEmpty()) {
				Intent intent = new Intent(MainActivity.this, EditActivity.class);
				intent.putExtra("VIDEO_PATH", videoPath);
				startActivity(intent);
			} else {
				Toast.makeText(this, "Không thể đọc file video này", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
