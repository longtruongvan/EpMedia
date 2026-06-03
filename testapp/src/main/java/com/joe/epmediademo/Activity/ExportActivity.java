package com.joe.epmediademo.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.joe.epmediademo.Application.MyApplication;
import com.joe.epmediademo.R;

import java.io.File;
import java.util.Locale;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

public class ExportActivity extends AppCompatActivity implements View.OnClickListener {

	private ImageView btn_back;
	private ImageView iv_export_preview;
	private TextView tv_export_res_tag;
	private TextView tv_export_duration_tag;

	private TextView tv_export_status;
	private TextView tv_export_percent;
	private ProgressBar progress_export;
	private TextView tv_estimated_size;

	private Button btn_res_720p;
	private Button btn_res_1080p;
	private Button btn_res_2k;
	private Button btn_res_4k;

	private Button btn_fps_24;
	private Button btn_fps_30;
	private Button btn_fps_60;

	private View btn_share_instagram;
	private View btn_share_tiktok;
	private View btn_share_youtube;
	private Button btn_trigger_export;

	// Intent configuration inputs
	private String videoUrl;
	private float trimStartSec;
	private float trimEndSec;
	private int selectedCropPreset;
	private int currentRotation;
	private boolean isMirror;
	private String subtitleText;
	private float subtitleXPercent;
	private float subtitleYPercent;
	private int videoWidth;
	private int videoHeight;

	// Export settings states
	private int selectedResMode = 1; // 0 = 720p, 1 = 1080p, 2 = 2K, 3 = 4K
	private int selectedFpsMode = 1; // 0 = 24, 1 = 30, 2 = 60
	private boolean isExporting = false;
	private String outputPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_export);

		// Read configuration extras
		Intent intent = getIntent();
		videoUrl = intent.getStringExtra("VIDEO_PATH");
		trimStartSec = intent.getFloatExtra("TRIM_START", 0f);
		trimEndSec = intent.getFloatExtra("TRIM_END", 0f);
		selectedCropPreset = intent.getIntExtra("CROP_PRESET", 0);
		currentRotation = intent.getIntExtra("ROTATION", 0);
		isMirror = intent.getBooleanExtra("MIRROR", false);
		subtitleText = intent.getStringExtra("SUBTITLE_TEXT");
		subtitleXPercent = intent.getFloatExtra("SUBTITLE_X", 50f);
		subtitleYPercent = intent.getFloatExtra("SUBTITLE_Y", 85f);
		videoWidth = intent.getIntExtra("VIDEO_WIDTH", 0);
		videoHeight = intent.getIntExtra("VIDEO_HEIGHT", 0);

		initView();
		loadVideoMetadataAndPreview();
		updateEstimatedSize();
	}

	private void initView() {
		btn_back = (ImageView) findViewById(R.id.btn_back);
		iv_export_preview = (ImageView) findViewById(R.id.iv_export_preview);
		tv_export_res_tag = (TextView) findViewById(R.id.tv_export_res_tag);
		tv_export_duration_tag = (TextView) findViewById(R.id.tv_export_duration_tag);

		tv_export_status = (TextView) findViewById(R.id.tv_export_status);
		tv_export_percent = (TextView) findViewById(R.id.tv_export_percent);
		progress_export = (ProgressBar) findViewById(R.id.progress_export);
		tv_estimated_size = (TextView) findViewById(R.id.tv_estimated_size);

		btn_res_720p = (Button) findViewById(R.id.btn_res_720p);
		btn_res_1080p = (Button) findViewById(R.id.btn_res_1080p);
		btn_res_2k = (Button) findViewById(R.id.btn_res_2k);
		btn_res_4k = (Button) findViewById(R.id.btn_res_4k);

		btn_fps_24 = (Button) findViewById(R.id.btn_fps_24);
		btn_fps_30 = (Button) findViewById(R.id.btn_fps_30);
		btn_fps_60 = (Button) findViewById(R.id.btn_fps_60);

		btn_share_instagram = findViewById(R.id.btn_share_instagram);
		btn_share_tiktok = findViewById(R.id.btn_share_tiktok);
		btn_share_youtube = findViewById(R.id.btn_share_youtube);
		btn_trigger_export = (Button) findViewById(R.id.btn_trigger_export);

		btn_back.setOnClickListener(this);
		btn_res_720p.setOnClickListener(this);
		btn_res_1080p.setOnClickListener(this);
		btn_res_2k.setOnClickListener(this);
		btn_res_4k.setOnClickListener(this);

		btn_fps_24.setOnClickListener(this);
		btn_fps_30.setOnClickListener(this);
		btn_fps_60.setOnClickListener(this);

		btn_share_instagram.setOnClickListener(this);
		btn_share_tiktok.setOnClickListener(this);
		btn_share_youtube.setOnClickListener(this);
		btn_trigger_export.setOnClickListener(this);

		findViewById(R.id.nav_home).setOnClickListener(this);
		findViewById(R.id.nav_templates).setOnClickListener(this);
		findViewById(R.id.nav_projects).setOnClickListener(this);
		findViewById(R.id.nav_ai).setOnClickListener(this);
		findViewById(R.id.nav_profile).setOnClickListener(this);

		// Initial selectors highlighted states
		updateResolutionButtons(1);
		updateFpsButtons(1);
	}

	private void loadVideoMetadataAndPreview() {
		if (videoUrl == null || videoUrl.isEmpty()) return;

		float duration = trimEndSec - trimStartSec;
		int secs = (int) duration % 60;
		int mins = (int) (duration / 60) % 60;
		tv_export_duration_tag.setText(String.format(Locale.US, "%02d:%02d", mins, secs));

		// Set Resolution tag text
		tv_export_res_tag.setText(getResName(selectedResMode));

		// Asynchronously load thumbnail frame from video path
		new Thread(new Runnable() {
			@Override
			public void run() {
				MediaMetadataRetriever retriever = new MediaMetadataRetriever();
				try {
					retriever.setDataSource(videoUrl);
					long timeUs = (long) (trimStartSec * 1000000L);
					final Bitmap bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
					if (bitmap != null) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								iv_export_preview.setImageBitmap(bitmap);
								iv_export_preview.setAlpha(0.6f);
							}
						});
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					try {
						retriever.release();
					} catch (Exception ignored) {}
				}
			}
		}).start();
	}

	private void updateEstimatedSize() {
		float duration = trimEndSec - trimStartSec;
		if (duration <= 0) duration = 10f; // fallback

		// Resolution weight: 720p = 1.0, 1080p = 2.0, 2K = 3.5, 4K = 7.0
		double resWeight = 1.0;
		if (selectedResMode == 1) resWeight = 2.0;
		else if (selectedResMode == 2) resWeight = 3.5;
		else if (selectedResMode == 3) resWeight = 7.0;

		// FPS weight: 24 = 0.8, 30 = 1.0, 60 = 1.6
		double fpsWeight = 1.0;
		if (selectedFpsMode == 0) fpsWeight = 0.8;
		else if (selectedFpsMode == 2) fpsWeight = 1.6;

		// Base MB per second multiplier (approx 0.8 MB/sec at 1080p 30fps)
		double estSizeMB = duration * 0.4 * resWeight * fpsWeight;
		tv_estimated_size.setText(getString(R.string.estimated_size_format, estSizeMB));
	}

	private String getResName(int mode) {
		switch (mode) {
			case 0: return "720p";
			case 2: return "2K";
			case 3: return "4K";
			case 1:
			default:
				return "1080p";
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btn_back) {
			if (isExporting) {
				Toast.makeText(this, R.string.toast_export_back_warning, Toast.LENGTH_SHORT).show();
			} else {
				finish();
			}
		} else if (id == R.id.btn_res_720p) {
			updateResolutionButtons(0);
		} else if (id == R.id.btn_res_1080p) {
			updateResolutionButtons(1);
		} else if (id == R.id.btn_res_2k) {
			updateResolutionButtons(2);
		} else if (id == R.id.btn_res_4k) {
			updateResolutionButtons(3);
		} else if (id == R.id.btn_fps_24) {
			updateFpsButtons(0);
		} else if (id == R.id.btn_fps_30) {
			updateFpsButtons(1);
		} else if (id == R.id.btn_fps_60) {
			updateFpsButtons(2);
		} else if (id == R.id.btn_trigger_export) {
			if (!isExporting) {
				startVideoExport();
			}
		} else if (id == R.id.btn_share_instagram) {
			shareVideo("com.instagram.android");
		} else if (id == R.id.btn_share_tiktok) {
			shareVideo("com.zhiliaoapp.musically");
		} else if (id == R.id.btn_share_youtube) {
			shareVideo("com.google.android.youtube");
		} else if (id == R.id.nav_home || id == R.id.nav_templates || id == R.id.nav_projects || id == R.id.nav_ai || id == R.id.nav_profile) {
			if (isExporting) {
				Toast.makeText(this, R.string.toast_export_back_warning, Toast.LENGTH_SHORT).show();
			} else {
				Intent intent = new Intent(this, MainActivity.class);
				intent.putExtra("TARGET_TAB", id);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
				finish();
			}
		}
	}

	private void updateResolutionButtons(int mode) {
		selectedResMode = mode;
		tv_export_res_tag.setText(getResName(mode));
		updateEstimatedSize();

		Button[] buttons = {btn_res_720p, btn_res_1080p, btn_res_2k, btn_res_4k};
		int activeColor = getResources().getColor(R.color.colorAccent);
		int activeTextColor = getResources().getColor(R.color.lumina_bg);
		int normalColor = getResources().getColor(R.color.lumina_surface_container);
		int normalTextColor = getResources().getColor(R.color.lumina_text_primary);

		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i] != null) {
				if (i == mode) {
					buttons[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
					buttons[i].setTextColor(activeTextColor);
				} else {
					buttons[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(normalColor));
					buttons[i].setTextColor(normalTextColor);
				}
			}
		}
	}

	private void updateFpsButtons(int mode) {
		selectedFpsMode = mode;
		updateEstimatedSize();

		Button[] buttons = {btn_fps_24, btn_fps_30, btn_fps_60};
		int activeColor = getResources().getColor(R.color.colorAccent);
		int activeTextColor = getResources().getColor(R.color.lumina_bg);
		int normalColor = getResources().getColor(R.color.lumina_surface_container);
		int normalTextColor = getResources().getColor(R.color.lumina_text_primary);

		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i] != null) {
				if (i == mode) {
					buttons[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
					buttons[i].setTextColor(activeTextColor);
				} else {
					buttons[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(normalColor));
					buttons[i].setTextColor(normalTextColor);
				}
			}
		}
	}

	private void startVideoExport() {
		if (videoUrl == null || videoUrl.isEmpty()) {
			Toast.makeText(this, R.string.toast_export_no_source, Toast.LENGTH_SHORT).show();
			return;
		}

		isExporting = true;
		btn_trigger_export.setEnabled(false);
		btn_trigger_export.setText(R.string.exporting_label);
		btn_trigger_export.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.lumina_surface_container)));
		btn_trigger_export.setTextColor(getResources().getColor(R.color.lumina_text_secondary));

		tv_export_status.setText(R.string.exporting_video_label);
		progress_export.setProgress(0);
		tv_export_percent.setText("0%");

		EpVideo epVideo = new EpVideo(videoUrl);

		// Apply trim clip
		epVideo.clip(trimStartSec, trimEndSec - trimStartSec);

		// Apply crop presets
		if (selectedCropPreset != 0 && videoWidth > 0 && videoHeight > 0) {
			int cw = videoWidth;
			int ch = videoHeight;
			int cx = 0;
			int cy = 0;
			
			if (selectedCropPreset == 1) { // 16:9
				ch = (int) (videoWidth * 9f / 16f);
				if (ch > videoHeight) {
					ch = videoHeight;
					cw = (int) (videoHeight * 16f / 9f);
				}
				cx = (videoWidth - cw) / 2;
				cy = (videoHeight - ch) / 2;
			} else if (selectedCropPreset == 2) { // 9:16
				cw = (int) (videoHeight * 9f / 16f);
				if (cw > videoWidth) {
					cw = videoWidth;
					ch = (int) (videoWidth * 16f / 9f);
				}
				cx = (videoWidth - cw) / 2;
				cy = (videoHeight - ch) / 2;
			} else if (selectedCropPreset == 3) { // 1:1
				int minDim = Math.min(videoWidth, videoHeight);
				cw = minDim;
				ch = minDim;
				cx = (videoWidth - minDim) / 2;
				cy = (videoHeight - minDim) / 2;
			}
			epVideo.crop(cw, ch, cx, cy);
		}

		// Apply rotate & mirror
		if (currentRotation != 0 || isMirror) {
			epVideo.rotation(currentRotation, isMirror);
		}

		// Apply subtitle overlay
		if (subtitleText != null && !subtitleText.trim().isEmpty()) {
			int targetX = (int) (videoWidth * (subtitleXPercent / 100f));
			int targetY = (int) (videoHeight * (subtitleYPercent / 100f));
			epVideo.addText(targetX, targetY, 36, "white", MyApplication.getSavePath() + "msyh.ttf", subtitleText);
		}

		outputPath = MyApplication.getSavePath() + "out.mp4";

		// Custom options can add resolution/bitrate flags based on settings (selectedResMode, selectedFpsMode)
		EpEditor.OutputOption opt = new EpEditor.OutputOption(outputPath);
		// Simple custom command mapping if desired
		if (selectedResMode == 0) {
			opt.setWidth(1280);
			opt.setHeight(720);
		} else if (selectedResMode == 1) {
			opt.setWidth(1920);
			opt.setHeight(1080);
		} else if (selectedResMode == 2) {
			opt.setWidth(2560);
			opt.setHeight(1440);
		} else if (selectedResMode == 3) {
			opt.setWidth(3840);
			opt.setHeight(2160);
		}

		if (selectedFpsMode == 0) opt.frameRate = 24;
		else if (selectedFpsMode == 1) opt.frameRate = 30;
		else if (selectedFpsMode == 2) opt.frameRate = 60;

		EpEditor.exec(epVideo, opt, new OnEditorListener() {
			@Override
			public void onSuccess() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						isExporting = false;
						progress_export.setProgress(100);
						tv_export_percent.setText("100%");
						tv_export_status.setText(R.string.export_completed_label);

						btn_trigger_export.setEnabled(true);
						btn_trigger_export.setText(R.string.open_exported_video);
						btn_trigger_export.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
						btn_trigger_export.setTextColor(getResources().getColor(R.color.lumina_bg));

						Toast.makeText(ExportActivity.this, getString(R.string.toast_export_success, outputPath), Toast.LENGTH_LONG).show();

						// Play video directly on completion
						openGeneratedVideo();
					}
				});
			}

			@Override
			public void onFailure() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						isExporting = false;
						tv_export_status.setText(R.string.export_failed_label);
						btn_trigger_export.setEnabled(true);
						btn_trigger_export.setText(R.string.retry_exporting);
						btn_trigger_export.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
						btn_trigger_export.setTextColor(getResources().getColor(R.color.lumina_bg));

						Toast.makeText(ExportActivity.this, R.string.toast_export_failed, Toast.LENGTH_SHORT).show();
					}
				});
			}

			@Override
			public void onProgress(final float v) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						int progress = (int) (v * 100);
						progress_export.setProgress(progress);
						tv_export_percent.setText(progress + "%");
					}
				});
			}
		});
	}

	private void openGeneratedVideo() {
		if (outputPath != null && new File(outputPath).exists()) {
			Intent v = new Intent(Intent.ACTION_VIEW);
			v.setDataAndType(Uri.parse(outputPath), "video/mp4");
			startActivity(v);
		}
	}

	private void shareVideo(String packageName) {
		if (outputPath == null || !new File(outputPath).exists()) {
			Toast.makeText(this, R.string.toast_export_share_first, Toast.LENGTH_SHORT).show();
			return;
		}

		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("video/mp4");
		share.putExtra(Intent.EXTRA_STREAM, Uri.parse(outputPath));
		share.setPackage(packageName);

		try {
			startActivity(share);
		} catch (Exception e) {
			Toast.makeText(this, R.string.toast_export_share_no_app, Toast.LENGTH_SHORT).show();
			
			// Show general sharing selector as fallback
			Intent generalShare = Intent.createChooser(share, getString(R.string.share_title));
			startActivity(generalShare);
		}
	}
}
