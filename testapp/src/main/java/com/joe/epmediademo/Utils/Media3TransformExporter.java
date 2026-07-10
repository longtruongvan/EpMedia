package com.joe.epmediademo.Utils;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.OptIn;
import androidx.media3.common.Effect;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.audio.AudioProcessor;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.effect.Contrast;
import androidx.media3.effect.Presentation;
import androidx.media3.effect.ScaleAndRotateTransformation;
import androidx.media3.transformer.Composition;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.EditedMediaItemSequence;
import androidx.media3.transformer.Effects;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.ProgressHolder;
import androidx.media3.transformer.Transformer;

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
		return effects;
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
