package com.joe.epmediademo.Activity;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.joe.epmediademo.Application.MyApplication;
import com.joe.epmediademo.R;
import com.joe.epmediademo.Utils.Media3TransformExporter;
import com.joe.epmediademo.Utils.PlatformVideoExporter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

public class ExportActivity extends AppCompatActivity implements View.OnClickListener {
	private static final String TAG = "ExportActivity";

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
	private java.util.ArrayList<String> videoUrls;
	private String audioPath;
	private float trimStartSec;
	private float trimEndSec;
	private int selectedCropPreset;
	private int currentRotation;
	private boolean isMirror;
	private String subtitleText;
	private float subtitleXPercent;
	private float subtitleYPercent;
	private float subtitleScale;
	private int videoWidth;
	private int videoHeight;
	private float speed;
	private int filterId;
	private int effectId;
	private int overlayId;
	private float videoVolume;
	private String stickerText;
	private int transitionId;
	private boolean isEnhanced;

	// Export settings states
	private int selectedResMode = 1; // 0 = 720p, 1 = 1080p, 2 = 2K, 3 = 4K
	private int selectedFpsMode = 1; // 0 = 24, 1 = 30, 2 = 60
	private boolean isExporting = false;
	private String outputPath;
	private Uri publishedOutputUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_export);

		// Read configuration extras
		Intent intent = getIntent();
		videoUrls = intent.getStringArrayListExtra("VIDEO_PATHS");
		audioPath = intent.getStringExtra("AUDIO_PATH");
		trimStartSec = intent.getFloatExtra("TRIM_START", 0f);
		trimEndSec = intent.getFloatExtra("TRIM_END", 0f);
		selectedCropPreset = intent.getIntExtra("CROP_PRESET", 0);
		currentRotation = intent.getIntExtra("ROTATION", 0);
		isMirror = intent.getBooleanExtra("MIRROR", false);
		subtitleText = intent.getStringExtra("SUBTITLE_TEXT");
		subtitleXPercent = intent.getFloatExtra("SUBTITLE_X", 50f);
		subtitleYPercent = intent.getFloatExtra("SUBTITLE_Y", 85f);
		subtitleScale = intent.getFloatExtra("SUBTITLE_SCALE", 1.0f);
		videoWidth = intent.getIntExtra("VIDEO_WIDTH", 0);
		videoHeight = intent.getIntExtra("VIDEO_HEIGHT", 0);
		speed = intent.getFloatExtra("SPEED", 1.0f);
		filterId = intent.getIntExtra("FILTER_ID", 0);
		effectId = intent.getIntExtra("EFFECT_ID", 3);
		overlayId = intent.getIntExtra("OVERLAY_ID", 3);
		videoVolume = intent.getFloatExtra("VIDEO_VOLUME", 1.0f);
		stickerText = intent.getStringExtra("STICKER_TEXT");
		transitionId = intent.getIntExtra("TRANSITION_ID", -1);
		isEnhanced = intent.getBooleanExtra("ENHANCE", false);

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
		View btn_export_action = findViewById(R.id.btn_export_action);
		if (btn_export_action != null) {
			btn_export_action.setOnClickListener(this);
		}

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
		if (videoUrls == null || videoUrls.isEmpty()) return;

		float duration = getDisplayDurationSec();
		int secs = (int) duration % 60;
		int mins = (int) (duration / 60) % 60;
		tv_export_duration_tag.setText(String.format(Locale.US, "%02d:%02d", mins, secs));

		// Set Resolution tag text
		tv_export_res_tag.setText(getResName(selectedResMode));

		if (!new java.io.File(videoUrls.get(0)).exists()) {
			iv_export_preview.setImageResource(android.R.drawable.ic_dialog_alert);
			iv_export_preview.setAlpha(0.45f);
			Toast.makeText(this, R.string.toast_export_no_source, Toast.LENGTH_SHORT).show();
		} else {
			// Asynchronously load thumbnail frame from video path
			new Thread(new Runnable() {
				@Override
				public void run() {
					MediaMetadataRetriever retriever = new MediaMetadataRetriever();
					try {
						retriever.setDataSource(videoUrls.get(0));
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
	}

	private void updateEstimatedSize() {
		float duration = getDisplayDurationSec();
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

	private float getDisplayDurationSec() {
		if (videoUrls == null || videoUrls.isEmpty()) {
			return 0f;
		}
		if (videoUrls.size() == 1 && trimEndSec > trimStartSec) {
			return trimEndSec - trimStartSec;
		}

		long durationMs = 0L;
		for (String path : videoUrls) {
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			try {
				retriever.setDataSource(path);
				String value = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
				if (value != null) {
					durationMs += Long.parseLong(value);
				}
			} catch (Exception ignored) {
			} finally {
				try {
					retriever.release();
				} catch (Exception ignored) {
				}
			}
		}
		return durationMs / 1000f;
	}

	private boolean hasMissingSource() {
		if (videoUrls == null || videoUrls.isEmpty()) {
			return true;
		}
		for (String path : videoUrls) {
			if (path == null || !new File(path).exists()) {
				return true;
			}
		}
		return false;
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
		} else if (id == R.id.btn_trigger_export || id == R.id.btn_export_action) {
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
		if (videoUrls == null || videoUrls.isEmpty()) {
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

		boolean isMissingSource = hasMissingSource();
		if (isMissingSource) {
			showExportFailure();
			Toast.makeText(this, R.string.toast_export_no_source, Toast.LENGTH_LONG).show();
			return;
		}

		outputPath = MyApplication.getSavePath() + "out.mp4";

		// Clean up any lingering files
		cleanupTempFiles();
		deleteFileSilently(outputPath);
		publishedOutputUri = null;

		if (hasUnsupportedAdvancedEdits()) {
			showUnsupportedAdvancedExport();
			return;
		}

		if (requiresTransformExporter()) {
			runTransformExport(outputPath);
		} else {
			runPlatformExport(outputPath);
		}
	}

	private boolean hasUnsupportedAdvancedEdits() {
		return videoUrls == null
				|| videoUrls.isEmpty()
				|| (videoUrls.size() > 1 && trimEndSec > trimStartSec)
				|| speed != 1.0f
				|| (audioPath != null && !audioPath.trim().isEmpty())
				|| transitionId != -1;
	}

	private boolean requiresTransformExporter() {
		if (videoUrls != null
				&& videoUrls.size() > 1
				&& selectedResMode == 1
				&& selectedFpsMode == 1
				&& selectedCropPreset == 0
				&& currentRotation == 0
				&& !isMirror
				&& !isEnhanced
				&& (subtitleText == null || subtitleText.trim().isEmpty())
				&& (stickerText == null || stickerText.trim().isEmpty())
				&& (filterId == 0 || filterId == R.id.btn_filter_none)
				&& effectId == 3
				&& overlayId == 3
				&& videoVolume == 1.0f) {
			return false;
		}
		return true;
	}

	private void runPlatformExport(String platformOutputPath) {
		PlatformVideoExporter.exportAsync(videoUrls, platformOutputPath, trimStartSec, trimEndSec,
				new PlatformVideoExporter.Listener() {
					@Override
					public void onProgress(final float progress) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								int percent = Math.max(0, Math.min(99, (int) (progress * 100f)));
								progress_export.setProgress(percent);
								tv_export_percent.setText(percent + "%");
							}
						});
					}

					@Override
					public void onSuccess() {
						publishedOutputUri = publishExportToGallery(platformOutputPath);
						showExportSuccess();
					}

					@Override
					public void onFailure(Exception exception) {
						Log.e(TAG, "Platform export failed", exception);
						showExportFailure();
					}
				});
	}

	private void runTransformExport(String transformOutputPath) {
		Media3TransformExporter.Config config = new Media3TransformExporter.Config();
		config.inputPaths = videoUrls;
		config.outputPath = transformOutputPath;
		config.trimStartSec = trimStartSec;
		config.trimEndSec = trimEndSec;
		config.outputHeight = getSelectedOutputHeight();
		config.outputFps = getSelectedOutputFps();
		config.cropPreset = selectedCropPreset;
		config.rotationDegrees = currentRotation;
		config.mirror = isMirror;
		config.enhance = isEnhanced;
		config.subtitleText = subtitleText;
		config.subtitleXPercent = subtitleXPercent;
		config.subtitleYPercent = subtitleYPercent;
		config.subtitleScale = subtitleScale;
		config.stickerText = stickerText;
		config.filterId = filterId;
		config.effectId = effectId;
		config.overlayId = overlayId;
		config.videoVolume = videoVolume;

		Media3TransformExporter.exportAsync(getApplicationContext(), config, new Media3TransformExporter.Listener() {
			@Override
			public void onProgress(final float progress) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						int percent = Math.max(0, Math.min(99, (int) (progress * 100f)));
						progress_export.setProgress(percent);
						tv_export_percent.setText(percent + "%");
					}
				});
			}

			@Override
			public void onSuccess() {
				publishedOutputUri = publishExportToGallery(transformOutputPath);
				showExportSuccess();
			}

			@Override
			public void onFailure(Exception exception) {
				Log.e(TAG, "Transform export failed", exception);
				showExportFailure();
			}
		});
	}

	private int getSelectedOutputHeight() {
		switch (selectedResMode) {
			case 0:
				return 720;
			case 2:
				return 1440;
			case 3:
				return 2160;
			case 1:
			default:
				return 1080;
		}
	}

	private int getSelectedOutputFps() {
		switch (selectedFpsMode) {
			case 0:
				return 24;
			case 2:
				return 60;
			case 1:
			default:
				return 30;
		}
	}

	private void showExportSuccess() {
		cleanupTempFiles();
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

				Toast.makeText(ExportActivity.this, getString(R.string.toast_export_success, getExportDisplayLocation()), Toast.LENGTH_LONG).show();

				// Play video directly on completion
				openGeneratedVideo();
			}
		});
	}

	private void showExportFailure() {
		cleanupTempFiles();
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

	private void showUnsupportedAdvancedExport() {
		cleanupTempFiles();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				isExporting = false;
				progress_export.setProgress(0);
				tv_export_percent.setText("0%");
				tv_export_status.setText(R.string.export_advanced_unsupported_label);
				btn_trigger_export.setEnabled(true);
				btn_trigger_export.setText(R.string.retry_exporting);
				btn_trigger_export.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
				btn_trigger_export.setTextColor(getResources().getColor(R.color.lumina_bg));

				Toast.makeText(ExportActivity.this, R.string.toast_export_advanced_unsupported, Toast.LENGTH_LONG).show();
			}
		});
	}

	private void cleanupTempFiles() {
		String savePath = MyApplication.getSavePath();
		deleteFileSilently(savePath + "temp_visuals.mp4");
		deleteFileSilently(savePath + "temp_speed.mp4");
		deleteFileSilently(savePath + "temp_volume.mp4");
		deleteFileSilently(savePath + "out_mixed.mp4");
	}

	private void deleteFileSilently(String path) {
		if (path != null) {
			try {
				File file = new File(path);
				if (file.exists()) {
					file.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void openGeneratedVideo() {
		if (outputPath != null && new File(outputPath).exists()) {
			Intent v = new Intent(Intent.ACTION_VIEW);
			v.setDataAndType(getShareableOutputUri(), "video/mp4");
			v.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
			startActivity(v);
		}
	}

	private void shareVideo(String packageName) {
		if (outputPath == null || !new File(outputPath).exists()) {
			Toast.makeText(this, R.string.toast_export_share_first, Toast.LENGTH_SHORT).show();
			return;
		}

		Intent share = buildShareIntent(packageName);

		try {
			startActivity(share);
		} catch (Exception e) {
			Toast.makeText(this, R.string.toast_export_share_no_app, Toast.LENGTH_SHORT).show();
			
			// Show general sharing selector as fallback
			Intent generalShare = Intent.createChooser(buildShareIntent(null), getString(R.string.share_title));
			startActivity(generalShare);
		}
	}

	private Intent buildShareIntent(String packageName) {
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("video/mp4");
		share.putExtra(Intent.EXTRA_STREAM, getShareableOutputUri());
		share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
		if (packageName != null) {
			share.setPackage(packageName);
		}
		return share;
	}

	private Uri getShareableOutputUri() {
		return publishedOutputUri != null ? publishedOutputUri : getOutputContentUri();
	}

	private Uri getOutputContentUri() {
		return FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", new File(outputPath));
	}

	private String getExportDisplayLocation() {
		return publishedOutputUri != null ? getString(R.string.export_gallery_location) : outputPath;
	}

	private Uri publishExportToGallery(String sourcePath) {
		if (sourcePath == null) {
			return null;
		}

		File source = new File(sourcePath);
		if (!source.exists()) {
			return null;
		}

		String displayName = "EpMedia_" + System.currentTimeMillis() + ".mp4";
		ContentValues values = new ContentValues();
		values.put(MediaStore.Video.Media.DISPLAY_NAME, displayName);
		values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
		values.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000L);
		values.put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000L);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/EpMedia");
			values.put(MediaStore.Video.Media.IS_PENDING, 1);
		}

		Uri collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
		Uri itemUri = null;
		try {
			itemUri = getContentResolver().insert(collection, values);
			if (itemUri == null) {
				return null;
			}

			OutputStream out = getContentResolver().openOutputStream(itemUri);
			if (out == null) {
				throw new IOException("Could not open gallery output stream");
			}
			copyFileToStream(source, out);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
				ContentValues pendingValues = new ContentValues();
				pendingValues.put(MediaStore.Video.Media.IS_PENDING, 0);
				getContentResolver().update(itemUri, pendingValues, null, null);
			}
			return itemUri;
		} catch (Exception e) {
			Log.w(TAG, "Could not publish export to gallery", e);
			if (itemUri != null) {
				try {
					getContentResolver().delete(itemUri, null, null);
				} catch (Exception ignored) {
				}
			}
			return null;
		}
	}

	private void copyFileToStream(File source, OutputStream out) throws IOException {
		FileInputStream in = new FileInputStream(source);
		try {
			byte[] buffer = new byte[1024 * 64];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			out.flush();
		} finally {
			try {
				in.close();
			} finally {
				out.close();
			}
		}
	}
}
