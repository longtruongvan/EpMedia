import re

file_path = "testapp/src/main/java/com/joe/epmediademo/Activity/ExportActivity.java"
with open(file_path, "r") as f:
    content = f.read()

# 1. Update variables
content = content.replace("private String videoUrl;", "private java.util.ArrayList<String> videoUrls;")
content = content.replace('videoUrl = intent.getStringExtra("VIDEO_PATH");', 'videoUrls = intent.getStringArrayListExtra("VIDEO_PATHS");')

# 2. Extract the EpVideo configuration block from 'EpVideo epVideo = new EpVideo(videoUrl);' to 'EpEditor.exec(epVideo, opt, new OnEditorListener() {'
match = re.search(r'(EpVideo epVideo = new EpVideo\(videoUrl\);.*?)(EpEditor\.exec\(epVideo, opt, new OnEditorListener\(\) \{)', content, re.DOTALL)
if match:
    config_block = match.group(1)
    
    # We want to replace 'EpVideo epVideo = new EpVideo(videoUrl);' inside config_block
    new_config_block = config_block.replace("EpVideo epVideo = new EpVideo(videoUrl);", "EpVideo epVideo = new EpVideo(videoUrls.get(i));")
    
    # Wrap config_block in a loop
    loop_start = """		java.util.List<EpVideo> epVideos = new java.util.ArrayList<>();
		if (videoUrls == null || videoUrls.isEmpty()) return;
		for (int i = 0; i < videoUrls.size(); i++) {
"""
    # Replace the trim logic to only apply to the first clip for simplicity
    new_config_block = new_config_block.replace(
        "epVideo.clip(trimStartSec, trimEndSec - trimStartSec);",
        "if (i == 0 && (trimStartSec > 0 || trimEndSec > 0)) epVideo.clip(trimStartSec, trimEndSec - trimStartSec);"
    )
    
    loop_end = """
			epVideos.add(epVideo);
		}
"""
    
    full_new_block = loop_start + new_config_block + loop_end
    
    # Now build the new execution logic
    exec_logic = """
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
						updateProgress((int) (v * 100));
					}
				});
			}
		};

		if (epVideos.size() == 1) {
			EpEditor.exec(epVideos.get(0), opt, commonListener);
		} else {
			EpEditor.merge(epVideos, opt, commonListener);
		}
"""
    
    # Let's find the original exec block to replace
    # We replace from EpVideo epVideo = ... to the end of the EpEditor.exec block
    # How to safely find the end of EpEditor.exec block?
    # It looks like:
    # EpEditor.exec(epVideo, opt, new OnEditorListener() {
    #     ...
    #     public void onProgress(final float v) {
    #         runOnUiThread(new Runnable() {
    #             public void run() {
    #                 updateProgress((int) (v * 100));
    #             }
    #         });
    #     }
    # });
    
    regex_to_replace = r'(EpVideo epVideo = new EpVideo\(videoUrl\);.*?)EpEditor\.exec\(epVideo, opt, new OnEditorListener\(\) \{.*?\}\);\s*\}\);'
    # Actually, the block ends with "});\n\t}" -> wait, the onProgress has a runOnUiThread.
    # Let's just find the exact block since we know what it looks like.
    
    target_exec_block = """		EpEditor.exec(epVideo, opt, new OnEditorListener() {
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
		});"""
    
    if config_block in content and target_exec_block in content:
        content = content.replace(config_block, full_new_block)
        content = content.replace(target_exec_block, exec_logic)
        
        with open(file_path, "w") as f:
            f.write(content)
        print("Done")
    else:
        print("Could not find exact blocks to replace")
