package com.joe.epmediademo.Utils;

import android.media.MediaExtractor;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PlatformVideoExporter {
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

	public interface Listener {
		void onProgress(float progress);
		void onSuccess();
		void onFailure(Exception exception);
	}

	private PlatformVideoExporter() {
	}

	public static void exportAsync(final String inputPath, final String outputPath, final float trimStartSec,
								   final float trimEndSec, final Listener listener) {
		ArrayList<String> paths = new ArrayList<>();
		paths.add(inputPath);
		exportAsync(paths, outputPath, trimStartSec, trimEndSec, listener);
	}

	public static void exportAsync(final List<String> inputPaths, final String outputPath, final float trimStartSec,
								   final float trimEndSec, final Listener listener) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (inputPaths == null || inputPaths.isEmpty()) {
						throw new IllegalArgumentException("No input videos selected");
					}
					if (inputPaths.size() == 1) {
						export(inputPaths.get(0), outputPath, trimStartSec, trimEndSec, listener);
					} else {
						exportSequence(inputPaths, outputPath, listener);
					}
					listener.onSuccess();
				} catch (Exception e) {
					listener.onFailure(e);
				}
			}
		}).start();
	}

	private static void exportSequence(List<String> inputPaths, String outputPath, Listener listener) throws Exception {
		prepareOutputFile(outputPath);

		long totalDurationUs = 0L;
		for (String inputPath : inputPaths) {
			totalDurationUs += Math.max(1L, readDurationUs(inputPath));
		}
		totalDurationUs = Math.max(1L, totalDurationUs);

		MediaMuxer muxer = null;
		boolean muxerStarted = false;
		try {
			TrackSetup setup = buildTrackSetup(inputPaths.get(0));
			muxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
			applyOrientation(inputPaths.get(0), muxer);
			for (String trackType : setup.trackFormatsByType.keySet()) {
				setup.muxerTrackByType.put(trackType, muxer.addTrack(setup.trackFormatsByType.get(trackType)));
			}

			ByteBuffer buffer = ByteBuffer.allocateDirect(setup.maxBufferSize);
			MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
			muxer.start();
			muxerStarted = true;

			long clipOffsetUs = 0L;
			for (String inputPath : inputPaths) {
				long writtenDurationUs = appendClip(inputPath, muxer, setup, buffer, bufferInfo, clipOffsetUs, totalDurationUs, listener);
				clipOffsetUs += Math.max(1L, writtenDurationUs);
			}
			listener.onProgress(1f);
		} finally {
			if (muxer != null) {
				try {
					if (muxerStarted) {
						muxer.stop();
					}
				} finally {
					muxer.release();
				}
			}
		}
	}

	private static TrackSetup buildTrackSetup(String inputPath) throws Exception {
		MediaExtractor extractor = new MediaExtractor();
		try {
			extractor.setDataSource(inputPath);
			TrackSetup setup = new TrackSetup();
			for (int i = 0; i < extractor.getTrackCount(); i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String trackType = getSupportedTrackType(format);
				if (trackType != null && !setup.trackFormatsByType.containsKey(trackType)) {
					setup.trackFormatsByType.put(trackType, format);
					if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
						setup.maxBufferSize = Math.max(setup.maxBufferSize, format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
					}
				}
			}
			if (setup.trackFormatsByType.isEmpty() || !setup.trackFormatsByType.containsKey("video")) {
				throw new IllegalStateException("No video track found in " + inputPath);
			}
			return setup;
		} finally {
			extractor.release();
		}
	}

	private static long appendClip(String inputPath, MediaMuxer muxer, TrackSetup setup, ByteBuffer buffer,
								   MediaCodec.BufferInfo bufferInfo, long clipOffsetUs, long totalDurationUs,
								   Listener listener) throws Exception {
		MediaExtractor extractor = new MediaExtractor();
		try {
			extractor.setDataSource(inputPath);
			Map<Integer, String> selectedTracks = new HashMap<>();
			for (int i = 0; i < extractor.getTrackCount(); i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String trackType = getSupportedTrackType(format);
				if (trackType != null && setup.muxerTrackByType.containsKey(trackType)) {
					validateCompatibleTrack(inputPath, setup.trackFormatsByType.get(trackType), format, trackType);
					extractor.selectTrack(i);
					selectedTracks.put(i, trackType);
					if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
						setup.maxBufferSize = Math.max(setup.maxBufferSize, format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
					}
				}
			}
			if (!selectedTracks.containsValue("video")) {
				throw new IllegalStateException("No compatible video track found in " + inputPath);
			}

			long firstSampleUs = -1L;
			long lastSampleUs = 0L;
			while (true) {
				int trackIndex = extractor.getSampleTrackIndex();
				if (trackIndex < 0) {
					break;
				}
				String trackType = selectedTracks.get(trackIndex);
				if (trackType == null) {
					extractor.advance();
					continue;
				}

				buffer.clear();
				int sampleSize = extractor.readSampleData(buffer, 0);
				if (sampleSize < 0) {
					break;
				}
				long sampleTimeUs = extractor.getSampleTime();
				if (firstSampleUs < 0L) {
					firstSampleUs = sampleTimeUs;
				}
				long normalizedSampleUs = Math.max(0L, sampleTimeUs - firstSampleUs);
				lastSampleUs = Math.max(lastSampleUs, normalizedSampleUs);
				bufferInfo.set(0, sampleSize, clipOffsetUs + normalizedSampleUs, toCodecBufferFlags(extractor.getSampleFlags()));
				muxer.writeSampleData(setup.muxerTrackByType.get(trackType), buffer, bufferInfo);
				listener.onProgress(Math.min(0.99f, (float) (clipOffsetUs + normalizedSampleUs) / totalDurationUs));
				extractor.advance();
			}

			long metadataDurationUs = readDurationUs(inputPath);
			return Math.max(lastSampleUs + 1_000L, metadataDurationUs);
		} finally {
			extractor.release();
		}
	}

	private static String getSupportedTrackType(MediaFormat format) {
		if (format == null || !format.containsKey(MediaFormat.KEY_MIME)) {
			return null;
		}
		String mime = format.getString(MediaFormat.KEY_MIME);
		if (mime == null) {
			return null;
		}
		if (mime.startsWith("video/")) {
			return "video";
		}
		if (mime.startsWith("audio/")) {
			return "audio";
		}
		return null;
	}

	private static void validateCompatibleTrack(String inputPath, MediaFormat expected, MediaFormat actual, String trackType) {
		String expectedMime = expected.getString(MediaFormat.KEY_MIME);
		String actualMime = actual.getString(MediaFormat.KEY_MIME);
		if (expectedMime == null || !expectedMime.equals(actualMime)) {
			throw new IllegalArgumentException("Incompatible " + trackType + " codec in " + inputPath);
		}
		if ("video".equals(trackType)) {
			requireSameInteger(inputPath, expected, actual, MediaFormat.KEY_WIDTH, trackType);
			requireSameInteger(inputPath, expected, actual, MediaFormat.KEY_HEIGHT, trackType);
		}
		if ("audio".equals(trackType)) {
			requireSameInteger(inputPath, expected, actual, MediaFormat.KEY_SAMPLE_RATE, trackType);
			requireSameInteger(inputPath, expected, actual, MediaFormat.KEY_CHANNEL_COUNT, trackType);
		}
	}

	private static void requireSameInteger(String inputPath, MediaFormat expected, MediaFormat actual, String key, String trackType) {
		if (expected.containsKey(key) && actual.containsKey(key)
				&& expected.getInteger(key) != actual.getInteger(key)) {
			throw new IllegalArgumentException("Incompatible " + trackType + " " + key + " in " + inputPath);
		}
	}

	private static void export(String inputPath, String outputPath, float trimStartSec, float trimEndSec,
							   Listener listener) throws Exception {
		prepareOutputFile(outputPath);

		long inputDurationUs = readDurationUs(inputPath);
		long startUs = Math.max(0L, (long) (trimStartSec * 1_000_000L));
		long requestedEndUs = trimEndSec > trimStartSec ? (long) (trimEndSec * 1_000_000L) : inputDurationUs;
		long endUs = inputDurationUs > 0 ? Math.min(requestedEndUs, inputDurationUs) : requestedEndUs;
		long progressDurationUs = Math.max(1L, endUs - startUs);

		MediaExtractor extractor = new MediaExtractor();
		MediaMuxer muxer = null;
		try {
			extractor.setDataSource(inputPath);
			muxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
			applyOrientation(inputPath, muxer);

			Map<Integer, Integer> trackMap = new HashMap<>();
			int maxBufferSize = DEFAULT_BUFFER_SIZE;
			for (int i = 0; i < extractor.getTrackCount(); i++) {
				MediaFormat format = extractor.getTrackFormat(i);
				String mime = format.containsKey(MediaFormat.KEY_MIME)
						? format.getString(MediaFormat.KEY_MIME)
						: "";
				if (mime != null && (mime.startsWith("video/") || mime.startsWith("audio/"))) {
					extractor.selectTrack(i);
					trackMap.put(i, muxer.addTrack(format));
					if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
						maxBufferSize = Math.max(maxBufferSize, format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE));
					}
				}
			}
			if (trackMap.isEmpty()) {
				throw new IllegalStateException("No audio or video tracks found in " + inputPath);
			}

			ByteBuffer buffer = ByteBuffer.allocateDirect(maxBufferSize);
			android.media.MediaCodec.BufferInfo bufferInfo = new android.media.MediaCodec.BufferInfo();
			muxer.start();
			if (startUs > 0) {
				extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC);
			}

			while (true) {
				int trackIndex = extractor.getSampleTrackIndex();
				if (trackIndex < 0) {
					break;
				}
				Integer muxerTrackIndex = trackMap.get(trackIndex);
				long sampleTimeUs = extractor.getSampleTime();
				if (endUs > 0 && sampleTimeUs > endUs) {
					break;
				}
				if (muxerTrackIndex == null || sampleTimeUs < startUs) {
					extractor.advance();
					continue;
				}

				buffer.clear();
				int sampleSize = extractor.readSampleData(buffer, 0);
				if (sampleSize < 0) {
					break;
				}
				bufferInfo.set(0, sampleSize, sampleTimeUs - startUs, toCodecBufferFlags(extractor.getSampleFlags()));
				muxer.writeSampleData(muxerTrackIndex, buffer, bufferInfo);
				listener.onProgress(Math.min(1f, (float) (sampleTimeUs - startUs) / progressDurationUs));
				extractor.advance();
			}
		} finally {
			extractor.release();
			if (muxer != null) {
				try {
					muxer.stop();
				} finally {
					muxer.release();
				}
			}
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

	private static class TrackSetup {
		int maxBufferSize = DEFAULT_BUFFER_SIZE;
		final Map<String, MediaFormat> trackFormatsByType = new HashMap<>();
		final Map<String, Integer> muxerTrackByType = new HashMap<>();
	}

	private static long readDurationUs(String inputPath) {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(inputPath);
			String durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			return durationMs == null ? 0L : Long.parseLong(durationMs) * 1000L;
		} catch (Exception ignored) {
			return 0L;
		} finally {
			try {
				retriever.release();
			} catch (Exception ignored) {
			}
		}
	}

	private static int toCodecBufferFlags(int extractorFlags) {
		int codecFlags = 0;
		if ((extractorFlags & MediaExtractor.SAMPLE_FLAG_SYNC) != 0) {
			codecFlags |= MediaCodec.BUFFER_FLAG_KEY_FRAME;
		}
		if ((extractorFlags & MediaExtractor.SAMPLE_FLAG_PARTIAL_FRAME) != 0) {
			codecFlags |= MediaCodec.BUFFER_FLAG_PARTIAL_FRAME;
		}
		return codecFlags;
	}

	private static void applyOrientation(String inputPath, MediaMuxer muxer) {
		MediaMetadataRetriever retriever = new MediaMetadataRetriever();
		try {
			retriever.setDataSource(inputPath);
			String rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
			if (rotation != null) {
				int degrees = Integer.parseInt(rotation);
				if (degrees == 90 || degrees == 180 || degrees == 270) {
					muxer.setOrientationHint(degrees);
				}
			}
		} catch (Exception ignored) {
		} finally {
			try {
				retriever.release();
			} catch (Exception ignored) {
			}
		}
	}
}
