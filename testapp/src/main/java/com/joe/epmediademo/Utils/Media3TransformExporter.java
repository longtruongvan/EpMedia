package com.joe.epmediademo.Utils;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import androidx.annotation.OptIn;
import androidx.media3.common.Effect;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.effect.Brightness;
import androidx.media3.effect.Contrast;
import androidx.media3.effect.OverlayEffect;
import androidx.media3.effect.OverlaySettings;
import androidx.media3.effect.Presentation;
import androidx.media3.effect.RgbAdjustment;
import androidx.media3.effect.ScaleAndRotateTransformation;
import androidx.media3.effect.TextOverlay;
import androidx.media3.effect.TextureOverlay;
import androidx.media3.transformer.Composition;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.EditedMediaItemSequence;
import androidx.media3.transformer.Effects;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.ProgressHolder;
import androidx.media3.transformer.Transformer;

import com.google.common.collect.ImmutableList;
import com.joe.epmediademo.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class Media3TransformExporter {
	private static final int PROGRESS_POLL_MS = 250;

	public interface Listener {
		void onProgress(float progress);
		void onSuccess();
		void onFailure(Exception exception);
	}

	public static final class Config {
		public List<String> inputPaths = Collections.emptyList();
		public String outputPath;
		public float trimStartSec;
		public float trimEndSec;
		public int outputHeight;
		public int outputFps;
		public int cropPreset;
		public int rotationDegrees;
		public boolean mirror;
		public boolean enhance;
		public String subtitleText;
		public float subtitleXPercent = 50f;
		public float subtitleYPercent = 85f;
		public float subtitleScale = 1f;
		public String stickerText;
		public int filterId;
	}

	private Media3TransformExporter() {
	}

	@OptIn(markerClass = UnstableApi.class)
	public static void exportAsync(final Context context, final Config config, final Listener listener) {
		final HandlerThread thread = new HandlerThread("EpMediaTransformExport");
		thread.start();
		final Handler handler = new Handler(thread.getLooper());
		final AtomicBoolean finished = new AtomicBoolean(false);

		handler.post(new Runnable() {
			@Override
			public void run() {
				try {
					validateConfig(config);
					prepareOutputFile(config.outputPath);

					final Composition composition = buildComposition(config);
					final Transformer transformer = new Transformer.Builder(context.getApplicationContext())
							.setLooper(thread.getLooper())
							.setVideoMimeType(MimeTypes.VIDEO_H264)
							.setAudioMimeType(MimeTypes.AUDIO_AAC)
							.addListener(new Transformer.Listener() {
								@Override
								public void onCompleted(Composition composition, ExportResult exportResult) {
									if (finished.compareAndSet(false, true)) {
										listener.onProgress(1f);
										listener.onSuccess();
										thread.quitSafely();
									}
								}

								@Override
								public void onError(Composition composition, ExportResult exportResult, ExportException exportException) {
									failOnce(finished, thread, listener, config.outputPath, exportException);
								}
							})
							.build();

					startProgressPolling(handler, transformer, listener, finished);
					transformer.start(composition, config.outputPath);
				} catch (Exception exception) {
					failOnce(finished, thread, listener, config.outputPath, exception);
				}
			}
		});
	}

	private static void validateConfig(Config config) {
		if (config == null) {
			throw new IllegalArgumentException("Missing export config");
		}
		if (config.inputPaths == null || config.inputPaths.isEmpty()) {
			throw new IllegalArgumentException("No input videos selected");
		}
		if (config.outputPath == null || config.outputPath.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing output path");
		}
		for (String inputPath : config.inputPaths) {
			if (inputPath == null || !new File(inputPath).exists()) {
				throw new IllegalArgumentException("Input video does not exist: " + inputPath);
			}
		}
		if (config.outputHeight <= 0) {
			throw new IllegalArgumentException("Invalid output height");
		}
		if (config.outputFps <= 0) {
			throw new IllegalArgumentException("Invalid output FPS");
		}
	}

	@OptIn(markerClass = UnstableApi.class)
	private static Composition buildComposition(Config config) {
		List<Effect> videoEffects = buildVideoEffects(config);
		Effects effects = new Effects(Collections.<AudioProcessor>emptyList(), videoEffects);
		List<EditedMediaItem> editedItems = new ArrayList<>();

		for (int i = 0; i < config.inputPaths.size(); i++) {
			MediaItem.Builder mediaBuilder = new MediaItem.Builder()
					.setUri(Uri.fromFile(new File(config.inputPaths.get(i))));
			if (config.inputPaths.size() == 1 && config.trimEndSec > config.trimStartSec) {
				mediaBuilder.setClippingConfiguration(new MediaItem.ClippingConfiguration.Builder()
						.setStartPositionMs(Math.max(0L, (long) (config.trimStartSec * 1000L)))
						.setEndPositionMs(Math.max(1L, (long) (config.trimEndSec * 1000L)))
						.build());
			}

			editedItems.add(new EditedMediaItem.Builder(mediaBuilder.build())
					.setFrameRate(config.outputFps)
					.setEffects(effects)
					.build());
		}

		EditedMediaItemSequence sequence = new EditedMediaItemSequence(editedItems);
		return new Composition.Builder(sequence)
				.setTransmuxAudio(false)
				.setTransmuxVideo(false)
				.build();
	}

	@OptIn(markerClass = UnstableApi.class)
	private static List<Effect> buildVideoEffects(Config config) {
		List<Effect> effects = new ArrayList<>();
		if (config.mirror || config.rotationDegrees != 0) {
			ScaleAndRotateTransformation.Builder transformBuilder = new ScaleAndRotateTransformation.Builder()
					.setRotationDegrees(config.rotationDegrees);
			if (config.mirror) {
				transformBuilder.setScale(-1f, 1f);
			}
			effects.add(transformBuilder.build());
		}

		float aspectRatio = getCropAspectRatio(config.cropPreset);
		if (aspectRatio > 0f) {
			effects.add(Presentation.createForAspectRatio(aspectRatio, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP));
		}
		effects.add(Presentation.createForHeight(config.outputHeight));

		if (config.enhance) {
			effects.add(new Contrast(0.18f));
		}
		addFilterEffects(effects, config.filterId);
		addOverlayEffects(effects, config);
		return effects;
	}

	@OptIn(markerClass = UnstableApi.class)
	private static void addFilterEffects(List<Effect> effects, int filterId) {
		if (filterId == R.id.btn_filter_warm) {
			effects.add(new RgbAdjustment.Builder()
					.setRedScale(1.08f)
					.setGreenScale(1.02f)
					.setBlueScale(0.92f)
					.build());
			effects.add(new Brightness(0.03f));
		} else if (filterId == R.id.btn_filter_cool) {
			effects.add(new RgbAdjustment.Builder()
					.setRedScale(0.93f)
					.setGreenScale(1.02f)
					.setBlueScale(1.10f)
					.build());
		} else if (filterId == R.id.btn_filter_vintage) {
			effects.add(new RgbAdjustment.Builder()
					.setRedScale(1.07f)
					.setGreenScale(0.98f)
					.setBlueScale(0.86f)
					.build());
			effects.add(new Contrast(0.12f));
			effects.add(new Brightness(-0.02f));
		}
	}

	@OptIn(markerClass = UnstableApi.class)
	private static void addOverlayEffects(List<Effect> effects, Config config) {
		ImmutableList.Builder<TextureOverlay> overlays = ImmutableList.builder();
		if (hasText(config.subtitleText)) {
			overlays.add(TextOverlay.createStaticTextOverlay(
					buildStyledText(config.subtitleText, Math.round(48f * clamp(config.subtitleScale, 0.5f, 5f))),
					new OverlaySettings.Builder()
							.setBackgroundFrameAnchor(toAnchor(config.subtitleXPercent), toYAnchor(config.subtitleYPercent))
							.setOverlayFrameAnchor(0f, 0f)
							.build()));
		}
		if (hasText(config.stickerText)) {
			overlays.add(TextOverlay.createStaticTextOverlay(
					buildStyledText(config.stickerText, 96),
					new OverlaySettings.Builder()
							.setBackgroundFrameAnchor(0.72f, -0.62f)
							.setOverlayFrameAnchor(0f, 0f)
							.build()));
		}

		ImmutableList<TextureOverlay> overlayList = overlays.build();
		if (!overlayList.isEmpty()) {
			effects.add(new OverlayEffect(overlayList));
		}
	}

	private static SpannableString buildStyledText(String value, int textSizePx) {
		String text = value == null ? "" : value.trim();
		SpannableString spannable = new SpannableString(text);
		spannable.setSpan(new AbsoluteSizeSpan(Math.max(24, textSizePx)), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return spannable;
	}

	private static boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

	private static float toAnchor(float percent) {
		return clamp(percent, 0f, 100f) / 50f - 1f;
	}

	private static float toYAnchor(float percent) {
		return 1f - clamp(percent, 0f, 100f) / 50f;
	}

	private static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}

	private static float getCropAspectRatio(int cropPreset) {
		switch (cropPreset) {
			case 1:
				return 1f;
			case 2:
				return 9f / 16f;
			case 3:
				return 16f / 9f;
			case 4:
				return 4f / 5f;
			case 0:
			default:
				return 0f;
		}
	}

	@OptIn(markerClass = UnstableApi.class)
	private static void startProgressPolling(final Handler handler, final Transformer transformer,
											 final Listener listener, final AtomicBoolean finished) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				if (finished.get()) {
					return;
				}
				ProgressHolder progressHolder = new ProgressHolder();
				int state = transformer.getProgress(progressHolder);
				if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
					listener.onProgress(Math.min(0.99f, Math.max(0f, progressHolder.progress / 100f)));
				}
				handler.postDelayed(this, PROGRESS_POLL_MS);
			}
		}, PROGRESS_POLL_MS);
	}

	private static void failOnce(AtomicBoolean finished, HandlerThread thread, Listener listener,
								 String outputPath, Exception exception) {
		if (finished.compareAndSet(false, true)) {
			deleteFileSilently(outputPath);
			listener.onFailure(exception);
			thread.quitSafely();
		}
	}

	private static void prepareOutputFile(String outputPath) {
		File outputFile = new File(outputPath);
		File parent = outputFile.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs()) {
			throw new IllegalStateException("Could not create export directory: " + parent);
		}
		if (outputFile.exists() && !outputFile.delete()) {
			throw new IllegalStateException("Could not replace existing export: " + outputPath);
		}
	}

	private static void deleteFileSilently(String path) {
		if (path == null) {
			return;
		}
		try {
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception ignored) {
		}
	}
}
