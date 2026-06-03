package com.joe.epmediademo.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
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
import java.util.Locale;

import VideoHandle.EpEditor;
import VideoHandle.EpVideo;
import VideoHandle.OnEditorListener;

public class EditActivity extends AppCompatActivity implements View.OnClickListener {

	private static final int CHOOSE_FILE = 10;
	
	// Top Header views
	private ImageView bt_back;
	private ImageView bt_undo;
	private ImageView bt_redo;
	private Button bt_exec;
	
	// Video Player & Overlay Views
	private FrameLayout video_container;
	private VideoView video_view;
	private TextView tv_subtitle_preview;
	private TextView tv_timecode;
	private LinearLayout video_empty_overlay;
	private LinearLayout layout_video_controls;
	private LinearLayout btn_video_speed;
	private TextView tv_speed_val;
	private ImageView iv_play_pause;
	private ImageView btn_video_fullscreen;
	private Button bt_file;
	
	// Timeline Views
	private FrameLayout timeline_container;
	private TextView tv_timeline_timecode;
	private ImageView btn_zoom_in;
	private ImageView btn_layers;
	private HorizontalScrollView timeline_scroll;
	private LinearLayout layout_tracks_wrapper;
	private LinearLayout timeline_thumbnails;
	
	// Bottom Editing Toolbar Tabs
	private HorizontalScrollView scroll_bottom_tabs;
	private LinearLayout tab_edit;
	private ImageView iv_tab_edit;
	private TextView tv_tab_edit;
	private LinearLayout tab_audio;
	private ImageView iv_tab_audio;
	private TextView tv_tab_audio;
	private LinearLayout tab_text;
	private ImageView iv_tab_text;
	private TextView tv_tab_text;
	private LinearLayout tab_stickers;
	private ImageView iv_tab_stickers;
	private TextView tv_tab_stickers;
	private LinearLayout tab_effects;
	private ImageView iv_tab_effects;
	private TextView tv_tab_effects;
	private LinearLayout tab_filters;
	private ImageView iv_tab_filters;
	private TextView tv_tab_filters;
	private LinearLayout tab_transition;
	private ImageView iv_tab_transition;
	private TextView tv_tab_transition;
	private LinearLayout tab_overlay;
	private ImageView iv_tab_overlay;
	private TextView tv_tab_overlay;
	
	// Slide-Up Overlay Panels
	private LinearLayout panel_clip_settings;
	private ImageView btn_close_clip_settings;
	private LinearLayout btn_action_split;
	private LinearLayout btn_action_speed;
	private LinearLayout btn_action_volume;
	private LinearLayout btn_action_crop;
	private LinearLayout btn_action_delete;
	private LinearLayout btn_action_mirror;
	private LinearLayout btn_action_enhance;
	private LinearLayout btn_action_more;
	
	private LinearLayout panel_canvas_presets;
	private ImageView btn_close_canvas_presets;
	private Button btn_crop_original;
	private Button btn_crop_16_9;
	private Button btn_crop_9_16;
	private Button btn_crop_1_1;
	
	private LinearLayout panel_text_input;
	private ImageView btn_close_text_input;
	private EditText et_subtitle_input;
	private Slider slider_text_x;
	private Slider slider_text_y;
	
	private LinearLayout panel_trim_editor;
	private ImageView btn_close_trim_editor;
	private RangeSlider range_slider;
	private TextView tv_trim_start;
	private TextView tv_trim_duration;
	private TextView tv_trim_end;

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
			setupVideoPlayer(videoUrl);
		}
	}

	private void initView() {
		// Top header bindings
		bt_back = (ImageView) findViewById(R.id.bt_back);
		bt_undo = (ImageView) findViewById(R.id.bt_undo);
		bt_redo = (ImageView) findViewById(R.id.bt_redo);
		bt_exec = (Button) findViewById(R.id.bt_exec);
		
		// Video Preview bindings
		video_container = (FrameLayout) findViewById(R.id.video_container);
		video_view = (VideoView) findViewById(R.id.video_view);
		tv_subtitle_preview = (TextView) findViewById(R.id.tv_subtitle_preview);
		tv_timecode = (TextView) findViewById(R.id.tv_timecode);
		video_empty_overlay = (LinearLayout) findViewById(R.id.video_empty_overlay);
		layout_video_controls = (LinearLayout) findViewById(R.id.layout_video_controls);
		btn_video_speed = (LinearLayout) findViewById(R.id.btn_video_speed);
		tv_speed_val = (TextView) findViewById(R.id.tv_speed_val);
		iv_play_pause = (ImageView) findViewById(R.id.iv_play_pause);
		btn_video_fullscreen = (ImageView) findViewById(R.id.btn_video_fullscreen);
		bt_file = (Button) findViewById(R.id.bt_file);
		
		// Timeline bindings
		timeline_container = (FrameLayout) findViewById(R.id.timeline_container);
		tv_timeline_timecode = (TextView) findViewById(R.id.tv_timeline_timecode);
		btn_zoom_in = (ImageView) findViewById(R.id.btn_zoom_in);
		btn_layers = (ImageView) findViewById(R.id.btn_layers);
		timeline_scroll = (HorizontalScrollView) findViewById(R.id.timeline_scroll);
		layout_tracks_wrapper = (LinearLayout) findViewById(R.id.layout_tracks_wrapper);
		timeline_thumbnails = (LinearLayout) findViewById(R.id.timeline_thumbnails);
		
		// Bottom tabs bindings
		scroll_bottom_tabs = (HorizontalScrollView) findViewById(R.id.scroll_bottom_tabs);
		tab_edit = (LinearLayout) findViewById(R.id.tab_edit);
		iv_tab_edit = (ImageView) findViewById(R.id.iv_tab_edit);
		tv_tab_edit = (TextView) findViewById(R.id.tv_tab_edit);
		tab_audio = (LinearLayout) findViewById(R.id.tab_audio);
		iv_tab_audio = (ImageView) findViewById(R.id.iv_tab_audio);
		tv_tab_audio = (TextView) findViewById(R.id.tv_tab_audio);
		tab_text = (LinearLayout) findViewById(R.id.tab_text);
		iv_tab_text = (ImageView) findViewById(R.id.iv_tab_text);
		tv_tab_text = (TextView) findViewById(R.id.tv_tab_text);
		tab_stickers = (LinearLayout) findViewById(R.id.tab_stickers);
		iv_tab_stickers = (ImageView) findViewById(R.id.iv_tab_stickers);
		tv_tab_stickers = (TextView) findViewById(R.id.tv_tab_stickers);
		tab_effects = (LinearLayout) findViewById(R.id.tab_effects);
		iv_tab_effects = (ImageView) findViewById(R.id.iv_tab_effects);
		tv_tab_effects = (TextView) findViewById(R.id.tv_tab_effects);
		tab_filters = (LinearLayout) findViewById(R.id.tab_filters);
		iv_tab_filters = (ImageView) findViewById(R.id.iv_tab_filters);
		tv_tab_filters = (TextView) findViewById(R.id.tv_tab_filters);
		tab_transition = (LinearLayout) findViewById(R.id.tab_transition);
		iv_tab_transition = (ImageView) findViewById(R.id.iv_tab_transition);
		tv_tab_transition = (TextView) findViewById(R.id.tv_tab_transition);
		tab_overlay = (LinearLayout) findViewById(R.id.tab_overlay);
		iv_tab_overlay = (ImageView) findViewById(R.id.iv_tab_overlay);
		tv_tab_overlay = (TextView) findViewById(R.id.tv_tab_overlay);
		
		// Slide-Up panels bindings
		panel_clip_settings = (LinearLayout) findViewById(R.id.panel_clip_settings);
		btn_close_clip_settings = (ImageView) findViewById(R.id.btn_close_clip_settings);
		btn_action_split = (LinearLayout) findViewById(R.id.btn_action_split);
		btn_action_speed = (LinearLayout) findViewById(R.id.btn_action_speed);
		btn_action_volume = (LinearLayout) findViewById(R.id.btn_action_volume);
		btn_action_crop = (LinearLayout) findViewById(R.id.btn_action_crop);
		btn_action_delete = (LinearLayout) findViewById(R.id.btn_action_delete);
		btn_action_mirror = (LinearLayout) findViewById(R.id.btn_action_mirror);
		btn_action_enhance = (LinearLayout) findViewById(R.id.btn_action_enhance);
		btn_action_more = (LinearLayout) findViewById(R.id.btn_action_more);
		
		panel_canvas_presets = (LinearLayout) findViewById(R.id.panel_canvas_presets);
		btn_close_canvas_presets = (ImageView) findViewById(R.id.btn_close_canvas_presets);
		btn_crop_original = (Button) findViewById(R.id.btn_crop_original);
		btn_crop_16_9 = (Button) findViewById(R.id.btn_crop_16_9);
		btn_crop_9_16 = (Button) findViewById(R.id.btn_crop_9_16);
		btn_crop_1_1 = (Button) findViewById(R.id.btn_crop_1_1);
		
		panel_text_input = (LinearLayout) findViewById(R.id.panel_text_input);
		btn_close_text_input = (ImageView) findViewById(R.id.btn_close_text_input);
		et_subtitle_input = (EditText) findViewById(R.id.et_subtitle_input);
		slider_text_x = (Slider) findViewById(R.id.slider_text_x);
		slider_text_y = (Slider) findViewById(R.id.slider_text_y);
		
		panel_trim_editor = (LinearLayout) findViewById(R.id.panel_trim_editor);
		btn_close_trim_editor = (ImageView) findViewById(R.id.btn_close_trim_editor);
		range_slider = (RangeSlider) findViewById(R.id.range_slider);
		tv_trim_start = (TextView) findViewById(R.id.tv_trim_start);
		tv_trim_duration = (TextView) findViewById(R.id.tv_trim_duration);
		tv_trim_end = (TextView) findViewById(R.id.tv_trim_end);

		// Set Click Listeners
		bt_back.setOnClickListener(this);
		bt_undo.setOnClickListener(this);
		bt_redo.setOnClickListener(this);
		bt_exec.setOnClickListener(this);
		
		bt_file.setOnClickListener(this);
		video_container.setOnClickListener(this);
		btn_video_speed.setOnClickListener(this);
		iv_play_pause.setOnClickListener(this);
		btn_video_fullscreen.setOnClickListener(this);
		
		btn_zoom_in.setOnClickListener(this);
		btn_layers.setOnClickListener(this);
		
		tab_edit.setOnClickListener(this);
		tab_audio.setOnClickListener(this);
		tab_text.setOnClickListener(this);
		tab_stickers.setOnClickListener(this);
		tab_effects.setOnClickListener(this);
		tab_filters.setOnClickListener(this);
		tab_transition.setOnClickListener(this);
		tab_overlay.setOnClickListener(this);
		
		btn_close_clip_settings.setOnClickListener(this);
		btn_action_split.setOnClickListener(this);
		btn_action_speed.setOnClickListener(this);
		btn_action_volume.setOnClickListener(this);
		btn_action_crop.setOnClickListener(this);
		btn_action_delete.setOnClickListener(this);
		btn_action_mirror.setOnClickListener(this);
		btn_action_enhance.setOnClickListener(this);
		btn_action_more.setOnClickListener(this);
		
		btn_close_canvas_presets.setOnClickListener(this);
		btn_crop_original.setOnClickListener(this);
		btn_crop_16_9.setOnClickListener(this);
		btn_crop_9_16.setOnClickListener(this);
		btn_crop_1_1.setOnClickListener(this);
		
		btn_close_text_input.setOnClickListener(this);
		btn_close_trim_editor.setOnClickListener(this);

		// Add Text Change textwatcher for Live update
		et_subtitle_input.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				subtitleText = s.toString();
				updateSubtitlePreview();
			}
		});

		// Live Subtitle Position Sliders
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

		// RangeSlider Event Handlers
		range_slider.addOnChangeListener(new RangeSlider.OnChangeListener() {
			@Override
			public void onValueChange(RangeSlider slider, float value, boolean fromUser) {
				List<Float> values = slider.getValues();
				if (values.size() >= 2) {
					trimStartSec = values.get(0);
					trimEndSec = values.get(1);
					updateTrimLabels();
					
					if (fromUser && video_view != null) {
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
				if (video_view != null) {
					video_view.seekTo((int) (trimStartSec * 1000));
					video_view.start();
					iv_play_pause.setImageResource(R.drawable.ic_pause);
				}
			}
		});

		// Configure Sync Scrolling Physics on Timeline drag
		timeline_scroll.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
					isUserSeeking = true;
					if (video_view != null && video_view.isPlaying()) {
						video_view.pause();
						iv_play_pause.setImageResource(R.drawable.ic_play);
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
					v.postDelayed(new Runnable() {
						@Override
						public void run() {
							isUserSeeking = false;
						}
					}, 300);
				}
				return false;
			}
		});

		timeline_scroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
			@Override
			public void onScrollChanged() {
				if (isUserSeeking && video_view != null) {
					int scrollX = timeline_scroll.getScrollX();
					int trackWidth = layout_tracks_wrapper.getWidth();
					int durationMs = video_view.getDuration();
					if (trackWidth > 0 && durationMs > 0) {
						float ratio = (float) scrollX / trackWidth;
						if (ratio < 0) ratio = 0;
						if (ratio > 1) ratio = 1;
						int seekMs = (int) (ratio * durationMs);
						video_view.seekTo(seekMs);
						updateTimecodes(seekMs);
					}
				}
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

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.bt_back) {
			finish();
		} else if (id == R.id.bt_undo) {
			Toast.makeText(this, "Không có hành động để Undo", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.bt_redo) {
			Toast.makeText(this, "Không có hành động để Redo", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.bt_exec) {
			execVideo();
		} else if (id == R.id.bt_file) {
			chooseFile();
		} else if (id == R.id.video_container) {
			if (videoUrl == null || videoUrl.isEmpty()) {
				chooseFile();
			} else {
				togglePlayPause();
			}
		} else if (id == R.id.btn_video_speed) {
			showPanel(panel_trim_editor);
		} else if (id == R.id.iv_play_pause) {
			togglePlayPause();
		} else if (id == R.id.btn_video_fullscreen) {
			Toast.makeText(this, "Chế độ xem toàn màn hình", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_zoom_in) {
			Toast.makeText(this, "Thu phóng timeline", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_layers) {
			Toast.makeText(this, "Quản lý các Layer", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.tab_edit) {
			showPanel(panel_clip_settings);
			setActiveTab(tab_edit, iv_tab_edit, tv_tab_edit);
		} else if (id == R.id.tab_audio) {
			Toast.makeText(this, "Đã chọn âm thanh", Toast.LENGTH_SHORT).show();
			setActiveTab(tab_audio, iv_tab_audio, tv_tab_audio);
		} else if (id == R.id.tab_text) {
			showPanel(panel_text_input);
			setActiveTab(tab_text, iv_tab_text, tv_tab_text);
		} else if (id == R.id.tab_stickers) {
			Toast.makeText(this, "Đã chọn nhãn dán", Toast.LENGTH_SHORT).show();
			setActiveTab(tab_stickers, iv_tab_stickers, tv_tab_stickers);
		} else if (id == R.id.tab_effects) {
			Toast.makeText(this, "Đã chọn hiệu ứng", Toast.LENGTH_SHORT).show();
			setActiveTab(tab_effects, iv_tab_effects, tv_tab_effects);
		} else if (id == R.id.tab_filters) {
			Toast.makeText(this, "Đã chọn bộ lọc", Toast.LENGTH_SHORT).show();
			setActiveTab(tab_filters, iv_tab_filters, tv_tab_filters);
		} else if (id == R.id.tab_transition) {
			Toast.makeText(this, "Đã chọn chuyển cảnh", Toast.LENGTH_SHORT).show();
			setActiveTab(tab_transition, iv_tab_transition, tv_tab_transition);
		} else if (id == R.id.tab_overlay) {
			Toast.makeText(this, "Đã chọn lớp phủ", Toast.LENGTH_SHORT).show();
			setActiveTab(tab_overlay, iv_tab_overlay, tv_tab_overlay);
		} else if (id == R.id.btn_close_clip_settings) {
			showPanel(null);
		} else if (id == R.id.btn_action_split) {
			Toast.makeText(this, "Đã tách clip tại điểm hiện tại", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_action_speed) {
			showPanel(panel_trim_editor);
		} else if (id == R.id.btn_action_volume) {
			showVolumeDialog();
		} else if (id == R.id.btn_action_crop) {
			showPanel(panel_canvas_presets);
		} else if (id == R.id.btn_action_delete) {
			resetEditorState();
		} else if (id == R.id.btn_action_mirror) {
			isMirror = !isMirror;
			updateVideoTransformations();
			Toast.makeText(this, isMirror ? "Đã lật ngược video (Mirror)" : "Chế độ bình thường", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_action_enhance) {
			Toast.makeText(this, "Đã bật bộ lọc làm nét (Enhance)", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_action_more) {
			Toast.makeText(this, "Nhiều tùy chọn hơn sắp ra mắt", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_close_canvas_presets) {
			showPanel(panel_clip_settings);
		} else if (id == R.id.btn_crop_original) {
			updateCropPresetButtons(0);
			Toast.makeText(this, "Tỉ lệ: Gốc", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_crop_16_9) {
			updateCropPresetButtons(1);
			Toast.makeText(this, "Tỉ lệ: 16:9", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_crop_9_16) {
			updateCropPresetButtons(2);
			Toast.makeText(this, "Tỉ lệ: 9:16", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_crop_1_1) {
			updateCropPresetButtons(3);
			Toast.makeText(this, "Tỉ lệ: 1:1", Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_close_text_input) {
			showPanel(null);
		} else if (id == R.id.btn_close_trim_editor) {
			showPanel(panel_clip_settings);
		}
	}

	private void showPanel(View panelToShow) {
		panel_clip_settings.setVisibility(View.GONE);
		panel_canvas_presets.setVisibility(View.GONE);
		panel_text_input.setVisibility(View.GONE);
		panel_trim_editor.setVisibility(View.GONE);
		if (panelToShow != null) {
			panelToShow.setVisibility(View.VISIBLE);
		}
	}

	private void setActiveTab(LinearLayout activeTab, ImageView activeIcon, TextView activeText) {
		int normalColor = getResources().getColor(R.color.lumina_text_secondary);
		int activeColor = getResources().getColor(R.color.colorAccent);
		
		ImageView[] icons = {iv_tab_edit, iv_tab_audio, iv_tab_text, iv_tab_stickers, iv_tab_effects, iv_tab_filters, iv_tab_transition, iv_tab_overlay};
		TextView[] texts = {tv_tab_edit, tv_tab_audio, tv_tab_text, tv_tab_stickers, tv_tab_effects, tv_tab_filters, tv_tab_transition, tv_tab_overlay};
		
		for (int i = 0; i < icons.length; i++) {
			if (icons[i] != null) {
				icons[i].setImageTintList(android.content.res.ColorStateList.valueOf(normalColor));
			}
			if (texts[i] != null) {
				texts[i].setTextColor(normalColor);
			}
		}
		
		if (activeIcon != null) {
			activeIcon.setImageTintList(android.content.res.ColorStateList.valueOf(activeColor));
		}
		if (activeText != null) {
			activeText.setTextColor(activeColor);
		}
	}

	private void updateCropPresetButtons(int preset) {
		selectedCropPreset = preset;
		
		Button[] buttons = {btn_crop_original, btn_crop_16_9, btn_crop_9_16, btn_crop_1_1};
		int activeColor = getResources().getColor(R.color.colorAccent);
		int activeTextColor = getResources().getColor(R.color.lumina_bg);
		int normalColor = getResources().getColor(R.color.lumina_surface_container);
		int normalTextColor = getResources().getColor(R.color.lumina_text_primary);
		
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i] != null) {
				if (i == preset) {
					buttons[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
					buttons[i].setTextColor(activeTextColor);
				} else {
					buttons[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(normalColor));
					buttons[i].setTextColor(normalTextColor);
				}
			}
		}
	}

	private void showVolumeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
		builder.setTitle("Adjust Volume");
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(40, 40, 40, 40);
		
		final TextView tvVol = new TextView(this);
		tvVol.setText("Volume: 100%");
		tvVol.setTextColor(getResources().getColor(R.color.lumina_text_primary));
		tvVol.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
		layout.addView(tvVol);
		
		Slider slider = new Slider(this);
		slider.setValueFrom(0f);
		slider.setValueTo(100f);
		slider.setValue(100f);
		slider.addOnChangeListener(new Slider.OnChangeListener() {
			@Override
			public void onValueChange(Slider slider, float value, boolean fromUser) {
				tvVol.setText("Volume: " + (int) value + "%");
			}
		});
		layout.addView(slider);
		
		builder.setView(layout);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(EditActivity.this, "Đã thiết lập âm lượng thành công", Toast.LENGTH_SHORT).show();
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.show();
	}

	private void resetEditorState() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
		builder.setTitle("Reset Projects");
		builder.setMessage("Bạn có chắc chắn muốn xóa hết tất cả hiệu ứng và chỉnh sửa của video này không?");
		builder.setPositiveButton("Reset", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				currentRotation = 0;
				isMirror = false;
				selectedCropPreset = 0;
				updateVideoTransformations();
				updateCropPresetButtons(0);
				
				et_subtitle_input.setText("");
				subtitleText = "";
				updateSubtitlePreview();
				
				if (video_view != null) {
					int durationMs = video_view.getDuration();
					trimStartSec = 0f;
					trimEndSec = durationMs / 1000f;
					range_slider.setValues(Arrays.asList(0f, trimEndSec));
					updateTrimLabels();
					video_view.seekTo(1);
				}
				Toast.makeText(EditActivity.this, "Đã xóa toàn bộ chỉnh sửa", Toast.LENGTH_SHORT).show();
			}
		});
		builder.setNegativeButton("Hủy", null);
		builder.show();
	}

	private void togglePlayPause() {
		if (video_view == null) return;
		if (video_view.isPlaying()) {
			video_view.pause();
			iv_play_pause.setImageResource(R.drawable.ic_play);
		} else {
			video_view.start();
			iv_play_pause.setImageResource(R.drawable.ic_pause);
		}
	}

	private void setupVideoPlayer(final String path) {
		video_view.setVisibility(View.VISIBLE);
		video_empty_overlay.setVisibility(View.GONE);
		bt_file.setVisibility(View.GONE);
		timeline_container.setVisibility(View.VISIBLE);
		layout_video_controls.setVisibility(View.VISIBLE);
		scroll_bottom_tabs.setVisibility(View.VISIBLE);

		video_view.setVideoPath(path);
		video_view.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(android.media.MediaPlayer mp) {
				int durationMs = video_view.getDuration();
				float durationSec = durationMs / 1000f;
				
				trimStartSec = 0f;
				trimEndSec = durationSec;
				
				// Extract video dimensions
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
				
				// Set up RangeSlider bounds
				range_slider.setValueFrom(0f);
				range_slider.setValueTo(durationSec);
				range_slider.setValues(Arrays.asList(0f, durationSec));
				
				updateTrimLabels();
				
				// Setup scroll padding
				int screenWidth = getResources().getDisplayMetrics().widthPixels;
				int halfWidth = screenWidth / 2;
				timeline_scroll.setPadding(halfWidth, 0, halfWidth, 0);
				timeline_scroll.setClipToPadding(false);
				
				video_view.seekTo(1);
				iv_play_pause.setImageResource(R.drawable.ic_play);
				
				loadTimelineThumbnails(path);
				
				// Reset transformations
				currentRotation = 0;
				isMirror = false;
				selectedCropPreset = 0;
				updateVideoTransformations();
				updateCropPresetButtons(0);
				
				et_subtitle_input.setText("");
				subtitleText = "";
				subtitleXPercent = 50f;
				subtitleYPercent = 85f;
				slider_text_x.setValue(50f);
				slider_text_y.setValue(85f);
				updateSubtitlePreview();
				
				showPanel(panel_clip_settings);
				setActiveTab(tab_edit, iv_tab_edit, tv_tab_edit);
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
		tv_trim_start.setText(String.format(Locale.US, "Bắt đầu: %.1fs", trimStartSec));
		tv_trim_end.setText(String.format(Locale.US, "Kết thúc: %.1fs", trimEndSec));
		tv_trim_duration.setText(String.format(Locale.US, "Thời lượng: %.1fs", duration));
	}

	private void updateVideoTransformations() {
		if (video_view == null) return;
		video_view.setRotation(currentRotation);
		video_view.setScaleX(isMirror ? -1f : 1f);
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
					} else {
						updateTimecodes(currentPosMs);
						int durationMs = video_view.getDuration();
						if (durationMs > 0) {
							float progressRatio = (float) currentPosMs / durationMs;
							int trackWidth = layout_tracks_wrapper.getWidth();
							if (trackWidth > 0) {
								int targetScrollX = (int) (progressRatio * trackWidth);
								timeline_scroll.scrollTo(targetScrollX, 0);
							}
						}
					}
				}
				playHandler.postDelayed(this, 33);
			}
		};
		playHandler.post(playRunnable);
	}

	private void updateTimecodes(int ms) {
		if (ms < 0) ms = 0;
		int seconds = (ms / 1000) % 60;
		int minutes = (ms / (1000 * 60)) % 60;
		int hours = (ms / (1000 * 60 * 60)) % 24;
		String formattedTime = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds);
		
		if (tv_timecode != null) {
			tv_timecode.setText(formattedTime);
		}
		if (tv_timeline_timecode != null) {
			tv_timeline_timecode.setText(formattedTime);
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
		if (requestCode == CHOOSE_FILE && resultCode == RESULT_OK && data != null) {
			videoUrl = UriUtils.getPath(EditActivity.this, data.getData());
			setupVideoPlayer(videoUrl);
		}
	}

	private void execVideo() {
		if (videoUrl != null && !videoUrl.isEmpty()) {
			EpVideo epVideo = new EpVideo(videoUrl);
			
			// Apply trim
			epVideo.clip(trimStartSec, trimEndSec - trimStartSec);
			
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

	@Override
	protected void onPause() {
		super.onPause();
		if (video_view != null && video_view.isPlaying()) {
			video_view.pause();
			iv_play_pause.setImageResource(R.drawable.ic_play);
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
