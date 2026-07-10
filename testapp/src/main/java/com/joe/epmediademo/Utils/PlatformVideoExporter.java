package com.joe.epmediademo.Utils;

import android.media.MediaExtractor;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
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
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					export(inputPath, outputPath, trimStartSec, trimEndSec, listener);
					listener.onSuccess();
				} catch (Exception e) {
					listener.onFailure(e);
				}
			}
		}).start();
	}

	private static void export(String inputPath, String outputPath, float trimStartSec, float trimEndSec,
							   Listener listener) throws Exception {
		File outputFile = new File(outputPath);
		File parent = outputFile.getParentFile();
		if (parent != null && !parent.exists() && !parent.mkdirs()) {
			throw new IllegalStateException("Could not create export directory: " + parent);
		}
		if (outputFile.exists() && !outputFile.delete()) {
			throw new IllegalStateException("Could not replace existing export: " + outputPath);
		}

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
