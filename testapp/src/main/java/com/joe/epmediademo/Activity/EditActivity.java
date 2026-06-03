package com.joe.epmediademo.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.joe.epmediademo.Application.MyApplication;
import com.joe.epmediademo.R;
import com.joe.epmediademo.Utils.UriUtils;

import java.util.Arrays;
import java.util.List;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

	private static final int CHOOSE_FILE = 10;
	
	// Video Player & Overlay Views
	private VideoView video_view;
	private View video_container;
	private TextView tv_subtitle_preview;
	private ImageView iv_play_pause;
	private View video_empty_overlay;
	private TextView tv_file;
	private Button bt_file, bt_exec;
	
	// Dynamic Panels
	private View panel_onboarding;
	private View layout_panels;
	private View panel_trim;
	private View panel_canvas;
	private View panel_text;
	
	// Toolbar Tabs
	private View tab_trim;
	private View tab_canvas;
	private View tab_text;
	private TextView tv_tab_trim;
	private TextView tv_tab_canvas;
	private TextView tv_tab_text;
	private ImageView iv_tab_trim;
	private ImageView iv_tab_canvas;
	private ImageView iv_tab_text;
	
	// Trim Panel Views
	private LinearLayout timeline_thumbnails;
	private RangeSlider range_slider;
	private TextView tv_trim_start, tv_trim_duration, tv_trim_end;
	
	// Canvas Panel Views
	private Button btn_rotate_90;
	private Button btn_mirror;
	private Button btn_crop_original;
	private Button btn_crop_16_9;
	private Button btn_crop_9_16;
	private Button btn_crop_1_1;
	
	// Text Panel Views
	private EditText et_subtitle_input;
	private Slider slider_text_x;
	private Slider slider_text_y;

	// State variables
	private String videoUrl;
	private ProgressDialog mProgressDialog;
	private Handler playHandler = new Handler();
	private Runnable playRunnable;
	private float trimStartSec = 0f;
	private float trimEndSec = 0f;
	private boolean isUserSeeking = false;
	
	private int videoWidth = 0;
	private int videoHeight = 0;
	private int currentRotation = 0;
	private boolean isMirror = false;
	private int selectedCropPreset = 0; // 0 = Original, 1 = 16:9, 2 = 9:16, 3 = 1:1
	private String subtitleText = "";
	private float subtitleXPercent = 50f;
	private float subtitleYPercent = 85f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		initView();
		startPlayProgressTracker();

		// Load video from intent if passed from MainActivity
		videoUrl = getIntent().getStringExtra("VIDEO_PATH");
		if (videoUrl != null && !videoUrl.isEmpty()) {
			tv_file.setText(videoUrl);
			setupVideoPlayer(videoUrl);
		}
	}

	private void initView() {
		// Find main controllers
		tv_file = (TextView) findViewById(R.id.tv_file);
		bt_file = (Button) findViewById(R.id.bt_file);
		bt_exec = (Button) findViewById(R.id.bt_exec);
		video_view = (VideoView) findViewById(R.id.video_view);
		video_container = findViewById(R.id.video_container);
		tv_subtitle_preview = (TextView) findViewById(R.id.tv_subtitle_preview);
		iv_play_pause = (ImageView) findViewById(R.id.iv_play_pause);
		video_empty_overlay = findViewById(R.id.video_empty_overlay);

		// Find panel views
		panel_onboarding = findViewById(R.id.panel_onboarding);
		layout_panels = findViewById(R.id.layout_panels);
		panel_trim = findViewById(R.id.panel_trim);
		panel_canvas = findViewById(R.id.panel_canvas);
		panel_text = findViewById(R.id.panel_text);

		// Find tab views
		tab_trim = findViewById(R.id.tab_trim);
		tab_canvas = findViewById(R.id.tab_canvas);
		tab_text = findViewById(R.id.tab_text);
		tv_tab_trim = (TextView) findViewById(R.id.tv_tab_trim);
		tv_tab_canvas = (TextView) findViewById(R.id.tv_tab_canvas);
		tv_tab_text = (TextView) findViewById(R.id.tv_tab_text);
		iv_tab_trim = (ImageView) findViewById(R.id.iv_tab_trim);
		iv_tab_canvas = (ImageView) findViewById(R.id.iv_tab_canvas);
		iv_tab_text = (ImageView) findViewById(R.id.iv_tab_text);

		// Find trim controls
		timeline_thumbnails = (LinearLayout) findViewById(R.id.timeline_thumbnails);
		range_slider = (RangeSlider) findViewById(R.id.range_slider);
		tv_trim_start = (TextView) findViewById(R.id.tv_trim_start);
		tv_trim_duration = (TextView) findViewById(R.id.tv_trim_duration);
		tv_trim_end = (TextView) findViewById(R.id.tv_trim_end);

		// Find canvas controls
		btn_rotate_90 = (Button) findViewById(R.id.btn_rotate_90);
		btn_mirror = (Button) findViewById(R.id.btn_mirror);
		btn_crop_original = (Button) findViewById(R.id.btn_crop_original);
		btn_crop_16_9 = (Button) findViewById(R.id.btn_crop_16_9);
		btn_crop_9_16 = (Button) findViewById(R.id.btn_crop_9_16);
		btn_crop_1_1 = (Button) findViewById(R.id.btn_crop_1_1);

		// Find text controls
		et_subtitle_input = (EditText) findViewById(R.id.et_subtitle_input);
		slider_text_x = (Slider) findViewById(R.id.slider_text_x);
		slider_text_y = (Slider) findViewById(R.id.slider_text_y);

		// Set Click Listeners
		bt_file.setOnClickListener(this);
		bt_exec.setOnClickListener(this);

		video_container.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (videoUrl == null) {
					chooseFile();
				} else {
					togglePlayPause();
				}
			}
		});

		// Setup Toolbar Tab Clicks
		tab_trim.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { selectTab(0); }
		});
		tab_canvas.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { selectTab(1); }
		});
		tab_text.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { selectTab(2); }
		});

		// Setup Canvas Event Handlers
		btn_rotate_90.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				currentRotation = (currentRotation + 90) % 360;
				updateVideoTransformations();
				Toast.makeText(EditActivity.this, "Xoay: " + currentRotation + "°", Toast.LENGTH_SHORT).show();
			}
		});

		btn_mirror.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isMirror = !isMirror;
				updateVideoTransformations();
				updateMirrorButtonState();
			}
		});

		btn_crop_original.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { updateCropPresetSelection(0); }
		});
		btn_crop_16_9.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { updateCropPresetSelection(1); }
		});
		btn_crop_9_16.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { updateCropPresetSelection(2); }
		});
		btn_crop_1_1.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View v) { updateCropPresetSelection(3); }
		});

		// Setup Text Event Handlers
		et_subtitle_input.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				subtitleText = s.toString();
				updateSubtitlePreview();
			}
		});

		slider_text_x.addOnChangeListener(new Slider.OnChangeListener() {
			@Override
			public void onValueChange(Slider slider, float value, boolean fromUser) {
				subtitleXPercent = value;
				updateSubtitlePreview();
			}
		});

		slider_text_y.addOnChangeListener(new Slider.OnChangeListener() {
			@Override
			public void onValueChange(Slider slider, float value, boolean fromUser) {
				subtitleYPercent = value;
				updateSubtitlePreview();
			}
		});

		// Setup RangeSlider Event Handlers
		range_slider.addOnChangeListener(new RangeSlider.OnChangeListener() {
			@Override
			public void onValueChange(RangeSlider slider, float value, boolean fromUser) {
				List<Float> values = slider.getValues();
				if (values.size() >= 2) {
					trimStartSec = values.get(0);
					trimEndSec = values.get(1);
					updateTrimLabels();
					
					if (fromUser) {
						int currentPosMs = video_view.getCurrentPosition();
						float currentPosSec = currentPosMs / 1000f;
						if (Math.abs(currentPosSec - trimStartSec) > 0.2f && Math.abs(currentPosSec - trimEndSec) > 0.2f) {
							video_view.seekTo((int) (trimStartSec * 1000));
						} else if (Math.abs(currentPosSec - trimStartSec) < 0.2f) {
							video_view.seekTo((int) (trimStartSec * 1000));
						} else {
							video_view.seekTo((int) (trimEndSec * 1000));
						}
					}
				}
			}
		});

		range_slider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
			@Override
			public void onStartTrackingTouch(RangeSlider slider) {
				isUserSeeking = true;
			}

			@Override
			public void onStopTrackingTouch(RangeSlider slider) {
				isUserSeeking = false;
				video_view.seekTo((int) (trimStartSec * 1000));
				video_view.start();
				iv_play_pause.setVisibility(View.GONE);
			}
		});

		// Configure progress dialog
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMax(100);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setTitle("Processing Video");
	}

	private void selectTab(int tabIndex) {
		// tabs: 0 = Trim, 1 = Canvas, 2 = Text
		panel_trim.setVisibility(tabIndex == 0 ? View.VISIBLE : View.GONE);
		panel_canvas.setVisibility(tabIndex == 1 ? View.VISIBLE : View.GONE);
		panel_text.setVisibility(tabIndex == 2 ? View.VISIBLE : View.GONE);
		
		tv_tab_trim.setTextColor(tabIndex == 0 ? getColorAccent() : getCapcutTextSecondaryColor());
		tv_tab_canvas.setTextColor(tabIndex == 1 ? getColorAccent() : getCapcutTextSecondaryColor());
		tv_tab_text.setTextColor(tabIndex == 2 ? getColorAccent() : getCapcutTextSecondaryColor());
		
		iv_tab_trim.setImageTintList(android.content.res.ColorStateList.valueOf(tabIndex == 0 ? getColorAccent() : getCapcutTextSecondaryColor()));
		iv_tab_canvas.setImageTintList(android.content.res.ColorStateList.valueOf(tabIndex == 1 ? getColorAccent() : getCapcutTextSecondaryColor()));
		iv_tab_text.setImageTintList(android.content.res.ColorStateList.valueOf(tabIndex == 2 ? getColorAccent() : getCapcutTextSecondaryColor()));
	}

	private void togglePlayPause() {
		if (video_view.isPlaying()) {
			video_view.pause();
			iv_play_pause.setImageResource(android.R.drawable.ic_media_play);
			iv_play_pause.setVisibility(View.VISIBLE);
		} else {
			video_view.start();
			iv_play_pause.setVisibility(View.GONE);
		}
	}

	private void setupVideoPlayer(String path) {
		video_view.setVisibility(View.VISIBLE);
		video_empty_overlay.setVisibility(View.GONE);
		layout_panels.setVisibility(View.VISIBLE);
		panel_onboarding.setVisibility(View.GONE);

		video_view.setVideoPath(path);
		video_view.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(android.media.MediaPlayer mp) {
				int durationMs = video_view.getDuration();
				float durationSec = durationMs / 1000f;
				
				trimStartSec = 0f;
				trimEndSec = durationSec;
				
				// Extract dimensions
				try {
					MediaMetadataRetriever retriever = new MediaMetadataRetriever();
					retriever.setDataSource(path);
					String widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
					String heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
					videoWidth = Integer.parseInt(widthStr);
					videoHeight = Integer.parseInt(heightStr);
					retriever.release();
				} catch (Exception e) {
					videoWidth = mp.getVideoWidth();
					videoHeight = mp.getVideoHeight();
				}
				
				range_slider.setValueFrom(0f);
				range_slider.setValueTo(durationSec);
				range_slider.setValues(Arrays.asList(0f, durationSec));
				
				updateTrimLabels();
				
				video_view.seekTo(1);
				iv_play_pause.setImageResource(android.R.drawable.ic_media_play);
				iv_play_pause.setVisibility(View.VISIBLE);
				
				loadTimelineThumbnails(path);
				
				// Reset modifications
				currentRotation = 0;
				isMirror = false;
				selectedCropPreset = 0;
				updateVideoTransformations();
				updateMirrorButtonState();
				updateCropPresetSelection(0);
				
				et_subtitle_input.setText("");
				subtitleText = "";
				subtitleXPercent = 50f;
				subtitleYPercent = 85f;
				slider_text_x.setValue(50f);
				slider_text_y.setValue(85f);
				updateSubtitlePreview();
				
				selectTab(0);
			}
		});
		
		video_view.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(android.media.MediaPlayer mp) {
				video_view.seekTo((int) (trimStartSec * 1000));
				video_view.start();
			}
		});
	}

	private void updateTrimLabels() {
		float duration = trimEndSec - trimStartSec;
		tv_trim_start.setText(String.format(java.util.Locale.US, "Bắt đầu: %.1fs", trimStartSec));
		tv_trim_end.setText(String.format(java.util.Locale.US, "Kết thúc: %.1fs", trimEndSec));
		tv_trim_duration.setText(String.format(java.util.Locale.US, "Thời lượng: %.1fs", duration));
	}

	private void updateVideoTransformations() {
		if (video_view == null) return;
		video_view.setRotation(currentRotation);
		video_view.setScaleX(isMirror ? -1f : 1f);
	}

	private void updateMirrorButtonState() {
		btn_mirror.setBackgroundTintList(android.content.res.ColorStateList.valueOf(isMirror ? getColorAccent() : getCapcutSurfaceColor()));
		btn_mirror.setTextColor(isMirror ? getCapcutBgColor() : getCapcutTextColor());
	}

	private void updateCropPresetSelection(int preset) {
		selectedCropPreset = preset;
		
		btn_crop_original.setBackgroundTintList(android.content.res.ColorStateList.valueOf(preset == 0 ? getColorAccent() : getCapcutSurfaceColor()));
		btn_crop_original.setTextColor(preset == 0 ? getCapcutBgColor() : getCapcutTextColor());
		
		btn_crop_16_9.setBackgroundTintList(android.content.res.ColorStateList.valueOf(preset == 1 ? getColorAccent() : getCapcutSurfaceColor()));
		btn_crop_16_9.setTextColor(preset == 1 ? getCapcutBgColor() : getCapcutTextColor());
		
		btn_crop_9_16.setBackgroundTintList(android.content.res.ColorStateList.valueOf(preset == 2 ? getColorAccent() : getCapcutSurfaceColor()));
		btn_crop_9_16.setTextColor(preset == 2 ? getCapcutBgColor() : getCapcutTextColor());
		
		btn_crop_1_1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(preset == 3 ? getColorAccent() : getCapcutSurfaceColor()));
		btn_crop_1_1.setTextColor(preset == 3 ? getCapcutBgColor() : getCapcutTextColor());
	}

	private void updateSubtitlePreview() {
		if (tv_subtitle_preview == null) return;
		if (subtitleText != null && !subtitleText.trim().isEmpty()) {
			tv_subtitle_preview.setVisibility(View.VISIBLE);
			tv_subtitle_preview.setText(subtitleText);
			
			float containerWidth = video_container.getWidth();
			float containerHeight = video_container.getHeight();
			float density = getResources().getDisplayMetrics().density;
			
			float xOffset = ((subtitleXPercent - 50f) / 50f) * (containerWidth / 2f - 40f * density);
			tv_subtitle_preview.setTranslationX(xOffset);
			
			float yOffset = -((100f - subtitleYPercent) / 100f) * (containerHeight - 60f * density);
			tv_subtitle_preview.setTranslationY(yOffset);
		} else {
			tv_subtitle_preview.setVisibility(View.GONE);
		}
	}

	private void loadTimelineThumbnails(final String videoPath) {
		final LinearLayout thumbnailContainer = timeline_thumbnails;
		thumbnailContainer.removeAllViews();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
				try {
					retriever.setDataSource(videoPath);
					String durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
					long durationMs = Long.parseLong(durationStr);
					
					final int numThumbs = 8;
					final long intervalUs = (durationMs * 1000) / numThumbs;
					
					for (int i = 0; i < numThumbs; i++) {
						long timeUs = i * intervalUs;
						final Bitmap bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
						
						if (bitmap != null) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ImageView imageView = new ImageView(EditActivity.this);
									float density = getResources().getDisplayMetrics().density;
									int widthPx = (int) (60 * density);
									int heightPx = (int) (45 * density);
									LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPx, heightPx);
									params.setMargins(2, 0, 2, 0);
									imageView.setLayoutParams(params);
									imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
									imageView.setImageBitmap(bitmap);
									thumbnailContainer.addView(imageView);
								}
							});
						}
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

	private void startPlayProgressTracker() {
		if (playRunnable != null) {
			playRunnable = null;
		}
		playRunnable = new Runnable() {
			@Override
			public void run() {
				if (video_view != null && video_view.isPlaying() && !isUserSeeking) {
					int currentPosMs = video_view.getCurrentPosition();
					float currentPosSec = currentPosMs / 1000f;
					
					if (trimEndSec > 0 && currentPosSec >= trimEndSec) {
						video_view.seekTo((int) (trimStartSec * 1000));
					}
				}
				playHandler.postDelayed(this, 100);
			}
		};
		playHandler.post(playRunnable);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.bt_file:
				chooseFile();
				break;
			case R.id.bt_exec:
				execVideo();
				break;
		}
	}

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
					videoUrl = UriUtils.getPath(EditActivity.this, data.getData());
					tv_file.setText(videoUrl);
					setupVideoPlayer(videoUrl);
					break;
				}
		}
	}

	private void execVideo(){
		if(videoUrl != null && !"".equals(videoUrl)){
			EpVideo epVideo = new EpVideo(videoUrl);
			
			// Apply trim
			epVideo.clip(trimStartSec, trimEndSec);
			
			// Apply crop
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
			
			// Apply rotation and mirror
			if (currentRotation != 0 || isMirror) {
				epVideo.rotation(currentRotation, isMirror);
			}
			
			// Apply subtitle
			if (subtitleText != null && !subtitleText.trim().isEmpty()) {
				int targetX = (int) (videoWidth * (subtitleXPercent / 100f));
				int targetY = (int) (videoHeight * (subtitleYPercent / 100f));
				epVideo.addText(targetX, targetY, 36, "white", MyApplication.getSavePath() + "msyh.ttf", subtitleText);
			}
			
			mProgressDialog.setProgress(0);
			mProgressDialog.show();
			final String outPath = MyApplication.getSavePath() + "out.mp4";
			EpEditor.exec(epVideo, new EpEditor.OutputOption(outPath), new OnEditorListener() {
				@Override
				public void onSuccess() {
					Toast.makeText(EditActivity.this, "Xuất video thành công: " + outPath, Toast.LENGTH_LONG).show();
					mProgressDialog.dismiss();

					Intent v = new Intent(Intent.ACTION_VIEW);
					v.setDataAndType(Uri.parse(outPath), "video/mp4");
					startActivity(v);
				}

				@Override
				public void onFailure() {
					Toast.makeText(EditActivity.this, "Xuất video thất bại", Toast.LENGTH_SHORT).show();
					mProgressDialog.dismiss();
				}

				@Override
				public void onProgress(float v) {
					mProgressDialog.setProgress((int) (v * 100));
				}
			});
		} else {
			Toast.makeText(this, "Vui lòng chọn một video trước", Toast.LENGTH_SHORT).show();
		}
	}

	// Helper theme color accessors
	private int getColorAccent() {
		return getResources().getColor(R.color.colorAccent);
	}
	private int getCapcutSurfaceColor() {
		return getResources().getColor(R.color.capcut_surface);
	}
	private int getCapcutBgColor() {
		return getResources().getColor(R.color.capcut_bg);
	}
	private int getCapcutTextColor() {
		return getResources().getColor(R.color.capcut_text_primary);
	}
	private int getCapcutTextSecondaryColor() {
		return getResources().getColor(R.color.capcut_text_secondary);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (video_view != null && video_view.isPlaying()) {
			video_view.pause();
			iv_play_pause.setImageResource(android.R.drawable.ic_media_play);
			iv_play_pause.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (playRunnable != null) {
			playHandler.removeCallbacks(playRunnable);
		}
	}
}
