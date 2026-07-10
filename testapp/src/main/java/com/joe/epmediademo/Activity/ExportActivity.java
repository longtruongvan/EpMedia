package com.joe.epmediademo.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.joe.epmediademo.Application.MyApplication;
import com.joe.epmediademo.R;
import com.joe.epmediademo.Utils.PlatformVideoExporter;

import java.io.File;
import java.util.Locale;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

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

	// Pipeline helper states
	private float p1Start, p1End;
	private float p2Start, p2End;
	private float p3Start, p3End;
	private float p4Start, p4End;
	private boolean needSpeed;
	private boolean needVolume;
	private boolean needMusic;
	private boolean hasAudio;

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

		float duration = trimEndSec - trimStartSec;
		int secs = (int) duration % 60;
		int mins = (int) (duration / 60) % 60;
		tv_export_duration_tag.setText(String.format(Locale.US, "%02d:%02d", mins, secs));

		// Set Resolution tag text
		tv_export_res_tag.setText(getResName(selectedResMode));

		if (videoUrls.get(0).startsWith("mock_") || !new java.io.File(videoUrls.get(0)).exists()) {
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

		boolean isMock = videoUrls.get(0).startsWith("mock_") || !new java.io.File(videoUrls.get(0)).exists();
		if (isMock) {
			showExportFailure();
			Toast.makeText(this, R.string.toast_export_no_source, Toast.LENGTH_LONG).show();
			return;
		}

		// Detect if video has audio stream
		hasAudio = false;
		android.media.MediaExtractor extractor = new android.media.MediaExtractor();
		try {
			extractor.setDataSource(videoUrls.get(0));
			int numTracks = extractor.getTrackCount();
			for (int i = 0; i < numTracks; i++) {
				android.media.MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.getString(android.media.MediaFormat.KEY_MIME);
				if (mime.startsWith("audio/")) {
					hasAudio = true;
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			extractor.release();
		}

		needSpeed = (speed != 1.0f);
		needVolume = (hasAudio && videoVolume != 1.0f);
		needMusic = (audioPath != null && !audioPath.isEmpty());

		// Compute weights and progress ranges
		float visualWeight = 5.0f;
		float speedWeight = needSpeed ? 2.0f : 0.0f;
		float volumeWeight = needVolume ? 1.0f : 0.0f;
		float musicWeight = needMusic ? 2.0f : 0.0f;
		float totalWeight = visualWeight + speedWeight + volumeWeight + musicWeight;

		p1Start = 0f;
		p1End = (visualWeight / totalWeight) * 100f;

		p2Start = p1End;
		p2End = p2Start + (speedWeight / totalWeight) * 100f;

		p3Start = p2End;
		p3End = p3Start + (volumeWeight / totalWeight) * 100f;

		p4Start = p3End;
		p4End = 100f;

		outputPath = MyApplication.getSavePath() + "out.mp4";

		// Determine intermediate paths
		final String pathVisuals = (needSpeed || needVolume || needMusic) ? (MyApplication.getSavePath() + "temp_visuals.mp4") : outputPath;
		final String pathSpeed = (needVolume || needMusic) ? (MyApplication.getSavePath() + "temp_speed.mp4") : outputPath;
		final String pathVolume = needMusic ? (MyApplication.getSavePath() + "temp_volume.mp4") : outputPath;

		// Clean up any lingering files
		cleanupTempFiles();
		deleteFileSilently(outputPath);

		if (canUsePlatformExporter()) {
			runPlatformExport(videoUrls.get(0), outputPath);
			return;
		}

		java.util.List<EpVideo> epVideos = new java.util.ArrayList<>();
		if (videoUrls != null && !videoUrls.isEmpty()) {
			for (int i = 0; i < videoUrls.size(); i++) {
				EpVideo epVideo = new EpVideo(videoUrls.get(i));
		
				// Apply trim clip
				if (i == 0 && (trimStartSec > 0 || trimEndSec > 0)) epVideo.clip(trimStartSec, trimEndSec - trimStartSec);
		
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
		
				// Apply color filters
				if (filterId == R.id.btn_filter_warm) {
					epVideo.addFilter("colorbalance=rh=0.1:gh=0.05:bh=-0.1");
				} else if (filterId == R.id.btn_filter_cool) {
					epVideo.addFilter("colorbalance=rh=-0.1:gh=0.0:bh=0.15");
				} else if (filterId == R.id.btn_filter_vintage) {
					epVideo.addFilter("colorbalance=rh=0.15:gh=0.05:bh=-0.05,eq=saturation=0.8");
				}
		
				// Apply visual effects
				if (effectId == 0) { // Glitch VHS
					epVideo.addFilter("hue=h=30:s=1.5,noise=alls=15:allf=t+u");
				} else if (effectId == 1) { // Cinematic Light Leak
					epVideo.addFilter("colorbalance=rh=0.1:gh=-0.05:bh=-0.05,eq=contrast=1.15:saturation=1.1");
				} else if (effectId == 2) { // Neon Cyberpunk Glow
					epVideo.addFilter("colorbalance=rh=0.25:gh=-0.1:bh=0.25,hue=s=1.4");
				}
		
				// Apply overlays
				if (overlayId == 0) { // Vignette Shadow
					epVideo.addFilter("vignette=PI/4");
				} else if (overlayId == 1) { // Retro Vignette
					epVideo.addFilter("vignette='PI/3':eval=frame,eq=saturation=0.7");
				} else if (overlayId == 2) { // Cinematic Letterbox (Black Bars)
					epVideo.addFilter("drawbox=y=0:h=ih/8:color=black:t=fill,drawbox=y=ih-ih/8:h=ih/8:color=black:t=fill");
				}
		
				// Apply Transitions (Fade / Zoom / Wipe)
				float duration = trimEndSec - trimStartSec;
				if (duration <= 0) {
					duration = 10f; // fallback
				}
				if (transitionId == 0) { // Fade to Black
					float fadeStart = Math.max(0f, duration - 1.0f);
					epVideo.addFilter("fade=t=in:st=0:d=1,fade=t=out:st=" + fadeStart + ":d=1");
				} else if (transitionId == 1) { // Cross Dissolve (mock with a soft color transition or fade in/out)
					epVideo.addFilter("fade=t=in:st=0:d=1.5");
				} else if (transitionId == 2) { // Wipe Left / Slide
					epVideo.addFilter("scroll=horizontal=0.0005");
				} else if (transitionId == 3) { // Zoom In
					epVideo.addFilter("zoompan=z='min(zoom+0.001,1.3)':d=125:x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s=1280x720");
				}
		
				// Apply Enhance filter (sharpen)
				if (isEnhanced) {
					epVideo.addFilter("unsharp=5:5:1.0:5:5:0.0");
				}
		
				// Apply sticker emoji overlay
				if (stickerText != null && !stickerText.trim().isEmpty()) {
					int cx = videoWidth > 0 ? (videoWidth / 2 - 24) : 100;
					int cy = videoHeight > 0 ? (videoHeight / 2 - 24) : 100;
					epVideo.addText(cx, cy, 48, "white", MyApplication.getSavePath() + "msyh.ttf", stickerText);
				}
		
				// Apply subtitle overlay
				if (subtitleText != null && !subtitleText.trim().isEmpty()) {
					int targetX = (int) (videoWidth * (subtitleXPercent / 100f));
					int targetY = (int) (videoHeight * (subtitleYPercent / 100f));
					int fontSize = (int) (36 * subtitleScale);
					if (fontSize < 10) fontSize = 10;
					if (fontSize > 200) fontSize = 200;
					epVideo.addText(targetX, targetY, fontSize, "white", MyApplication.getSavePath() + "msyh.ttf", subtitleText);
				}
		
				// Custom options can add resolution/bitrate flags based on settings (selectedResMode, selectedFpsMode)

		
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						tv_export_status.setText("Áp dụng bộ lọc và hiệu ứng...");
					}
				});
		
				epVideos.add(epVideo);
			}
		}
				EpEditor.OutputOption opt = new EpEditor.OutputOption(pathVisuals);
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

		OnEditorListener commonListener = new OnEditorListener() {
			@Override
			public void onSuccess() {
				if (needSpeed) {
					runSpeedStage(pathVisuals, pathSpeed, hasAudio, needVolume, needMusic);
				} else if (needVolume) {
					runVolumeStage(pathVisuals, pathVolume, needMusic);
				} else if (needMusic) {
					runMusicStage(pathVisuals);
				} else {
					showExportSuccess();
				}
			}

			@Override
			public void onFailure() {
				showExportFailure();
			}

			@Override
			public void onProgress(final float v) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						int progress = (int) (p1Start + v * (p1End - p1Start));
						progress_export.setProgress(progress);
						tv_export_percent.setText(progress + "%");
					}
				});
			}
		};

		if (epVideos.size() == 1) {
			runEditorSafely(new Runnable() {
				@Override
				public void run() {
					EpEditor.exec(epVideos.get(0), opt, commonListener);
				}
			});
		} else if (epVideos.size() > 1) {
			runEditorSafely(new Runnable() {
				@Override
				public void run() {
					EpEditor.merge(epVideos, opt, commonListener);
				}
			});
		}
	}

	private void runEditorSafely(Runnable editorCall) {
		try {
			editorCall.run();
		} catch (Throwable t) {
			Log.e(TAG, "Video export engine failed to start", t);
			showExportFailure();
		}
	}

	private boolean canUsePlatformExporter() {
		return videoUrls != null
				&& videoUrls.size() == 1
				&& selectedCropPreset == 0
				&& currentRotation == 0
				&& !isMirror
				&& (subtitleText == null || subtitleText.trim().isEmpty())
				&& (stickerText == null || stickerText.trim().isEmpty())
				&& speed == 1.0f
				&& videoVolume == 1.0f
				&& (audioPath == null || audioPath.trim().isEmpty())
				&& (filterId == 0 || filterId == R.id.btn_filter_none)
				&& effectId == 3
				&& overlayId == 3
				&& transitionId == -1
				&& !isEnhanced;
	}

	private void runPlatformExport(String inputPath, String platformOutputPath) {
		PlatformVideoExporter.exportAsync(inputPath, platformOutputPath, trimStartSec, trimEndSec,
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
						showExportSuccess();
					}

					@Override
					public void onFailure(Exception exception) {
						Log.e(TAG, "Platform export failed", exception);
						showExportFailure();
					}
				});
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

				Toast.makeText(ExportActivity.this, getString(R.string.toast_export_success, outputPath), Toast.LENGTH_LONG).show();

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

	private String copyAssetToTempFile(String assetPath) {
		if (assetPath != null && !assetPath.startsWith("audio/")) {
			return assetPath;
		}
		try {
			java.io.InputStream in = getAssets().open(assetPath);
			java.io.File tempFile = new java.io.File(getCacheDir(), "temp_bg_music.mp3");
			java.io.OutputStream out = new java.io.FileOutputStream(tempFile);
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			out.flush();
			out.close();
			return tempFile.getAbsolutePath();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void runSpeedStage(final String inputPath, final String outputPathStage, final boolean hasAudioState, final boolean needVolumeState, final boolean needMusicState) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tv_export_status.setText("Điều chỉnh tốc độ...");
			}
		});

		EpEditor.PTS ptsType = hasAudioState ? EpEditor.PTS.ALL : EpEditor.PTS.VIDEO;

		runEditorSafely(new Runnable() {
			@Override
			public void run() {
				EpEditor.changePTS(inputPath, outputPathStage, speed, ptsType, new OnEditorListener() {
					@Override
					public void onSuccess() {
						deleteFileSilently(inputPath);
						if (needVolumeState) {
							runVolumeStage(outputPathStage, needMusicState ? (MyApplication.getSavePath() + "temp_volume.mp4") : outputPath, needMusicState);
						} else if (needMusicState) {
							runMusicStage(outputPathStage);
						} else {
							showExportSuccess();
						}
					}

					@Override
					public void onFailure() {
						showExportFailure();
					}

					@Override
					public void onProgress(final float v) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								int progress = (int) (p2Start + v * (p2End - p2Start));
								progress_export.setProgress(progress);
								tv_export_percent.setText(progress + "%");
							}
						});
					}
				});
			}
		});
	}

	private void runVolumeStage(final String inputPath, final String outputPathStage, final boolean needMusicState) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tv_export_status.setText("Điều chỉnh âm lượng...");
			}
		});

		long durationUs = 0;
		try {
			android.media.MediaMetadataRetriever retriever = new android.media.MediaMetadataRetriever();
			retriever.setDataSource(inputPath);
			String durStr = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
			if (durStr != null) {
				durationUs = Long.parseLong(durStr) * 1000;
			}
			retriever.release();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (durationUs <= 0) {
			durationUs = 10 * 1000000; // fallback 10s
		}

		final long commandDurationUs = durationUs;
		final String cmd = "-y -i " + inputPath + " -filter:a volume=" + videoVolume + " -c:v copy " + outputPathStage;

		runEditorSafely(new Runnable() {
			@Override
			public void run() {
				EpEditor.execCmd(cmd, commandDurationUs, new OnEditorListener() {
					@Override
					public void onSuccess() {
						deleteFileSilently(inputPath);
						if (needMusicState) {
							runMusicStage(outputPathStage);
						} else {
							showExportSuccess();
						}
					}

					@Override
					public void onFailure() {
						showExportFailure();
					}

					@Override
					public void onProgress(final float v) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								int progress = (int) (p3Start + v * (p3End - p3Start));
								progress_export.setProgress(progress);
								tv_export_percent.setText(progress + "%");
							}
						});
					}
				});
			}
		});
	}

	private void runMusicStage(final String inputPath) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				tv_export_status.setText("Chèn nhạc nền...");
			}
		});

		final String tempAudioFile = copyAssetToTempFile(audioPath);
		if (tempAudioFile == null) {
			showExportFailure();
			return;
		}

		float mixVideoVolume = needVolume ? 1.0f : videoVolume;

		runEditorSafely(new Runnable() {
			@Override
			public void run() {
				EpEditor.music(inputPath, tempAudioFile, outputPath, mixVideoVolume, 0.8f, new OnEditorListener() {
					@Override
					public void onSuccess() {
						deleteFileSilently(tempAudioFile);
						deleteFileSilently(inputPath);
						showExportSuccess();
					}

					@Override
					public void onFailure() {
						deleteFileSilently(tempAudioFile);
						showExportFailure();
					}

					@Override
					public void onProgress(final float v) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								int progress = (int) (p4Start + v * (p4End - p4Start));
								progress_export.setProgress(progress);
								tv_export_percent.setText(progress + "%");
							}
						});
					}
				});
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
