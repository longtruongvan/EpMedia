package com.joe.epmediademo.Activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.effect.MatrixTransformation;
import androidx.media3.effect.GlEffect;
import androidx.media3.effect.GlShaderProgram;


import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.joe.epmediademo.Application.MyApplication;
import com.joe.epmediademo.R;
import com.joe.epmediademo.Utils.UriUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@androidx.annotation.OptIn(markerClass = UnstableApi.class)
public class EditActivity extends AppCompatActivity implements View.OnClickListener {

	private static final int CHOOSE_FILE = 10;
	
	// Top Header views
	private ImageView bt_back;
	private ImageView btn_seek_backward;
	private ImageView btn_seek_forward;
	private Button bt_exec;
	
	// Video Player & Overlay Views
	private FrameLayout video_container;
	private PlayerView playerView;
	private ExoPlayer exoPlayer;
	private View video_filter_overlay;
	private TextView tv_sticker_preview;
	private TextView tv_subtitle_preview;
	private TextView tv_timecode;
	private LinearLayout video_empty_overlay;
	private LinearLayout layout_video_controls;
	private LinearLayout btn_video_speed;
	private TextView tv_speed_val;
	private ImageView iv_play_pause;
	private ImageView btn_video_fullscreen;
	private Button bt_file;
	private android.widget.ImageView btn_add_video;
	
	// Timeline Views
	private FrameLayout timeline_container;
	private LinearLayout clip_effects;
	private LinearLayout clip_text;
	private TextView tv_timeline_timecode;
	private ImageView btn_zoom_in;
	private ImageView btn_layers;
	private TextView tv_track_effects_label;
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
	
	// Slide-Up Panels
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

	// Filters Panel
	private LinearLayout panel_filters;
	private ImageView btn_close_filters;
	private Button btn_filter_none;
	private Button btn_filter_warm;
	private Button btn_filter_cool;
	private Button btn_filter_vintage;

	// Audio Panel
	private LinearLayout panel_audio;
	private ImageView btn_close_audio;
	private Button btn_audio_track1;
	private Button btn_audio_track2;
	private Button btn_audio_track3;
	private Button btn_audio_online;

	// Stickers Panel
	private LinearLayout panel_stickers;
	private ImageView btn_close_stickers;
	private Button btn_sticker_fire;
	private Button btn_sticker_sparkles;
	private Button btn_sticker_heart;

	// State variables
	private java.util.ArrayList<String> videoUrls = new java.util.ArrayList<>();
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
	private float subtitleScale = 1.0f;
	private float subtitleRotation = 0f;

	// Visual filter & sticker state
	private int activeFilterId = R.id.btn_filter_none;
	private String activeStickerText = "";
	private boolean isEnhanced = false;
	private String selectedAudioPath = null;
	private android.app.Dialog dialog_online_music_instance = null;
	private float currentSpeed = 1.0f;
	private android.media.MediaPlayer realVideoPlayer = null;
	private int selectedEffect = 3; // 0 = Glitch, 1 = Cinematic, 2 = Cyberpunk, 3 = None
	private int selectedTransition = -1; // -1 = None
	private int selectedOverlay = 3; // 0 = Vignette, 1 = Retro Vignette, 2 = Letterbox, 3 = None
	private float videoVolume = 1.0f;

	private boolean isFallbackPreview = false;
	private boolean isFallbackPreviewPlaying = false;
	private int previewCurrentPosMs = 0;
	private int previewDurationMs = 60000;
	private boolean isTimelineZoomed = false;
	private boolean isFullscreen = false;
	private java.util.List<Float> splitPoints = new java.util.ArrayList<>();
	private RelativeLayout layout_top_header;

	// Undo / Redo Stacks
	private static class EditorState {
		float trimStartSec;
		float trimEndSec;
		int selectedCropPreset;
		int currentRotation;
		boolean isMirror;
		String subtitleText;
		float subtitleXPercent;
		float subtitleYPercent;
		float subtitleScale;
		float subtitleRotation;
		int activeFilterId;
		String activeStickerText;
		boolean isEnhanced;

		EditorState(float trimStartSec, float trimEndSec, int selectedCropPreset, int currentRotation, 
					boolean isMirror, String subtitleText, float subtitleXPercent, float subtitleYPercent, float subtitleScale, float subtitleRotation,
					int activeFilterId, String activeStickerText, boolean isEnhanced) {
			this.trimStartSec = trimStartSec;
			this.trimEndSec = trimEndSec;
			this.selectedCropPreset = selectedCropPreset;
			this.currentRotation = currentRotation;
			this.isMirror = isMirror;
			this.subtitleText = subtitleText;
			this.subtitleXPercent = subtitleXPercent;
			this.subtitleYPercent = subtitleYPercent;
			this.subtitleScale = subtitleScale;
			this.subtitleRotation = subtitleRotation;
			this.activeFilterId = activeFilterId;
			this.activeStickerText = activeStickerText;
			this.isEnhanced = isEnhanced;
		}
	}

	private java.util.Stack<EditorState> undoStack = new java.util.Stack<>();
	private java.util.Stack<EditorState> redoStack = new java.util.Stack<>();

	private TextWatcher subtitleTextWatcher = new TextWatcher() {
		@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void afterTextChanged(Editable s) {
			subtitleText = s.toString();
			updateSubtitlePreview();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		initView();
		startPlayProgressTracker();

		// Load video from intent if passed from MainActivity
		String initPath = getIntent().getStringExtra("VIDEO_PATH");
		if (initPath != null && !initPath.isEmpty()) {
			videoUrls.add(initPath);
			setupVideoPlayer(initPath);
			String initTool = getIntent().getStringExtra("INIT_TOOL");
			if (initTool != null) {
				applyInitialTool(initTool);
			}
			String templateId = getIntent().getStringExtra("TEMPLATE_ID");
			if (templateId != null) {
				applyTemplatePreset(templateId);
			}
		}
	}

	private void initView() {
		// Top header bindings
		bt_back = (ImageView) findViewById(R.id.bt_back);
		btn_seek_backward = (ImageView) findViewById(R.id.btn_seek_backward);
		btn_seek_forward = (ImageView) findViewById(R.id.btn_seek_forward);
		bt_exec = (Button) findViewById(R.id.bt_exec);
		layout_top_header = (RelativeLayout) findViewById(R.id.layout_top_header);
		
		// Video Preview bindings
		video_container = (FrameLayout) findViewById(R.id.video_container);
		playerView = findViewById(R.id.video_view);
		video_filter_overlay = (View) findViewById(R.id.video_filter_overlay);
		tv_sticker_preview = (TextView) findViewById(R.id.tv_sticker_preview);
		tv_subtitle_preview = (TextView) findViewById(R.id.tv_subtitle_preview);
		video_filter_overlay.setOnTouchListener(new com.joe.epmediademo.Utils.MultiTouchListener(this, new com.joe.epmediademo.Utils.MultiTouchListener.OnTransformListener() {
			@Override
			public void onScale(float scaleFactor) {
				if (tv_subtitle_preview.getVisibility() != View.VISIBLE) return;
				subtitleScale *= scaleFactor;
				if (subtitleScale < 0.5f) subtitleScale = 0.5f;
				if (subtitleScale > 5.0f) subtitleScale = 5.0f;
				updateSubtitlePreview();
			}

			@Override
			public void onRotate(float deltaAngle) {
				if (tv_subtitle_preview.getVisibility() != View.VISIBLE) return;
				subtitleRotation += deltaAngle;
				updateSubtitlePreview();
			}

			@Override
			public void onTranslate(float dx, float dy) {
				if (tv_subtitle_preview.getVisibility() != View.VISIBLE) return;
				if (video_container.getWidth() == 0) return;
				float density = getResources().getDisplayMetrics().density;
				float maxDx = video_container.getWidth() / 2f - 40f * density;
				float maxDy = video_container.getHeight() - 60f * density;
				
				float currentXOffset = ((subtitleXPercent - 50f) / 50f) * maxDx;
				float currentYOffset = -((100f - subtitleYPercent) / 100f) * maxDy;
				
				currentXOffset += dx;
				currentYOffset += dy;
				
				subtitleXPercent = (currentXOffset / maxDx) * 50f + 50f;
				subtitleYPercent = 100f - ((-currentYOffset / maxDy) * 100f);
				
				if (subtitleXPercent < 0) subtitleXPercent = 0;
				if (subtitleXPercent > 100) subtitleXPercent = 100;
				if (subtitleYPercent < 0) subtitleYPercent = 0;
				if (subtitleYPercent > 100) subtitleYPercent = 100;
				
				slider_text_x.setValue(subtitleXPercent);
				slider_text_y.setValue(subtitleYPercent);
				
				updateSubtitlePreview();
			}

			@Override
			public void onTransformEnded() {
				if (tv_subtitle_preview.getVisibility() != View.VISIBLE) return;
				pushStateToUndo();
			}
		}));
		video_filter_overlay.setClickable(true);
		video_filter_overlay.setFocusable(true);
		tv_timecode = (TextView) findViewById(R.id.tv_timecode);
		video_empty_overlay = (LinearLayout) findViewById(R.id.video_empty_overlay);
		layout_video_controls = (LinearLayout) findViewById(R.id.layout_video_controls);
		btn_video_speed = (LinearLayout) findViewById(R.id.btn_video_speed);
		tv_speed_val = (TextView) findViewById(R.id.tv_speed_val);
		iv_play_pause = (ImageView) findViewById(R.id.iv_play_pause);
		btn_video_fullscreen = (ImageView) findViewById(R.id.btn_video_fullscreen);
		bt_file = (Button) findViewById(R.id.bt_file);
		btn_add_video = (android.widget.ImageView) findViewById(R.id.btn_add_video);
		
		// Timeline bindings
		timeline_container = (FrameLayout) findViewById(R.id.timeline_container);
		clip_effects = (LinearLayout) findViewById(R.id.clip_effects);
		clip_text = (LinearLayout) findViewById(R.id.clip_text);
		if (clip_effects != null) clip_effects.setOnClickListener(this);
		if (clip_text != null) clip_text.setOnClickListener(this);
		tv_timeline_timecode = (TextView) findViewById(R.id.tv_timeline_timecode);
		tv_track_effects_label = (TextView) findViewById(R.id.tv_track_effects_label);
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

		// Panels
		panel_filters = (LinearLayout) findViewById(R.id.panel_filters);
		btn_close_filters = (ImageView) findViewById(R.id.btn_close_filters);
		btn_filter_none = (Button) findViewById(R.id.btn_filter_none);
		btn_filter_warm = (Button) findViewById(R.id.btn_filter_warm);
		btn_filter_cool = (Button) findViewById(R.id.btn_filter_cool);
		btn_filter_vintage = (Button) findViewById(R.id.btn_filter_vintage);

		panel_audio = (LinearLayout) findViewById(R.id.panel_audio);
		btn_close_audio = (ImageView) findViewById(R.id.btn_close_audio);
		btn_audio_track1 = (Button) findViewById(R.id.btn_audio_track1);
		btn_audio_track2 = (Button) findViewById(R.id.btn_audio_track2);
		btn_audio_track3 = (Button) findViewById(R.id.btn_audio_track3);
		btn_audio_online = (Button) findViewById(R.id.btn_audio_online);

		panel_stickers = (LinearLayout) findViewById(R.id.panel_stickers);
		btn_close_stickers = (ImageView) findViewById(R.id.btn_close_stickers);
		btn_sticker_fire = (Button) findViewById(R.id.btn_sticker_fire);
		btn_sticker_sparkles = (Button) findViewById(R.id.btn_sticker_sparkles);
		btn_sticker_heart = (Button) findViewById(R.id.btn_sticker_heart);

		// Set Click Listeners
		bt_back.setOnClickListener(this);
		if (btn_seek_backward != null) btn_seek_backward.setOnClickListener(this);
		if (btn_seek_forward != null) btn_seek_forward.setOnClickListener(this);
		bt_exec.setOnClickListener(this);
		
		bt_file.setOnClickListener(this);
		if (btn_add_video != null) btn_add_video.setOnClickListener(this);
		if (btn_add_video != null) btn_add_video.setOnClickListener(this);
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

		if (btn_close_filters != null) btn_close_filters.setOnClickListener(this);
		if (btn_filter_none != null) btn_filter_none.setOnClickListener(this);
		if (btn_filter_warm != null) btn_filter_warm.setOnClickListener(this);
		if (btn_filter_cool != null) btn_filter_cool.setOnClickListener(this);
		if (btn_filter_vintage != null) btn_filter_vintage.setOnClickListener(this);

		if (btn_close_audio != null) btn_close_audio.setOnClickListener(this);
		if (btn_audio_track1 != null) btn_audio_track1.setOnClickListener(this);
		if (btn_audio_track2 != null) btn_audio_track2.setOnClickListener(this);
		if (btn_audio_track3 != null) btn_audio_track3.setOnClickListener(this);
		if (btn_audio_online != null) btn_audio_online.setOnClickListener(this);

		if (btn_close_stickers != null) btn_close_stickers.setOnClickListener(this);
		if (btn_sticker_fire != null) btn_sticker_fire.setOnClickListener(this);
		if (btn_sticker_sparkles != null) btn_sticker_sparkles.setOnClickListener(this);
		if (btn_sticker_heart != null) btn_sticker_heart.setOnClickListener(this);

		// Add Text Change textwatcher for Live update
		et_subtitle_input.addTextChangedListener(subtitleTextWatcher);

		et_subtitle_input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					pushStateToUndo();
				}
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
		slider_text_x.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
			@Override public void onStartTrackingTouch(Slider slider) { pushStateToUndo(); }
			@Override public void onStopTrackingTouch(Slider slider) {}
		});

		slider_text_y.addOnChangeListener(new Slider.OnChangeListener() {
			@Override
			public void onValueChange(Slider slider, float value, boolean fromUser) {
				subtitleYPercent = value;
				updateSubtitlePreview();
			}
		});
		slider_text_y.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
			@Override public void onStartTrackingTouch(Slider slider) { pushStateToUndo(); }
			@Override public void onStopTrackingTouch(Slider slider) {}
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
					
					if (fromUser) {
						if (isFallbackPreview) {
							float currentPosSec = previewCurrentPosMs / 1000f;
							if (Math.abs(currentPosSec - trimStartSec) > 0.2f && Math.abs(currentPosSec - trimEndSec) > 0.2f) {
								previewCurrentPosMs = (int) (trimStartSec * 1000);
							} else if (Math.abs(currentPosSec - trimStartSec) < 0.2f) {
								previewCurrentPosMs = (int) (trimStartSec * 1000);
							} else {
								previewCurrentPosMs = (int) (trimEndSec * 1000);
							}
						} else if (playerView != null) {
							int currentPosMs = (exoPlayer != null ? (int)exoPlayer.getCurrentPosition() : 0);
							float currentPosSec = currentPosMs / 1000f;
							if (Math.abs(currentPosSec - trimStartSec) > 0.2f && Math.abs(currentPosSec - trimEndSec) > 0.2f) {
								if (exoPlayer != null) exoPlayer.seekTo((int) (trimStartSec * 1000));
							} else if (Math.abs(currentPosSec - trimStartSec) < 0.2f) {
								if (exoPlayer != null) exoPlayer.seekTo((int) (trimStartSec * 1000));
							} else {
								if (exoPlayer != null) exoPlayer.seekTo((int) (trimEndSec * 1000));
							}
						}
					}
				}
			}
		});

		range_slider.addOnSliderTouchListener(new RangeSlider.OnSliderTouchListener() {
			@Override
			public void onStartTrackingTouch(RangeSlider slider) {
				isUserSeeking = true;
				pushStateToUndo();
			}

			@Override
			public void onStopTrackingTouch(RangeSlider slider) {
				isUserSeeking = false;
				if (isFallbackPreview) {
					previewCurrentPosMs = (int) (trimStartSec * 1000);
					isFallbackPreviewPlaying = true;
					iv_play_pause.setImageResource(R.drawable.ic_pause);
				} else if (playerView != null) {
					if (exoPlayer != null) exoPlayer.seekTo((int) (trimStartSec * 1000));
					if (exoPlayer != null) exoPlayer.play();
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
					if (isFallbackPreview) {
						if (isFallbackPreviewPlaying) {
							isFallbackPreviewPlaying = false;
							iv_play_pause.setImageResource(R.drawable.ic_play);
						}
					} else if ((exoPlayer != null && exoPlayer.isPlaying())) {
						if (exoPlayer != null) exoPlayer.pause();
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
				if (isUserSeeking) {
					int scrollX = timeline_scroll.getScrollX();
					int trackWidth = layout_tracks_wrapper.getWidth();
					int durationMs = isFallbackPreview ? previewDurationMs : (exoPlayer != null ? (int)exoPlayer.getDuration() : 0);
					if (trackWidth > 0 && durationMs > 0) {
						float ratio = (float) scrollX / trackWidth;
						if (ratio < 0) ratio = 0;
						if (ratio > 1) ratio = 1;
						int seekMs = (int) (ratio * durationMs);
						if (isFallbackPreview) {
							previewCurrentPosMs = seekMs;
						} else if (playerView != null) {
							if (exoPlayer != null) exoPlayer.seekTo(seekMs);
						}
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

		// Staged undo/redo initialization styles
		updateUndoRedoButtonsVisibility();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.bt_back) {
			finish();
		} else if (id == R.id.btn_seek_backward) {
			seekVideo(-500); // Back 0.5s
		} else if (id == R.id.btn_seek_forward) {
			seekVideo(500);  // Forward 0.5s
		} else if (id == R.id.bt_exec) {
			execVideo();
		} else if (id == R.id.bt_file) {
			chooseFile();
		} else if (id == R.id.btn_add_video) {
			chooseFile();
		} else if (id == R.id.video_container) {
			if (videoUrls == null || videoUrls.isEmpty()) {
				chooseFile();
			} else {
				togglePlayPause();
			}
		} else if (id == R.id.btn_video_speed) {
			showSpeedDialog();
		} else if (id == R.id.iv_play_pause) {
			togglePlayPause();
		} else if (id == R.id.btn_video_fullscreen) {
			isFullscreen = !isFullscreen;
			if (isFullscreen) {
				if (layout_top_header != null) layout_top_header.setVisibility(View.GONE);
				if (timeline_container != null) timeline_container.setVisibility(View.GONE);
				if (scroll_bottom_tabs != null) scroll_bottom_tabs.setVisibility(View.GONE);
				showPanel(null);
				Toast.makeText(this, R.string.toast_fullscreen, Toast.LENGTH_SHORT).show();
			} else {
				if (layout_top_header != null) layout_top_header.setVisibility(View.VISIBLE);
				if (timeline_container != null) timeline_container.setVisibility(View.VISIBLE);
				if (scroll_bottom_tabs != null) scroll_bottom_tabs.setVisibility(View.VISIBLE);
				showPanel(panel_clip_settings);
			}
		} else if (id == R.id.btn_zoom_in) {
			isTimelineZoomed = !isTimelineZoomed;
			Toast.makeText(this, isTimelineZoomed ? "Timeline Zoomed 2x" : "Timeline Normal", Toast.LENGTH_SHORT).show();
			btn_zoom_in.setAlpha(isTimelineZoomed ? 1.0f : 0.5f);
			if (isFallbackPreview) {
				loadFallbackTimelineThumbnails(videoUrls.get(0));
			} else {
				loadTimelineThumbnails(videoUrls.get(0));
			}
		} else if (id == R.id.btn_layers) {
			Toast.makeText(this, R.string.toast_manage_layers, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.tab_edit) {
			showPanel(panel_clip_settings);
			setActiveTab(tab_edit, iv_tab_edit, tv_tab_edit);
		} else if (id == R.id.tab_audio) {
			showPanel(panel_audio);
			setActiveTab(tab_audio, iv_tab_audio, tv_tab_audio);
		} else if (id == R.id.tab_text) {
			showPanel(panel_text_input);
			setActiveTab(tab_text, iv_tab_text, tv_tab_text);
		} else if (id == R.id.tab_stickers) {
			showPanel(panel_stickers);
			setActiveTab(tab_stickers, iv_tab_stickers, tv_tab_stickers);
		} else if (id == R.id.tab_effects) {
			setActiveTab(tab_effects, iv_tab_effects, tv_tab_effects);
			showEffectsDialog();
		} else if (id == R.id.tab_filters) {
			showPanel(panel_filters);
			setActiveTab(tab_filters, iv_tab_filters, tv_tab_filters);
		} else if (id == R.id.tab_transition) {
			setActiveTab(tab_transition, iv_tab_transition, tv_tab_transition);
			showTransitionsDialog();
		} else if (id == R.id.tab_overlay) {
			setActiveTab(tab_overlay, iv_tab_overlay, tv_tab_overlay);
			showOverlaysDialog();
		} else if (id == R.id.btn_close_clip_settings) {
			showPanel(null);
		} else if (id == R.id.btn_action_split) {
			int currentPosMs = isFallbackPreview ? previewCurrentPosMs : (exoPlayer != null ? (int)exoPlayer.getCurrentPosition() : 0);
			int durationMs = isFallbackPreview ? previewDurationMs : (exoPlayer != null ? (int)exoPlayer.getDuration() : 0);
			if (durationMs > 0) {
				float ratio = (float) currentPosMs / durationMs;
				splitPoints.add(ratio);
				Toast.makeText(this, getString(R.string.toast_split_clip) + " (" + String.format(Locale.US, "%.1f", ratio * (durationMs/1000f)) + "s)", Toast.LENGTH_SHORT).show();
				if (isFallbackPreview) {
					loadFallbackTimelineThumbnails(videoUrls.get(0));
				} else {
					loadTimelineThumbnails(videoUrls.get(0));
				}
			}
		} else if (id == R.id.btn_action_speed) {
			showSpeedDialog();
		} else if (id == R.id.btn_action_volume) {
			showVolumeDialog();
		} else if (id == R.id.btn_action_crop) {
			showPanel(panel_canvas_presets);
		} else if (id == R.id.btn_action_delete) {
			if (!splitPoints.isEmpty()) {
				pushStateToUndo();
				int currentPosMs = isFallbackPreview ? previewCurrentPosMs : (exoPlayer != null ? (int)exoPlayer.getCurrentPosition() : 0);
				int durationMs = isFallbackPreview ? previewDurationMs : (exoPlayer != null ? (int)exoPlayer.getDuration() : 0);
				float currentRatio = durationMs > 0 ? (float) currentPosMs / durationMs : 0f;
				
				float closestSplit = -1f;
				float minDiff = Float.MAX_VALUE;
				for (float sp : splitPoints) {
					float diff = Math.abs(sp - currentRatio);
					if (diff < minDiff) {
						minDiff = diff;
						closestSplit = sp;
					}
				}
				
				if (closestSplit >= 0) {
					float durationSec = durationMs / 1000f;
					float splitSec = closestSplit * durationSec;
					
					if (currentRatio > closestSplit) {
						trimEndSec = splitSec;
						Toast.makeText(this, "Deleted latter segment from " + String.format(Locale.US, "%.1f", splitSec) + "s", Toast.LENGTH_SHORT).show();
					} else {
						trimStartSec = splitSec;
						Toast.makeText(this, "Deleted former segment up to " + String.format(Locale.US, "%.1f", splitSec) + "s", Toast.LENGTH_SHORT).show();
					}
					splitPoints.remove(closestSplit);
					
					if (isFallbackPreview) {
						previewCurrentPosMs = (int) (trimStartSec * 1000);
						loadFallbackTimelineThumbnails(videoUrls.get(0));
					} else {
						if (playerView != null) {
							if (exoPlayer != null) exoPlayer.seekTo((int) (trimStartSec * 1000));
						}
						loadTimelineThumbnails(videoUrls.get(0));
					}
				} else {
					resetEditorState();
				}
			} else {
				resetEditorState();
			}
		} else if (id == R.id.btn_action_mirror) {
			pushStateToUndo();
			isMirror = !isMirror;
			updateVideoTransformations();
			Toast.makeText(this, isMirror ? getString(R.string.toast_mirror_on) : getString(R.string.toast_mirror_off), Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_action_enhance) {
			pushStateToUndo();
			isEnhanced = !isEnhanced;
			updateVideoTransformations();
			Toast.makeText(this, isEnhanced ? R.string.toast_enhance_on : R.string.toast_enhance_off, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_action_more) {
			Toast.makeText(this, R.string.toast_more_options, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_close_canvas_presets) {
			showPanel(panel_clip_settings);
		} else if (id == R.id.btn_crop_original) {
			pushStateToUndo();
			updateCropPresetButtons(0);
			Toast.makeText(this, R.string.toast_crop_original, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_crop_16_9) {
			pushStateToUndo();
			updateCropPresetButtons(1);
			Toast.makeText(this, R.string.toast_crop_16_9, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_crop_9_16) {
			pushStateToUndo();
			updateCropPresetButtons(2);
			Toast.makeText(this, R.string.toast_crop_9_16, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_crop_1_1) {
			pushStateToUndo();
			updateCropPresetButtons(3);
			Toast.makeText(this, R.string.toast_crop_1_1, Toast.LENGTH_SHORT).show();
		} else if (id == R.id.btn_close_text_input) {
			showPanel(null);
		} else if (id == R.id.btn_close_trim_editor) {
			showPanel(panel_clip_settings);
		} else if (id == R.id.clip_effects) {
			showPanel(panel_filters);
			setActiveTab(tab_filters, iv_tab_filters, tv_tab_filters);
		} else if (id == R.id.clip_text) {
			showPanel(panel_text_input);
			setActiveTab(tab_text, iv_tab_text, tv_tab_text);
		} else if (id == R.id.btn_close_filters) {
			showPanel(null);
		} else if (id == R.id.btn_filter_none) {
			pushStateToUndo();
			applyFilterOverlay(R.id.btn_filter_none);
		} else if (id == R.id.btn_filter_warm) {
			pushStateToUndo();
			applyFilterOverlay(R.id.btn_filter_warm);
		} else if (id == R.id.btn_filter_cool) {
			pushStateToUndo();
			applyFilterOverlay(R.id.btn_filter_cool);
		} else if (id == R.id.btn_filter_vintage) {
			pushStateToUndo();
			applyFilterOverlay(R.id.btn_filter_vintage);
		} else if (id == R.id.btn_close_audio) {
			showPanel(null);
		} else if (id == R.id.btn_audio_track1) {
			Toast.makeText(this, "Chill Lo-Fi Beat Applied", Toast.LENGTH_SHORT).show();
			selectAudioTrack("audio/track_lofi.mp3");
		} else if (id == R.id.btn_audio_track2) {
			Toast.makeText(this, "Electronic Uplifting Applied", Toast.LENGTH_SHORT).show();
			selectAudioTrack("audio/track_electronic.mp3");
		} else if (id == R.id.btn_audio_track3) {
			Toast.makeText(this, "Acoustic Melody Applied", Toast.LENGTH_SHORT).show();
			selectAudioTrack("audio/track_acoustic.mp3");
		} else if (id == R.id.btn_audio_online) {
			showOnlineMusicDialog();
		} else if (id == R.id.btn_close_stickers) {
			showPanel(null);
		} else if (id == R.id.btn_sticker_fire) {
			pushStateToUndo();
			String nextSticker = "🔥".equals(activeStickerText) ? "" : "🔥";
			applyStickerPreview(nextSticker);
		} else if (id == R.id.btn_sticker_sparkles) {
			pushStateToUndo();
			String nextSticker = "✨".equals(activeStickerText) ? "" : "✨";
			applyStickerPreview(nextSticker);
		} else if (id == R.id.btn_sticker_heart) {
			pushStateToUndo();
			String nextSticker = "❤️".equals(activeStickerText) ? "" : "❤️";
			applyStickerPreview(nextSticker);
		}
	}

	private void pushStateToUndo() {
		EditorState state = new EditorState(
			trimStartSec,
			trimEndSec,
			selectedCropPreset,
			currentRotation,
			isMirror,
			subtitleText,
			subtitleXPercent,
			subtitleYPercent,
			subtitleScale,
			subtitleRotation,
			activeFilterId,
			activeStickerText,
			isEnhanced
		);
		undoStack.push(state);
		redoStack.clear();
		updateUndoRedoButtonsVisibility();
	}

	private void updateUndoRedoButtonsVisibility() {

	}

	private void seekVideo(long offsetMs) {
		if (exoPlayer != null) {
			long newPosition = exoPlayer.getCurrentPosition() + offsetMs;
			if (newPosition < 0) newPosition = 0;
			if (newPosition > exoPlayer.getDuration()) newPosition = exoPlayer.getDuration();
			exoPlayer.seekTo(newPosition);
		}
	}

	private void undo() {
		if (undoStack.isEmpty()) {
			Toast.makeText(this, R.string.toast_no_undo, Toast.LENGTH_SHORT).show();
			return;
		}
		EditorState currentState = new EditorState(
			trimStartSec,
			trimEndSec,
			selectedCropPreset,
			currentRotation,
			isMirror,
			subtitleText,
			subtitleXPercent,
			subtitleYPercent,
			subtitleScale,
			subtitleRotation,
			activeFilterId,
			activeStickerText,
			isEnhanced
		);
		redoStack.push(currentState);
		
		EditorState previousState = undoStack.pop();
		applyState(previousState);
		updateUndoRedoButtonsVisibility();
	}

	private void redo() {
		if (redoStack.isEmpty()) {
			Toast.makeText(this, R.string.toast_no_redo, Toast.LENGTH_SHORT).show();
			return;
		}
		EditorState currentState = new EditorState(
			trimStartSec,
			trimEndSec,
			selectedCropPreset,
			currentRotation,
			isMirror,
			subtitleText,
			subtitleXPercent,
			subtitleYPercent,
			subtitleScale,
			subtitleRotation,
			activeFilterId,
			activeStickerText,
			isEnhanced
		);
		undoStack.push(currentState);
		
		EditorState nextState = redoStack.pop();
		applyState(nextState);
		updateUndoRedoButtonsVisibility();
	}

	private void applyState(EditorState state) {
		trimStartSec = state.trimStartSec;
		trimEndSec = state.trimEndSec;
		selectedCropPreset = state.selectedCropPreset;
		currentRotation = state.currentRotation;
		isMirror = state.isMirror;
		subtitleText = state.subtitleText;
		subtitleXPercent = state.subtitleXPercent;
		subtitleYPercent = state.subtitleYPercent;
		subtitleScale = state.subtitleScale;
		subtitleRotation = state.subtitleRotation;
		activeFilterId = state.activeFilterId;
		activeStickerText = state.activeStickerText;
		isEnhanced = state.isEnhanced;

		// Apply transformations
		updateVideoTransformations();
		applyVideoViewAspectRatio(selectedCropPreset);
		updateCropPresetButtons(selectedCropPreset);
		updateToggleButton(btn_action_enhance, isEnhanced);

		// Apply subtitle text and pos
		et_subtitle_input.removeTextChangedListener(subtitleTextWatcher);
		et_subtitle_input.setText(subtitleText);
		et_subtitle_input.addTextChangedListener(subtitleTextWatcher);
		updateSubtitlePreview();
		
		slider_text_x.setValue(subtitleXPercent);
		slider_text_y.setValue(subtitleYPercent);

		// Apply filter tint
		applyFilterOverlay(activeFilterId);

		// Apply sticker
		applyStickerPreview(activeStickerText);

		// Apply trim slider values
		if (isFallbackPreview) {
			range_slider.setValues(Arrays.asList(trimStartSec, trimEndSec));
			updateTrimLabels();
			previewCurrentPosMs = (int) (trimStartSec * 1000);
		} else if (playerView != null) {
			range_slider.setValues(Arrays.asList(trimStartSec, trimEndSec));
			updateTrimLabels();
			if (exoPlayer != null) exoPlayer.seekTo((int) (trimStartSec * 1000));
		}
	}

	private void showPanel(View panelToShow) {
		panel_clip_settings.setVisibility(View.GONE);
		panel_canvas_presets.setVisibility(View.GONE);
		panel_text_input.setVisibility(View.GONE);
		panel_trim_editor.setVisibility(View.GONE);
		if (panel_filters != null) panel_filters.setVisibility(View.GONE);
		if (panel_audio != null) panel_audio.setVisibility(View.GONE);
		if (panel_stickers != null) panel_stickers.setVisibility(View.GONE);
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

		applyVideoViewAspectRatio(preset);
	}

	private void applyVideoViewAspectRatio(int preset) {
		if (playerView == null || video_container == null) return;
		
		int containerWidth = video_container.getWidth();
		int containerHeight = video_container.getHeight();
		
		if (containerWidth <= 0 || containerHeight <= 0) {
			return;
		}

		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) playerView.getLayoutParams();
		if (preset == 0) { // Original
			lp.width = FrameLayout.LayoutParams.MATCH_PARENT;
			lp.height = FrameLayout.LayoutParams.MATCH_PARENT;
			lp.gravity = android.view.Gravity.CENTER;
		} else {
			float targetRatio = 1f;
			if (preset == 1) { // 16:9
				targetRatio = 16f / 9f;
			} else if (preset == 2) { // 9:16
				targetRatio = 9f / 16f;
			} else if (preset == 3) { // 1:1
				targetRatio = 1f;
			}
			
			float containerRatio = (float) containerWidth / containerHeight;
			if (containerRatio > targetRatio) {
				lp.height = containerHeight;
				lp.width = (int) (containerHeight * targetRatio);
			} else {
				lp.width = containerWidth;
				lp.height = (int) (containerWidth / targetRatio);
			}
			lp.gravity = android.view.Gravity.CENTER;
		}
		
		playerView.setLayoutParams(lp);

		// Synchronize filter overlay bounds
		if (video_filter_overlay != null) {
			FrameLayout.LayoutParams overlayLp = (FrameLayout.LayoutParams) video_filter_overlay.getLayoutParams();
			overlayLp.width = lp.width;
			overlayLp.height = lp.height;
			overlayLp.gravity = lp.gravity;
			video_filter_overlay.setLayoutParams(overlayLp);
		}
	}

	private void applyFilterOverlay(int filterId) {
		activeFilterId = filterId;
		if (video_filter_overlay == null) return;
		if (filterId == R.id.btn_filter_none) {
			video_filter_overlay.setVisibility(View.GONE);
			video_filter_overlay.setBackgroundColor(Color.TRANSPARENT);
		} else if (filterId == R.id.btn_filter_warm) {
			video_filter_overlay.setVisibility(View.VISIBLE);
			video_filter_overlay.setBackgroundColor(Color.parseColor("#33FF9800"));
		} else if (filterId == R.id.btn_filter_cool) {
			video_filter_overlay.setVisibility(View.VISIBLE);
			video_filter_overlay.setBackgroundColor(Color.parseColor("#3300BCD4"));
		} else if (filterId == R.id.btn_filter_vintage) {
			video_filter_overlay.setVisibility(View.VISIBLE);
			video_filter_overlay.setBackgroundColor(Color.parseColor("#44795548"));
		}

		if (tv_track_effects_label != null) {
			if (filterId == R.id.btn_filter_none) tv_track_effects_label.setText("No Filter");
			else if (filterId == R.id.btn_filter_warm) tv_track_effects_label.setText("Warm");
			else if (filterId == R.id.btn_filter_cool) tv_track_effects_label.setText("Cool");
			else if (filterId == R.id.btn_filter_vintage) tv_track_effects_label.setText("Vintage");
		}

		// Update button styles
		Button[] buttons = {btn_filter_none, btn_filter_warm, btn_filter_cool, btn_filter_vintage};
		int activeColor = getResources().getColor(R.color.colorAccent);
		int activeTextColor = getResources().getColor(R.color.lumina_bg);
		int normalColor = getResources().getColor(R.color.lumina_surface_container);
		int normalTextColor = getResources().getColor(R.color.lumina_text_primary);

		for (Button b : buttons) {
			if (b != null) {
				if (b.getId() == filterId) {
					b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
					b.setTextColor(activeTextColor);
				} else {
					b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(normalColor));
					b.setTextColor(normalTextColor);
				}
			}
		}
	}

	private void applyStickerPreview(String stickerText) {
		activeStickerText = stickerText;
		if (tv_sticker_preview == null) return;
		if (stickerText == null || stickerText.isEmpty()) {
			tv_sticker_preview.setVisibility(View.GONE);
		} else {
			tv_sticker_preview.setVisibility(View.VISIBLE);
			tv_sticker_preview.setText(stickerText);
		}

		// Update button styles
		Button[] buttons = {btn_sticker_fire, btn_sticker_sparkles, btn_sticker_heart};
		int activeColor = getResources().getColor(R.color.colorAccent);
		int activeTextColor = getResources().getColor(R.color.lumina_bg);
		int normalColor = getResources().getColor(R.color.lumina_surface_container);
		int normalTextColor = getResources().getColor(R.color.lumina_text_primary);

		for (Button b : buttons) {
			if (b != null) {
				String bText = b.getText().toString();
				if (bText.contains(stickerText) && !stickerText.isEmpty()) {
					b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
					b.setTextColor(activeTextColor);
				} else {
					b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(normalColor));
					b.setTextColor(normalTextColor);
				}
			}
		}
	}

	private void showVolumeDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
		builder.setTitle(R.string.volume_label);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(40, 20, 40, 20);
		
		final TextView tvVol = new TextView(this);
		tvVol.setText(getString(R.string.volume_settings) + ": " + (int) (videoVolume * 100) + "%");
		tvVol.setTextColor(getResources().getColor(R.color.lumina_text_primary));
		layout.addView(tvVol);
		
		final Slider slider = new Slider(this);
		slider.setValueFrom(0f);
		slider.setValueTo(100f);
		slider.setValue(videoVolume * 100f);
		slider.addOnChangeListener(new Slider.OnChangeListener() {
			@Override
			public void onValueChange(Slider slider, float value, boolean fromUser) {
				tvVol.setText(getString(R.string.volume_settings) + ": " + (int) value + "%");
			}
		});
		layout.addView(slider);
		
		builder.setView(layout);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				pushStateToUndo();
				videoVolume = slider.getValue() / 100f;
				Toast.makeText(EditActivity.this, R.string.toast_volume_success, Toast.LENGTH_SHORT).show();
			}
		});
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.show();
	}

	private void resetEditorState() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
		builder.setTitle(R.string.alert_reset_title);
		builder.setMessage(R.string.alert_reset_message);
		builder.setPositiveButton(R.string.alert_reset_btn, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				pushStateToUndo();
				currentRotation = 0;
				isMirror = false;
				selectedCropPreset = 0;
				activeFilterId = R.id.btn_filter_none;
				activeStickerText = "";
				isEnhanced = false;
				
				currentSpeed = 1.0f;
				selectedEffect = 3;
				selectedTransition = -1;
				selectedOverlay = 3;
				videoVolume = 1.0f;
				applyPlaybackSpeed(1.0f);
				
				updateVideoTransformations();
				applyVideoViewAspectRatio(0);
				updateCropPresetButtons(0);
				updateToggleButton(btn_action_enhance, false);
				applyFilterOverlay(R.id.btn_filter_none);
				applyStickerPreview("");
				
				et_subtitle_input.removeTextChangedListener(subtitleTextWatcher);
				et_subtitle_input.setText("");
				et_subtitle_input.addTextChangedListener(subtitleTextWatcher);
				subtitleText = "";
				updateSubtitlePreview();
				
				if (playerView != null) {
					int durationMs = (exoPlayer != null ? (int)exoPlayer.getDuration() : 0);
					trimStartSec = 0f;
					trimEndSec = durationMs / 1000f;
					range_slider.setValues(Arrays.asList(0f, trimEndSec));
					updateTrimLabels();
					if (exoPlayer != null) exoPlayer.seekTo(1);
				}
				Toast.makeText(EditActivity.this, R.string.toast_delete_success, Toast.LENGTH_SHORT).show();
			}
		});
		builder.setNegativeButton(R.string.alert_cancel_btn, null);
		builder.show();
	}


	private void togglePlayPause() {
		if (playerView == null) return;
		if (isFallbackPreview) {
			if (isFallbackPreviewPlaying) {
				isFallbackPreviewPlaying = false;
				iv_play_pause.setImageResource(R.drawable.ic_play);
				pausePreviewAudio();
			} else {
				isFallbackPreviewPlaying = true;
				iv_play_pause.setImageResource(R.drawable.ic_pause);
				playPreviewAudio();
			}
		} else {
			if ((exoPlayer != null && exoPlayer.isPlaying())) {
				if (exoPlayer != null) exoPlayer.pause();
				iv_play_pause.setImageResource(R.drawable.ic_play);
				pausePreviewAudio();
			} else {
				if (exoPlayer != null) exoPlayer.play();
				iv_play_pause.setImageResource(R.drawable.ic_pause);
				playPreviewAudio();
			}
		}
	}

	private String findAnyRealVideo() {
		try {
			android.database.Cursor cursor = getContentResolver().query(
				android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
				new String[]{android.provider.MediaStore.Video.Media.DATA},
				null, null, android.provider.MediaStore.Video.Media.DATE_ADDED + " DESC"
			);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					String p = cursor.getString(0);
					if (p != null && new java.io.File(p).exists()) {
						cursor.close();
						return p;
					}
				}
				cursor.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void setupVideoPlayer(final String path) {
		playerView.setVisibility(View.VISIBLE);
		video_empty_overlay.setVisibility(View.GONE);
		bt_file.setVisibility(View.GONE);
		timeline_container.setVisibility(View.VISIBLE);
		layout_video_controls.setVisibility(View.VISIBLE);
		scroll_bottom_tabs.setVisibility(View.VISIBLE);

		splitPoints.clear();

		isFallbackPreview = false;
		if (!new java.io.File(path).exists()) {
			Toast.makeText(this, R.string.toast_export_no_source, Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		{
			playerView.setBackgroundColor(Color.TRANSPARENT);
			if (exoPlayer != null) {
				exoPlayer.release();
			}
			exoPlayer = new ExoPlayer.Builder(this)
					.setVideoChangeFrameRateStrategy(C.VIDEO_CHANGE_FRAME_RATE_STRATEGY_OFF)
					.build();
			playerView.setPlayer(exoPlayer);
			
			MediaItem mediaItem = MediaItem.fromUri(path);
			exoPlayer.setMediaItem(mediaItem);
			exoPlayer.prepare();
			exoPlayer.addListener(new Player.Listener() {
				@Override
				public void onPlaybackStateChanged(int playbackState) {
					if (playbackState == Player.STATE_READY) {
						if (videoWidth == 0) {
							int durationMs = (int) exoPlayer.getDuration();
							float durationSec = durationMs / 1000f;
							
							trimStartSec = 0f;
							trimEndSec = durationSec;
							
							if (exoPlayer.getVideoFormat() != null) {
								videoWidth = exoPlayer.getVideoFormat().width;
								videoHeight = exoPlayer.getVideoFormat().height;
							}
							
							range_slider.setValueFrom(0f);
							range_slider.setValueTo(durationSec);
							range_slider.setValues(java.util.Arrays.asList(0f, durationSec));
							
							updateTrimLabels();
							
							int screenWidth = getResources().getDisplayMetrics().widthPixels;
							int halfWidth = screenWidth / 2;
							timeline_scroll.setPadding(halfWidth, 0, halfWidth, 0);
							timeline_scroll.setClipToPadding(false);
							
							exoPlayer.seekTo(1);
							iv_play_pause.setImageResource(R.drawable.ic_play);
							
							loadTimelineThumbnails(path);
							
							currentRotation = 0;
							isMirror = false;
							selectedCropPreset = 0;
							activeFilterId = R.id.btn_filter_none;
							activeStickerText = "";
							isEnhanced = false;
							
							updateVideoTransformations();
							updateToggleButton(btn_action_enhance, false);
							
							playerView.post(new Runnable() {
								@Override
								public void run() {
									applyVideoViewAspectRatio(0);
								}
							});
							
							updateCropPresetButtons(0);
							applyFilterOverlay(R.id.btn_filter_none);
							applyStickerPreview("");
							
							et_subtitle_input.removeTextChangedListener(subtitleTextWatcher);
							et_subtitle_input.setText("");
							et_subtitle_input.addTextChangedListener(subtitleTextWatcher);
							subtitleText = "";
							subtitleXPercent = 50f;
							subtitleYPercent = 85f;
							subtitleScale = 1.0f;
							subtitleRotation = 0f;
							slider_text_x.setValue(50f);
							slider_text_y.setValue(85f);
							updateSubtitlePreview();
							
							showPanel(panel_clip_settings);
							setActiveTab(tab_edit, iv_tab_edit, tv_tab_edit);
							
							undoStack.clear();
							redoStack.clear();
							updateUndoRedoButtonsVisibility();
						}
					} else if (playbackState == Player.STATE_ENDED) {
						exoPlayer.seekTo((long) (trimStartSec * 1000));
						exoPlayer.play();
					}
				}
			});
		}
	}

	private void updateTrimLabels() {
		float duration = trimEndSec - trimStartSec;
		tv_trim_start.setText(getString(R.string.trim_start_label, trimStartSec));
		tv_trim_end.setText(getString(R.string.trim_end_label, trimEndSec));
		tv_trim_duration.setText(getString(R.string.trim_duration_label, duration));
	}

	private void updateVideoTransformations() {
		if (playerView == null) return;
		playerView.setRotation(currentRotation);
		playerView.setScaleX(isMirror ? -1f : 1f);
		
		updateToggleButton(btn_action_mirror, isMirror);
		updateToggleButton(btn_action_enhance, isEnhanced);
		
		if (exoPlayer != null) {
			java.util.List<androidx.media3.common.Effect> effects = new java.util.ArrayList<>();
			if (isEnhanced) {
				effects.add(new androidx.media3.effect.Contrast(1.0f)); // Max contrast for obvious visual indicator
			}
			exoPlayer.setVideoEffects(effects);
			
			// Force the player to redraw the current frame so the effect shows immediately
			if (!exoPlayer.isPlaying()) {
				exoPlayer.seekTo(exoPlayer.getCurrentPosition());
			}
		}
	}

	private void updateToggleButton(LinearLayout btnLayout, boolean isActive) {
		if (btnLayout == null) return;
		int activeColor = getResources().getColor(R.color.colorAccent);
		int normalColor = getResources().getColor(R.color.lumina_text_primary);
		for (int i = 0; i < btnLayout.getChildCount(); i++) {
			View child = btnLayout.getChildAt(i);
			if (child instanceof ImageView) {
				((ImageView) child).setImageTintList(android.content.res.ColorStateList.valueOf(isActive ? activeColor : normalColor));
			} else if (child instanceof TextView) {
				((TextView) child).setTextColor(isActive ? activeColor : normalColor);
			}
		}
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
			tv_subtitle_preview.setScaleX(subtitleScale);
			tv_subtitle_preview.setScaleY(subtitleScale);
			tv_subtitle_preview.setRotation(subtitleRotation);
		} else {
			tv_subtitle_preview.setVisibility(View.GONE);
		}
	}

	private void loadTimelineThumbnails(final String videoPath) {
		loadTimelineThumbnails(videoPath, true);
	}

	private void loadTimelineThumbnails(final String videoPath, final boolean clearOld) {
		final LinearLayout thumbnailContainer = timeline_thumbnails;
		if (clearOld) thumbnailContainer.removeAllViews();
		
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
						final int finalI = i;
						long timeUs = i * intervalUs;
						final Bitmap bitmap = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
						
						if (bitmap != null) {
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									ImageView imageView = new ImageView(EditActivity.this);
									float density = getResources().getDisplayMetrics().density;
									int widthPx = (int) ((isTimelineZoomed ? 120 : 60) * density);
									int heightPx = (int) (45 * density);
									LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPx, heightPx);
									params.setMargins(2, 0, 2, 0);
									imageView.setLayoutParams(params);
									imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
									imageView.setImageBitmap(bitmap);
									thumbnailContainer.addView(imageView);

									// Add visual split divider if a split point falls here
									boolean hasSplit = false;
									for (float sp : splitPoints) {
										float itemStart = (float) finalI / numThumbs;
										float itemEnd = (float) (finalI + 1) / numThumbs;
										if (sp >= itemStart && sp <= itemEnd) {
											hasSplit = true;
											break;
										}
									}
									if (hasSplit) {
										View divider = new View(EditActivity.this);
										LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams((int)(4 * density), heightPx);
										divParams.setMargins(4, 0, 4, 0);
										divider.setLayoutParams(divParams);
										divider.setBackgroundColor(getResources().getColor(R.color.colorAccent));
										thumbnailContainer.addView(divider);
									}
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

	private void loadFallbackTimelineThumbnails(final String path) {
		final LinearLayout thumbnailContainer = timeline_thumbnails;
		thumbnailContainer.removeAllViews();
		
		int baseColor = Color.parseColor("#3f51b5");
		if (path.contains("mountain")) {
			baseColor = Color.parseColor("#009688");
		} else if (path.contains("cybercity")) {
			baseColor = Color.parseColor("#9c27b0");
		} else if (path.contains("forest")) {
			baseColor = Color.parseColor("#4caf50");
		}
		
		float density = getResources().getDisplayMetrics().density;
		final int numThumbs = 10;
		int widthPx = (int) ((isTimelineZoomed ? 120 : 60) * density);
		int heightPx = (int) (45 * density);
		
		for (int i = 0; i < numThumbs; i++) {
			final int finalI = i;
			ImageView imageView = new ImageView(EditActivity.this);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthPx, heightPx);
			params.setMargins(2, 0, 2, 0);
			imageView.setLayoutParams(params);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			
			int alpha = 180 + (i * 7) % 75;
			int red = Color.red(baseColor);
			int green = Color.green(baseColor);
			int blue = Color.blue(baseColor);
			
			red = Math.max(0, Math.min(255, red + (i - 5) * 15));
			green = Math.max(0, Math.min(255, green + (i - 5) * 10));
			blue = Math.max(0, Math.min(255, blue + (i - 5) * 20));
			
			imageView.setBackgroundColor(Color.argb(alpha, red, green, blue));
			thumbnailContainer.addView(imageView);

			// Add visual split divider if a split point falls here
			boolean hasSplit = false;
			for (float sp : splitPoints) {
				float itemStart = (float) finalI / numThumbs;
				float itemEnd = (float) (finalI + 1) / numThumbs;
				if (sp >= itemStart && sp <= itemEnd) {
					hasSplit = true;
					break;
				}
			}
			if (hasSplit) {
				View divider = new View(EditActivity.this);
				LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams((int)(4 * density), heightPx);
				divParams.setMargins(4, 0, 4, 0);
				divider.setLayoutParams(divParams);
				divider.setBackgroundColor(getResources().getColor(R.color.colorAccent));
				thumbnailContainer.addView(divider);
			}
		}
	}

	private void startPlayProgressTracker() {
		if (playRunnable != null) {
			playRunnable = null;
		}
		playRunnable = new Runnable() {
			@Override
			public void run() {
				if (isFallbackPreview) {
					if (isFallbackPreviewPlaying && !isUserSeeking) {
						previewCurrentPosMs += (int) (33 * currentSpeed);
						float currentPosSec = previewCurrentPosMs / 1000f;
						if (trimEndSec > 0 && currentPosSec >= trimEndSec) {
							previewCurrentPosMs = (int) (trimStartSec * 1000);
						} else {
							updateTimecodes(previewCurrentPosMs);
							if (previewDurationMs > 0) {
								float progressRatio = (float) previewCurrentPosMs / previewDurationMs;
								int trackWidth = layout_tracks_wrapper.getWidth();
								if (trackWidth > 0) {
									int targetScrollX = (int) (progressRatio * trackWidth);
									timeline_scroll.scrollTo(targetScrollX, 0);
								}
							}
						}
					}
				} else {
					if ((exoPlayer != null && exoPlayer.isPlaying()) && !isUserSeeking) {
						int currentPosMs = (exoPlayer != null ? (int)exoPlayer.getCurrentPosition() : 0);
						float currentPosSec = currentPosMs / 1000f;
						
						if (trimEndSec > 0 && currentPosSec >= trimEndSec) {
							if (exoPlayer != null) exoPlayer.seekTo((int) (trimStartSec * 1000));
						} else {
							updateTimecodes(currentPosMs);
							int durationMs = (exoPlayer != null ? (int)exoPlayer.getDuration() : 0);
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

	
	private void appendVideo(String path) {
		if (exoPlayer != null) {
			androidx.media3.common.MediaItem mediaItem = androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(path));
			exoPlayer.addMediaItem(mediaItem);
			if (!exoPlayer.isPlaying()) {
				exoPlayer.prepare();
			}
			loadTimelineThumbnails(path, false);
            Toast.makeText(this, R.string.toast_video_added, Toast.LENGTH_SHORT).show();
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
			String newVideoUrl = UriUtils.getPath(EditActivity.this, data.getData());
			videoUrls.add(newVideoUrl);
			if (videoUrls.size() == 1) {
				setupVideoPlayer(newVideoUrl);
			} else {
				appendVideo(newVideoUrl);
			}
		}
	}

	private void execVideo() {
		if (!videoUrls.isEmpty()) {
			Intent intent = new Intent(EditActivity.this, ExportActivity.class);
			intent.putStringArrayListExtra("VIDEO_PATHS", videoUrls);
			intent.putExtra("TRIM_START", trimStartSec);
			intent.putExtra("TRIM_END", trimEndSec);
			intent.putExtra("CROP_PRESET", selectedCropPreset);
			intent.putExtra("ROTATION", currentRotation);
			intent.putExtra("MIRROR", isMirror);
			intent.putExtra("SUBTITLE_TEXT", subtitleText);
			intent.putExtra("SUBTITLE_X", subtitleXPercent);
			intent.putExtra("SUBTITLE_Y", subtitleYPercent);
			intent.putExtra("SUBTITLE_SCALE", subtitleScale);
			intent.putExtra("VIDEO_WIDTH", videoWidth);
			intent.putExtra("VIDEO_HEIGHT", videoHeight);
			intent.putExtra("SPEED", currentSpeed);
			intent.putExtra("FILTER_ID", activeFilterId);
			intent.putExtra("EFFECT_ID", selectedEffect);
			intent.putExtra("OVERLAY_ID", selectedOverlay);
			intent.putExtra("VIDEO_VOLUME", videoVolume);
			intent.putExtra("STICKER_TEXT", activeStickerText);
			intent.putExtra("TRANSITION_ID", selectedTransition);
			intent.putExtra("ENHANCE", isEnhanced);
			if (selectedAudioPath != null) {
				intent.putExtra("AUDIO_PATH", selectedAudioPath);
			}
			startActivity(intent);
		} else {
			Toast.makeText(this, R.string.toast_select_video_first, Toast.LENGTH_SHORT).show();
		}
	}

	private void showSpeedDialog() {
		final String[] speeds = {"0.5x", "1.0x (Mặc định)", "1.5x", "2.0x"};
		final float[] speedValues = {0.5f, 1.0f, 1.5f, 2.0f};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
		builder.setTitle(R.string.speed_presets);
		
		int checkedItem = 1;
		for (int i = 0; i < speedValues.length; i++) {
			if (speedValues[i] == currentSpeed) {
				checkedItem = i;
				break;
			}
		}
		
		builder.setSingleChoiceItems(speeds, checkedItem, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				pushStateToUndo();
				applyPlaybackSpeed(speedValues[which]);
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(R.string.alert_cancel_btn, null);
		builder.show();
	}

	private void applyPlaybackSpeed(float speed) {
		currentSpeed = speed;
		if (tv_speed_val != null) {
			tv_speed_val.setText(speed + "x");
		}
		if (realVideoPlayer != null) {
			try {
				if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
					realVideoPlayer.setPlaybackParams(realVideoPlayer.getPlaybackParams().setSpeed(speed));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isFallbackPreview) {
			isFallbackPreviewPlaying = false;
			iv_play_pause.setImageResource(R.drawable.ic_play);
		} else if ((exoPlayer != null && exoPlayer.isPlaying())) {
			if (exoPlayer != null) exoPlayer.pause();
			iv_play_pause.setImageResource(R.drawable.ic_play);
		}
		pausePreviewAudio();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		realVideoPlayer = null;
		if (playRunnable != null) {
			playHandler.removeCallbacks(playRunnable);
		}
		stopPreviewAudio();
	}

	private void applyInitialTool(final String tool) {
		new android.os.Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if ("remove_bg".equals(tool)) {
					showAiUnavailable("AI background removal");
				} else if ("auto_captions".equals(tool)) {
					showPanel(panel_text_input);
					setActiveTab(tab_text, iv_tab_text, tv_tab_text);
					showAiUnavailable("AI auto captions");
				} else if ("cutout".equals(tool)) {
					showAiUnavailable("AI smart cutout");
				} else if ("voice_changer".equals(tool)) {
					showPanel(panel_audio);
					setActiveTab(tab_audio, iv_tab_audio, tv_tab_audio);
					showAiUnavailable("AI voice changer");
				}
			}
		}, 1000);
	}

	private void showAiUnavailable(String featureName) {
		Toast.makeText(this, featureName + " requires a configured AI engine before production use.", Toast.LENGTH_LONG).show();
	}

	private void showEffectsDialog() {
		final String[] effects = {"Glitch VHS", "Cinematic Light Leak", "Neon Cyberpunk Glow", "None"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
		builder.setTitle(R.string.toast_effects_selected);
		builder.setItems(effects, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				pushStateToUndo();
				selectedEffect = which;
				Toast.makeText(EditActivity.this, "Applied Effect: " + effects[which], Toast.LENGTH_SHORT).show();
				if (video_filter_overlay != null) {
					video_filter_overlay.setVisibility(View.VISIBLE);
					if (which == 0) {
						video_filter_overlay.setBackgroundColor(Color.argb(50, 255, 0, 128));
					} else if (which == 1) {
						video_filter_overlay.setBackgroundColor(Color.argb(40, 255, 128, 0));
					} else if (which == 2) {
						video_filter_overlay.setBackgroundColor(Color.argb(55, 0, 255, 255));
					} else {
						video_filter_overlay.setVisibility(View.GONE);
					}
				}
			}
		});
		builder.show();
	}

	private void showTransitionsDialog() {
		final String[] transitions = {"Fade to Black", "Cross Dissolve", "Wipe Left", "Zoom In"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
		builder.setTitle(R.string.toast_transitions_selected);
		builder.setItems(transitions, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				selectedTransition = which;
				Toast.makeText(EditActivity.this, "Applied Transition: " + transitions[which], Toast.LENGTH_SHORT).show();
				if (playerView != null) {
					playerView.animate().alpha(0.0f).setDuration(250).withEndAction(new Runnable() {
						@Override
						public void run() {
							playerView.animate().alpha(1.0f).setDuration(250).start();
						}
					}).start();
				}
			}
		});
		builder.show();
	}

	private void showOverlaysDialog() {
		final String[] overlays = {"Vignette Shadow", "Retro Vignette", "Cinematic Letterbox (Black Bars)", "None"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Dialog_Alert);
		builder.setTitle(R.string.toast_overlay_selected);
		builder.setItems(overlays, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				pushStateToUndo();
				selectedOverlay = which;
				Toast.makeText(EditActivity.this, "Applied Overlay: " + overlays[which], Toast.LENGTH_SHORT).show();
				if (video_filter_overlay != null) {
					if (which == 0) {
						video_filter_overlay.setVisibility(View.VISIBLE);
						video_filter_overlay.setBackgroundColor(Color.argb(80, 0, 0, 0));
					} else if (which == 1) {
						video_filter_overlay.setVisibility(View.VISIBLE);
						video_filter_overlay.setBackgroundColor(Color.argb(60, 139, 69, 19));
					} else if (which == 2) {
						video_filter_overlay.setVisibility(View.VISIBLE);
						video_filter_overlay.setBackgroundColor(Color.argb(120, 10, 10, 10));
					} else {
						video_filter_overlay.setVisibility(View.GONE);
					}
				}
			}
		});
		builder.show();
	}

	private void applyTemplatePreset(String templateId) {
		if ("template_neon".equals(templateId)) {
			Toast.makeText(this, "Applying Vlog Neon Beats Preset...", Toast.LENGTH_SHORT).show();
			applyFilterOverlay(R.id.btn_filter_cool);
			selectAudioTrack("audio/track_electronic.mp3");
			subtitleText = "Vlog Neon Beats #trending";
			tv_subtitle_preview.post(new Runnable() {
				@Override
				public void run() {
					updateSubtitlePreview();
				}
			});
		} else if ("template_retro".equals(templateId)) {
			Toast.makeText(this, "Applying Travel Diary Preset...", Toast.LENGTH_SHORT).show();
			applyFilterOverlay(R.id.btn_filter_warm);
			selectAudioTrack("audio/track_acoustic.mp3");
			subtitleText = "Travel Diary: Explore the World";
			tv_subtitle_preview.post(new Runnable() {
				@Override
				public void run() {
					updateSubtitlePreview();
				}
			});
		} else if ("template_soft".equals(templateId)) {
			Toast.makeText(this, "Applying Chilling Mood Preset...", Toast.LENGTH_SHORT).show();
			applyFilterOverlay(R.id.btn_filter_vintage);
			selectAudioTrack("audio/track_lofi.mp3");
			subtitleText = "Chilling vibes...";
			tv_subtitle_preview.post(new Runnable() {
				@Override
				public void run() {
					updateSubtitlePreview();
				}
			});
		}
	}

	private void selectAudioTrack(String assetPath) {
		selectedAudioPath = assetPath;
		
		int activeColor = getResources().getColor(R.color.colorAccent);
		int activeTextColor = getResources().getColor(R.color.lumina_bg);
		int normalColor = getResources().getColor(R.color.lumina_surface_container);
		int normalTextColor = getResources().getColor(R.color.lumina_text_primary);

		Button[] buttons = {btn_audio_track1, btn_audio_track2, btn_audio_track3};
		String[] paths = {"audio/track_lofi.mp3", "audio/track_electronic.mp3", "audio/track_acoustic.mp3"};

		for (int i = 0; i < buttons.length; i++) {
			Button b = buttons[i];
			if (b != null) {
				if (paths[i].equals(assetPath)) {
					b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
					b.setTextColor(activeTextColor);
				} else {
					b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(normalColor));
					b.setTextColor(normalTextColor);
				}
			}
		}

		boolean isOnline = assetPath != null && !assetPath.startsWith("audio/");
		if (btn_audio_online != null) {
			if (isOnline) {
				btn_audio_online.setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
				btn_audio_online.setTextColor(activeTextColor);
			} else {
				btn_audio_online.setBackgroundTintList(android.content.res.ColorStateList.valueOf(normalColor));
				btn_audio_online.setTextColor(normalTextColor);
			}
		}

		stopPreviewAudio();
		if (isFallbackPreviewPlaying || ((exoPlayer != null && exoPlayer.isPlaying()))) {
			playPreviewAudio();
		}
	}

	private android.media.MediaPlayer previewAudioPlayer = null;

	private void playPreviewAudio() {
		if (selectedAudioPath == null || selectedAudioPath.isEmpty()) {
			return;
		}
		try {
			if (previewAudioPlayer == null) {
				previewAudioPlayer = new android.media.MediaPlayer();
				if (selectedAudioPath.startsWith("audio/")) {
					android.content.res.AssetFileDescriptor afd = getAssets().openFd(selectedAudioPath);
					previewAudioPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
					afd.close();
				} else {
					previewAudioPlayer.setDataSource(selectedAudioPath);
				}
				previewAudioPlayer.setLooping(true);
				previewAudioPlayer.prepare();
			}
			if (!previewAudioPlayer.isPlaying()) {
				previewAudioPlayer.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void pausePreviewAudio() {
		try {
			if (previewAudioPlayer != null && previewAudioPlayer.isPlaying()) {
				previewAudioPlayer.pause();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stopPreviewAudio() {
		try {
			if (previewAudioPlayer != null) {
				if (previewAudioPlayer.isPlaying()) {
					previewAudioPlayer.stop();
				}
				previewAudioPlayer.release();
				previewAudioPlayer = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class OnlineTrack {
		String id;
		String title;
		String artist;
		String category;
		String url;
		boolean isPlaying = false;
		boolean isLoading = false;
	}

	private class OnlineTrackAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<OnlineTrackAdapter.ViewHolder> {
		private java.util.List<OnlineTrack> list;
		private android.media.MediaPlayer player;
		private int currentPlayingPos = -1;

		public OnlineTrackAdapter(java.util.List<OnlineTrack> list) {
			this.list = list;
		}

		public void stopPlaying() {
			if (player != null) {
				try {
					if (player.isPlaying()) {
						player.stop();
					}
					player.release();
				} catch (Exception e) {
					e.printStackTrace();
				}
				player = null;
			}
			if (currentPlayingPos != -1 && currentPlayingPos < list.size()) {
				list.get(currentPlayingPos).isPlaying = false;
				list.get(currentPlayingPos).isLoading = false;
				notifyItemChanged(currentPlayingPos);
				currentPlayingPos = -1;
			}
		}

		@Override
		public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
			android.view.View v = android.view.LayoutInflater.from(parent.getContext()).inflate(R.layout.item_online_track, parent, false);
			return new ViewHolder(v);
		}

		@Override
		public void onBindViewHolder(final ViewHolder holder, final int position) {
			final OnlineTrack track = list.get(holder.getAdapterPosition());
			holder.tv_title.setText(track.title);
			holder.tv_artist.setText(track.artist);
			holder.tv_category.setText(track.category);

			// Generate premium vibrant gradient cover
			int hash = track.id.hashCode();
			int[][] presetColors = {
				{Color.parseColor("#FF416C"), Color.parseColor("#FF4B2B")}, // Warm Sunset Red-Orange
				{Color.parseColor("#00B4DB"), Color.parseColor("#0083B0")}, // Ocean Breeze Blue
				{Color.parseColor("#F1F2B5"), Color.parseColor("#135058")}, // Elegant Forest Teal
				{Color.parseColor("#7F00FF"), Color.parseColor("#E100FF")}, // Neon Purple-Pink
				{Color.parseColor("#11998e"), Color.parseColor("#38ef7d")}, // Emerald Mint Green
				{Color.parseColor("#fc00ff"), Color.parseColor("#00dbde")}, // Cyberpunk Violet-Cyan
				{Color.parseColor("#3a7bd5"), Color.parseColor("#3a6073")}  // Slate Deep Blue
			};
			int index = Math.abs(hash) % presetColors.length;
			android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable(
				android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
				presetColors[index]
			);
			holder.iv_gradient.setImageDrawable(gd);

			if (track.isLoading) {
				holder.pb_loading.setVisibility(android.view.View.VISIBLE);
				holder.iv_play.setVisibility(android.view.View.GONE);
			} else {
				holder.pb_loading.setVisibility(android.view.View.GONE);
				holder.iv_play.setVisibility(android.view.View.VISIBLE);
				if (track.isPlaying) {
					holder.iv_play.setImageResource(R.drawable.ic_pause);
				} else {
					holder.iv_play.setImageResource(R.drawable.ic_play);
				}
			}

			holder.layout_play.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(android.view.View v) {
					final int pos = holder.getAdapterPosition();
					if (pos == -1) return;
					
					stopPreviewAudio();
					if (isFallbackPreview) {
						isFallbackPreviewPlaying = false;
						iv_play_pause.setImageResource(R.drawable.ic_play);
					} else if ((exoPlayer != null && exoPlayer.isPlaying())) {
						if (exoPlayer != null) exoPlayer.pause();
						iv_play_pause.setImageResource(R.drawable.ic_play);
					}

					if (track.isPlaying) {
						stopPlaying();
					} else {
						stopPlaying();
						track.isLoading = true;
						currentPlayingPos = pos;
						notifyItemChanged(pos);

						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									if (player != null) {
										player.release();
										player = null;
									}
									
									String localName = "online_track_preview_" + track.id + ".mp3";
									final java.io.File cachedFile = new java.io.File(getCacheDir(), localName);
									
									if (!cachedFile.exists()) {
										final String resolvedUrl = resolveRedirects(track.url);
										java.io.File tmpFile = new java.io.File(getCacheDir(), localName + ".tmp");
										java.net.URL url = new java.net.URL(resolvedUrl);
										java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
										connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
										connection.setConnectTimeout(10000);
										connection.setReadTimeout(10000);
										connection.connect();

										if (connection.getResponseCode() != java.net.HttpURLConnection.HTTP_OK) {
											throw new Exception("HTTP " + connection.getResponseCode());
										}

										java.io.InputStream input = connection.getInputStream();
										java.io.OutputStream output = new java.io.FileOutputStream(tmpFile);

										byte[] data = new byte[4096];
										int count;
										while ((count = input.read(data)) != -1) {
											output.write(data, 0, count);
										}
										output.flush();
										output.close();
										input.close();
										connection.disconnect();

										if (!tmpFile.renameTo(cachedFile)) {
											throw new Exception("Lỗi lưu file cache");
										}
									}
									
									player = new android.media.MediaPlayer();
									player.setDataSource(cachedFile.getAbsolutePath());
									player.prepare();
									player.setOnCompletionListener(new android.media.MediaPlayer.OnCompletionListener() {
										@Override
										public void onCompletion(android.media.MediaPlayer mp) {
											runOnUiThread(new Runnable() {
												@Override
												public void run() {
													stopPlaying();
												}
											});
										}
									});
									
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											track.isLoading = false;
											track.isPlaying = true;
											notifyItemChanged(pos);
											try {
												player.start();
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									});
								} catch (final Exception e) {
									e.printStackTrace();
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											track.isLoading = false;
											track.isPlaying = false;
											notifyItemChanged(pos);
											Toast.makeText(EditActivity.this, "Không thể nghe thử: " + e.getMessage(), Toast.LENGTH_SHORT).show();
										}
									});
								}
							}
						}).start();
					}
				}
			});

			holder.btn_use.setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(android.view.View v) {
					stopPlaying();
					if (dialog_online_music_instance != null) {
						dialog_online_music_instance.dismiss();
					}
					downloadAndApplyOnlineTrack(track);
				}
			});
		}

		@Override
		public int getItemCount() {
			return list.size();
		}

		class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
			TextView tv_title, tv_artist, tv_category;
			ImageView iv_play, btn_use, iv_gradient;
			ProgressBar pb_loading;
			android.view.View layout_play;

			public ViewHolder(android.view.View itemView) {
				super(itemView);
				tv_title = (TextView) itemView.findViewById(R.id.tv_track_title);
				tv_artist = (TextView) itemView.findViewById(R.id.tv_track_artist);
				tv_category = (TextView) itemView.findViewById(R.id.tv_track_category);
				iv_play = (ImageView) itemView.findViewById(R.id.iv_track_play);
				btn_use = (ImageView) itemView.findViewById(R.id.btn_use_track);
				pb_loading = (ProgressBar) itemView.findViewById(R.id.pb_track_loading);
				layout_play = itemView.findViewById(R.id.layout_play_state);
				iv_gradient = (ImageView) itemView.findViewById(R.id.iv_album_gradient);
			}
		}
	}

	private String resolveRedirects(String urlStr) {
		try {
			int redirects = 0;
			while (redirects < 5) {
				java.net.URL url = new java.net.URL(urlStr);
				java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
				conn.setInstanceFollowRedirects(false);
				conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				int status = conn.getResponseCode();
				if (status == java.net.HttpURLConnection.HTTP_MOVED_TEMP
						|| status == java.net.HttpURLConnection.HTTP_MOVED_PERM
						|| status == 307
						|| status == 308) {
					String newUrl = conn.getHeaderField("Location");
					if (newUrl != null) {
						urlStr = newUrl;
						redirects++;
						conn.disconnect();
						continue;
					}
				}
				conn.disconnect();
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return urlStr;
	}

	private java.util.List<OnlineTrack> loadOnlineTracksFromAssets() {
		java.util.List<OnlineTrack> tracks = new java.util.ArrayList<>();
		try {
			java.io.InputStream is = getAssets().open("online_music_catalog.json");
			int size = is.available();
			byte[] buffer = new byte[size];
			is.read(buffer);
			is.close();
			String json = new String(buffer, "UTF-8");
			org.json.JSONArray array = new org.json.JSONArray(json);
			for (int i = 0; i < array.length(); i++) {
				org.json.JSONObject obj = array.getJSONObject(i);
				OnlineTrack t = new OnlineTrack();
				t.id = obj.getString("id");
				t.title = obj.getString("title");
				t.artist = obj.getString("artist");
				t.category = obj.getString("category");
				t.url = obj.getString("url");
				tracks.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tracks;
	}

	private void showOnlineMusicDialog() {
		final java.util.List<OnlineTrack> allTracks = loadOnlineTracksFromAssets();
		final java.util.List<OnlineTrack> filteredTracks = new java.util.ArrayList<>(allTracks);

		final android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
		dialog.setContentView(R.layout.dialog_online_music);
		dialog_online_music_instance = dialog;

		final androidx.recyclerview.widget.RecyclerView rv = (androidx.recyclerview.widget.RecyclerView) dialog.findViewById(R.id.recycler_online_music);
		rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
		
		final OnlineTrackAdapter adapter = new OnlineTrackAdapter(filteredTracks);
		rv.setAdapter(adapter);

		dialog.findViewById(R.id.btn_close_dialog).setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(android.view.View v) {
				adapter.stopPlaying();
				dialog.dismiss();
			}
		});

		dialog.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(android.content.DialogInterface dialogInterface) {
				adapter.stopPlaying();
			}
		});

		final EditText etSearch = (EditText) dialog.findViewById(R.id.et_music_search);
		
		final TextView btnAll = (TextView) dialog.findViewById(R.id.tab_cat_all);
		final TextView btnVlog = (TextView) dialog.findViewById(R.id.tab_cat_vlog);
		final TextView btnLofi = (TextView) dialog.findViewById(R.id.tab_cat_lofi);
		final TextView btnAcoustic = (TextView) dialog.findViewById(R.id.tab_cat_acoustic);
		final TextView btnCinematic = (TextView) dialog.findViewById(R.id.tab_cat_cinematic);
		final TextView btnUpbeat = (TextView) dialog.findViewById(R.id.tab_cat_upbeat);

		final String[] activeCategory = {"Tất cả"};
		final TextView[] categoryButtons = {btnAll, btnVlog, btnLofi, btnAcoustic, btnCinematic, btnUpbeat};
		final String[] categoryNames = {"Tất cả", "Vlog", "Lofi", "Acoustic", "Cinematic", "Upbeat"};

		final Runnable filterList = new Runnable() {
			@Override
			public void run() {
				String query = etSearch.getText().toString().trim().toLowerCase();
				String cat = activeCategory[0];
				
				filteredTracks.clear();
				for (OnlineTrack t : allTracks) {
					boolean matchesQuery = query.isEmpty() || t.title.toLowerCase().contains(query) || t.artist.toLowerCase().contains(query);
					boolean matchesCat = "Tất cả".equals(cat) || t.category.equalsIgnoreCase(cat);
					if (matchesQuery && matchesCat) {
						filteredTracks.add(t);
					}
				}
				adapter.notifyDataSetChanged();
			}
		};

		for (int i = 0; i < categoryButtons.length; i++) {
			final int index = i;
			categoryButtons[i].setOnClickListener(new android.view.View.OnClickListener() {
				@Override
				public void onClick(android.view.View v) {
					activeCategory[0] = categoryNames[index];
					
					int activeColor = getResources().getColor(R.color.colorAccent);
					int activeTextColor = Color.parseColor("#121216");
					int normalColor = Color.parseColor("#16FFFFFF");
					int normalTextColor = Color.parseColor("#E0E0E6");

					for (int k = 0; k < categoryButtons.length; k++) {
						if (k == index) {
							categoryButtons[k].setBackgroundTintList(android.content.res.ColorStateList.valueOf(activeColor));
							categoryButtons[k].setTextColor(activeTextColor);
							categoryButtons[k].setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
						} else {
							categoryButtons[k].setBackgroundTintList(android.content.res.ColorStateList.valueOf(normalColor));
							categoryButtons[k].setTextColor(normalTextColor);
							categoryButtons[k].setTypeface(android.graphics.Typeface.DEFAULT);
						}
					}
					
					adapter.stopPlaying();
					filterList.run();
				}
			});
		}

		etSearch.addTextChangedListener(new android.text.TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				adapter.stopPlaying();
				filterList.run();
			}
			@Override
			public void afterTextChanged(android.text.Editable s) {}
		});

		dialog.show();
	}

	private void downloadAndApplyOnlineTrack(final OnlineTrack track) {
		final String name = track.title;
		final String urlStr = track.url;
		final String localName = "online_track_" + urlStr.substring(urlStr.lastIndexOf('/') + 1);
		final java.io.File cachedFile = new java.io.File(getCacheDir(), localName);

		// Check if we already have the preview cache
		String previewLocalName = "online_track_preview_" + track.id + ".mp3";
		final java.io.File previewCacheFile = new java.io.File(getCacheDir(), previewLocalName);

		if (previewCacheFile.exists()) {
			try {
				copyFile(previewCacheFile, cachedFile);
				Toast.makeText(EditActivity.this, "Đã chọn nhạc thành công!", Toast.LENGTH_SHORT).show();
				selectAudioTrack(cachedFile.getAbsolutePath());
				return;
			} catch (Exception e) {
				e.printStackTrace();
				// Fallback to normal download if copy fails
			}
		}

		final android.app.ProgressDialog progress = new android.app.ProgressDialog(this, android.app.ProgressDialog.THEME_DEVICE_DEFAULT_DARK);
		progress.setTitle("Tải nhạc trực tuyến");
		progress.setMessage("Đang tải: " + name + "...");
		progress.setProgressStyle(android.app.ProgressDialog.STYLE_HORIZONTAL);
		progress.setIndeterminate(false);
		progress.setMax(100);
		progress.setCancelable(false);
		progress.show();

		new Thread(new Runnable() {
			@Override
			public void run() {
				java.io.InputStream input = null;
				java.io.OutputStream output = null;
				java.net.HttpURLConnection connection = null;
				try {
					final String resolvedUrl = resolveRedirects(urlStr);
					java.net.URL url = new java.net.URL(resolvedUrl);
					connection = (java.net.HttpURLConnection) url.openConnection();
					connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");
					connection.setConnectTimeout(10000);
					connection.setReadTimeout(10000);
					connection.connect();

					if (connection.getResponseCode() != java.net.HttpURLConnection.HTTP_OK) {
						throw new Exception("Server HTTP " + connection.getResponseCode());
					}

					int fileLength = connection.getContentLength();
					input = connection.getInputStream();
					output = new java.io.FileOutputStream(cachedFile);

					byte[] data = new byte[4096];
					long total = 0;
					int count;
					while ((count = input.read(data)) != -1) {
						total += count;
						if (fileLength > 0) {
							final int percent = (int) (total * 100 / fileLength);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									progress.setProgress(percent);
								}
							});
						}
						output.write(data, 0, count);
					}

					output.flush();
					output.close();
					input.close();

					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							progress.dismiss();
							Toast.makeText(EditActivity.this, "Đã tải thành công!", Toast.LENGTH_SHORT).show();
							selectAudioTrack(cachedFile.getAbsolutePath());
						}
					});

				} catch (final Exception e) {
					e.printStackTrace();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							progress.dismiss();
							Toast.makeText(EditActivity.this, "Lỗi tải nhạc: " + e.getMessage(), Toast.LENGTH_LONG).show();
						}
					});
				} finally {
					try {
						if (output != null) output.close();
						if (input != null) input.close();
					} catch (Exception ignored) {}
					if (connection != null) connection.disconnect();
				}
			}
		}).start();
	}

	private void copyFile(java.io.File src, java.io.File dst) throws java.io.IOException {
		java.io.InputStream in = new java.io.FileInputStream(src);
		try {
			java.io.OutputStream out = new java.io.FileOutputStream(dst);
			try {
				byte[] buf = new byte[1024];
				int len;
				while ((len = in.read(buf)) > 0) {
					out.write(buf, 0, len);
				}
			} finally {
				out.close();
			}
		} finally {
			in.close();
		}
	}
}
