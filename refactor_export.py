import re

file_path = "testapp/src/main/java/com/joe/epmediademo/Activity/ExportActivity.java"
with open(file_path, "r") as f:
    content = f.read()

# 1. Intent inputs
content = content.replace("private String videoUrl;", "private java.util.ArrayList<String> videoUrls;")
content = content.replace('videoUrl = intent.getStringExtra("VIDEO_PATH");', 'videoUrls = intent.getStringArrayListExtra("VIDEO_PATHS");')

# 2. EpVideo creation
# Find: EpVideo epVideo = new EpVideo(videoUrl);
# Replace with List loop

ep_video_logic_old = """		EpVideo epVideo = new EpVideo(videoUrl);

		// Apply trim clip
		epVideo.clip(trimStartSec, trimEndSec - trimStartSec);

		// Apply crop presets"""

ep_video_logic_new = """		java.util.List<EpVideo> epVideos = new java.util.ArrayList<>();
		if (videoUrls == null || videoUrls.isEmpty()) return;
		
		for (int i = 0; i < videoUrls.size(); i++) {
			EpVideo epVideo = new EpVideo(videoUrls.get(i));
			
			// Apply trim only to the first clip for simplicity
			if (i == 0 && (trimStartSec > 0 || trimEndSec > 0)) {
				epVideo.clip(trimStartSec, trimEndSec - trimStartSec);
			}

		// Apply crop presets"""

content = content.replace(ep_video_logic_old, ep_video_logic_new)

# 3. Replace all epVideo. with epVideo. in a loop?
# Since EpVideo has many addFilter calls, we need to wrap the whole configuration block in the loop.
# It's better to just keep epVideo as the variable inside the loop, and then add it to epVideos.

# Let's find the end of the configuration block where EpEditor.exec is called.
exec_old = """		EpEditor.exec(epVideo, opt, new OnEditorListener() {"""
exec_new = """			epVideos.add(epVideo);
		} // end for loop

		if (epVideos.size() == 1) {
			EpEditor.exec(epVideos.get(0), opt, new OnEditorListener() {
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
							updateProgress((int) (v * 100));
						}
					});
				}
			});
		} else {
			EpEditor.merge(epVideos, opt, new OnEditorListener() {
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
							updateProgress((int) (v * 100));
						}
					});
				}
			});
		}"""

# Wait, we need to replace the exact EpEditor.exec block.
# Let's do it using Regex to replace the exec call.
content = re.sub(r'EpEditor\.exec\(epVideo, opt, new OnEditorListener\(\) \{.*?\}\);', exec_new, content, flags=re.DOTALL)

with open(file_path, "w") as f:
    f.write(content)

print("Done")
