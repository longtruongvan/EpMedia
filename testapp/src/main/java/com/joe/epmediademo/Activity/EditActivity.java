package com.joe.epmediademo.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.slider.RangeSlider;
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
	private CheckBox cb_clip, cb_crop, cb_rotation, cb_mirror, cb_text;
	private EditText et_clip_start, et_clip_end, et_crop_x, et_crop_y, et_crop_w, et_crop_h, et_rotation, et_text_x, et_text_y, et_text;
	private TextView tv_file;
	private Button bt_file, bt_exec;
	private String videoUrl;
	private ProgressDialog mProgressDialog;

	// CapCut Interactive Additions
	private VideoView video_view;
	private View video_container;
	private TextView tv_subtitle_preview;
	private ImageView iv_play_pause;
	private View layout_timeline;
	private LinearLayout timeline_thumbnails;
	private RangeSlider range_slider;
	private TextView tv_trim_start, tv_trim_duration, tv_trim_end;

	private Handler playHandler = new Handler();
	private Runnable playRunnable;
	private float trimStartSec = 0f;
	private float trimEndSec = 0f;
	private boolean isUserSeeking = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		initView();
		startPlayProgressTracker();
	}

	private void initView() {
		cb_clip = (CheckBox) findViewById(R.id.cb_clip);
		cb_crop = (CheckBox) findViewById(R.id.cb_crop);
		cb_rotation = (CheckBox) findViewById(R.id.cb_rotation);
		cb_mirror = (CheckBox) findViewById(R.id.cb_mirror);
		cb_text = (CheckBox) findViewById(R.id.cb_text);
		et_clip_start = (EditText) findViewById(R.id.et_clip_start);
		et_clip_end = (EditText) findViewById(R.id.et_clip_end);
		et_crop_x = (EditText) findViewById(R.id.et_crop_x);
		et_crop_y = (EditText) findViewById(R.id.et_crop_y);
		et_crop_w = (EditText) findViewById(R.id.et_crop_w);
		et_crop_h = (EditText) findViewById(R.id.et_crop_h);
		et_rotation = (EditText) findViewById(R.id.et_rotation);
		et_text_x = (EditText) findViewById(R.id.et_text_x);
		et_text_y = (EditText) findViewById(R.id.et_text_y);
		et_text = (EditText) findViewById(R.id.et_text);
		tv_file = (TextView) findViewById(R.id.tv_file);
		bt_file = (Button) findViewById(R.id.bt_file);
		bt_exec = (Button) findViewById(R.id.bt_exec);

		// Find capcut interactive views
		video_view = (VideoView) findViewById(R.id.video_view);
		video_container = findViewById(R.id.video_container);
		tv_subtitle_preview = (TextView) findViewById(R.id.tv_subtitle_preview);
		iv_play_pause = (ImageView) findViewById(R.id.iv_play_pause);
		layout_timeline = findViewById(R.id.layout_timeline);
		timeline_thumbnails = (LinearLayout) findViewById(R.id.timeline_thumbnails);
		range_slider = (RangeSlider) findViewById(R.id.range_slider);
		tv_trim_start = (TextView) findViewById(R.id.tv_trim_start);
		tv_trim_duration = (TextView) findViewById(R.id.tv_trim_duration);
		tv_trim_end = (TextView) findViewById(R.id.tv_trim_end);

		bt_file.setOnClickListener(this);
		bt_exec.setOnClickListener(this);

		video_container.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (videoUrl != null) {
					togglePlayPause();
				}
			}
		});

		cb_mirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					cb_rotation.setChecked(true);
				}
				updateVideoTransformations();
			}
		});
		cb_rotation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(!isChecked){
					cb_mirror.setChecked(false);
				}
				updateVideoTransformations();
			}
		});

		et_rotation.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				updateVideoTransformations();
			}
		});

		cb_text.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				updateSubtitlePreview();
			}
		});

		TextWatcher subtitleWatcher = new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				updateSubtitlePreview();
			}
		};
		et_text.addTextChangedListener(subtitleWatcher);
		et_text_x.addTextChangedListener(subtitleWatcher);
		et_text_y.addTextChangedListener(subtitleWatcher);

		range_slider.addOnChangeListener(new RangeSlider.OnChangeListener() {
			@Override
			public void onValueChange(RangeSlider slider, float value, boolean fromUser) {
				List<Float> values = slider.getValues();
				if (values.size() >= 2) {
					trimStartSec = values.get(0);
					trimEndSec = values.get(1);
					
					et_clip_start.setText(String.format(java.util.Locale.US, "%.1f", trimStartSec));
					et_clip_end.setText(String.format(java.util.Locale.US, "%.1f", trimEndSec));
					
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

		et_clip_start.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				if (getCurrentFocus() == et_clip_start && !isUserSeeking) {
					try {
						float val = Float.parseFloat(s.toString().trim());
						if (val >= 0 && val < trimEndSec) {
							trimStartSec = val;
							range_slider.setValues(Arrays.asList(trimStartSec, trimEndSec));
							video_view.seekTo((int) (trimStartSec * 1000));
							updateTrimLabels();
						}
					} catch (Exception ignored) {}
				}
			}
		});

		et_clip_end.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void afterTextChanged(Editable s) {
				if (getCurrentFocus() == et_clip_end && !isUserSeeking) {
					try {
						float val = Float.parseFloat(s.toString().trim());
						if (val > trimStartSec && val <= range_slider.getValueTo()) {
							trimEndSec = val;
							range_slider.setValues(Arrays.asList(trimStartSec, trimEndSec));
							updateTrimLabels();
						}
					} catch (Exception ignored) {}
				}
			}
		});

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMax(100);
		mProgressDialog.setCancelable(false);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setTitle("Processing Video");
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
		video_view.setVideoPath(path);
		video_view.setOnPreparedListener(new android.media.MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(android.media.MediaPlayer mp) {
				int durationMs = video_view.getDuration();
				float durationSec = durationMs / 1000f;
				
				trimStartSec = 0f;
				trimEndSec = durationSec;
				
				et_clip_start.setText("0.0");
				et_clip_end.setText(String.format(java.util.Locale.US, "%.1f", durationSec));
				
				range_slider.setValueFrom(0f);
				range_slider.setValueTo(durationSec);
				range_slider.setValues(Arrays.asList(0f, durationSec));
				
				updateTrimLabels();
				
				layout_timeline.setVisibility(View.VISIBLE);
				video_view.seekTo(1);
				iv_play_pause.setImageResource(android.R.drawable.ic_media_play);
				iv_play_pause.setVisibility(View.VISIBLE);
				
				loadTimelineThumbnails(path);
				cb_clip.setChecked(true);
				updateSubtitlePreview();
				updateVideoTransformations();
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
		tv_trim_start.setText(String.format(java.util.Locale.US, "Start: %.1fs", trimStartSec));
		tv_trim_end.setText(String.format(java.util.Locale.US, "End: %.1fs", trimEndSec));
		tv_trim_duration.setText(String.format(java.util.Locale.US, "Duration: %.1fs", duration));
	}

	private void updateVideoTransformations() {
		if (video_view == null) return;
		
		float rotation = 0f;
		if (cb_rotation.isChecked()) {
			try {
				rotation = Float.parseFloat(et_rotation.getText().toString().trim());
			} catch (Exception ignored) {}
		}
		video_view.setRotation(rotation);
		
		float scaleX = 1f;
		if (cb_mirror.isChecked()) {
			scaleX = -1f;
		}
		video_view.setScaleX(scaleX);
	}

	private void updateSubtitlePreview() {
		if (tv_subtitle_preview == null) return;
		if (cb_text.isChecked()) {
			tv_subtitle_preview.setVisibility(View.VISIBLE);
			tv_subtitle_preview.setText(et_text.getText().toString());
			try {
				float x = Float.parseFloat(et_text_x.getText().toString().trim());
				float y = Float.parseFloat(et_text_y.getText().toString().trim());
				tv_subtitle_preview.setTranslationX(x - 50);
				tv_subtitle_preview.setTranslationY(y - 50);
			} catch (Exception ignored) {}
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
			if(cb_clip.isChecked())
				epVideo.clip(Float.parseFloat(et_clip_start.getText().toString().trim()),Float.parseFloat(et_clip_end.getText().toString().trim()));
			if(cb_crop.isChecked())
				epVideo.crop(Integer.parseInt(et_crop_w.getText().toString().trim()),Integer.parseInt(et_crop_h.getText().toString().trim()),Integer.parseInt(et_crop_x.getText().toString().trim()),Integer.parseInt(et_crop_y.getText().toString().trim()));
			if(cb_rotation.isChecked())
				epVideo.rotation(Integer.parseInt(et_rotation.getText().toString().trim()),cb_mirror.isChecked());
			if(cb_text.isChecked())
				epVideo.addText(Integer.parseInt(et_text_x.getText().toString().trim()),Integer.parseInt(et_text_y.getText().toString().trim()),30,"red",MyApplication.getSavePath() + "msyh.ttf",et_text.getText().toString().trim());
			mProgressDialog.setProgress(0);
			mProgressDialog.show();
			final String outPath = MyApplication.getSavePath() + "out.mp4";
			EpEditor.exec(epVideo, new EpEditor.OutputOption(outPath), new OnEditorListener() {
				@Override
				public void onSuccess() {
					Toast.makeText(EditActivity.this, "Edit Success:"+outPath, Toast.LENGTH_SHORT).show();
					mProgressDialog.dismiss();

					Intent v = new Intent(Intent.ACTION_VIEW);
					v.setDataAndType(Uri.parse(outPath), "video/mp4");
					startActivity(v);
				}

				@Override
				public void onFailure() {
					Toast.makeText(EditActivity.this, "Edit Failed", Toast.LENGTH_SHORT).show();
					mProgressDialog.dismiss();
				}

				@Override
				public void onProgress(float v) {
					mProgressDialog.setProgress((int) (v * 100));
				}
			});
		}else{
			Toast.makeText(this, "Select a video", Toast.LENGTH_SHORT).show();
		}
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
