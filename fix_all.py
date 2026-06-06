import re

# 1. Fix EditActivity.java
f_edit = "testapp/src/main/java/com/joe/epmediademo/Activity/EditActivity.java"
with open(f_edit, "r") as f:
    edit_content = f.read()

edit_content = edit_content.replace("private ImageView bt_file;", "private ImageView bt_file;\n\tprivate ImageView btn_add_video;")
edit_content = edit_content.replace("bt_file = (ImageView) findViewById(R.id.bt_file);", "bt_file = (ImageView) findViewById(R.id.bt_file);\n\t\tbtn_add_video = (ImageView) findViewById(R.id.btn_add_video);")

# Fix missing videoUrl declarations in EditActivity
edit_content = edit_content.replace("if (videoUrl == null || videoUrl.isEmpty())", "if (videoUrls == null || videoUrls.isEmpty())")
edit_content = edit_content.replace("loadMockTimelineThumbnails(videoUrl);", "loadMockTimelineThumbnails(videoUrls.get(0));")
edit_content = edit_content.replace("loadTimelineThumbnails(videoUrl);", "loadTimelineThumbnails(videoUrls.get(0));")

with open(f_edit, "w") as f:
    f.write(edit_content)


# 2. Fix ExportActivity.java
f_exp = "testapp/src/main/java/com/joe/epmediademo/Activity/ExportActivity.java"
with open(f_exp, "r") as f:
    exp_content = f.read()

exp_content = exp_content.replace("if (videoUrl == null || videoUrl.isEmpty())", "if (videoUrls == null || videoUrls.isEmpty())")
exp_content = exp_content.replace("if (videoUrl.startsWith(\"mock_\") || !new java.io.File(videoUrl).exists())", "if (videoUrls.get(0).startsWith(\"mock_\") || !new java.io.File(videoUrls.get(0)).exists())")
exp_content = exp_content.replace("if (videoUrl.contains(\"mountain\"))", "if (videoUrls.get(0).contains(\"mountain\"))")
exp_content = exp_content.replace("else if (videoUrl.contains(\"forest\") || videoUrl.contains(\"nature\"))", "else if (videoUrls.get(0).contains(\"forest\") || videoUrls.get(0).contains(\"nature\"))")
exp_content = exp_content.replace("else if (videoUrl.contains(\"tiktok\"))", "else if (videoUrls.get(0).contains(\"tiktok\"))")
exp_content = exp_content.replace("retriever.setDataSource(videoUrl);", "retriever.setDataSource(videoUrls.get(0));")
exp_content = exp_content.replace("boolean isMock = videoUrl.startsWith(\"mock_\") || !new java.io.File(videoUrl).exists();", "boolean isMock = videoUrls.get(0).startsWith(\"mock_\") || !new java.io.File(videoUrls.get(0)).exists();")
exp_content = exp_content.replace("extractor.setDataSource(videoUrl);", "extractor.setDataSource(videoUrls.get(0));")

# Fix the opt issue
# Find opt declaration
opt_decl = """				EpEditor.OutputOption opt = new EpEditor.OutputOption(pathVisuals);
				if (selectedResMode == 0) {
					opt.setWidth(1280);
					opt.setHeight(720);
				} else if (selectedResMode == 1) {
					opt.setWidth(1920);
					opt.setHeight(1080);
				} else if (selectedResMode == 2) {
					opt.setWidth(2560);
					opt.setHeight(1440);
				} else if (selectedResMode == 3) {
					opt.setWidth(3840);
					opt.setHeight(2160);
				}
		
				if (selectedFpsMode == 0) opt.frameRate = 24;
				else if (selectedFpsMode == 1) opt.frameRate = 30;
				else if (selectedFpsMode == 2) opt.frameRate = 60;"""

new_opt_decl = """		EpEditor.OutputOption opt = new EpEditor.OutputOption(pathVisuals);
		if (selectedResMode == 0) {
			opt.setWidth(1280);
			opt.setHeight(720);
		} else if (selectedResMode == 1) {
			opt.setWidth(1920);
			opt.setHeight(1080);
		} else if (selectedResMode == 2) {
			opt.setWidth(2560);
			opt.setHeight(1440);
		} else if (selectedResMode == 3) {
			opt.setWidth(3840);
			opt.setHeight(2160);
		}

		if (selectedFpsMode == 0) opt.frameRate = 24;
		else if (selectedFpsMode == 1) opt.frameRate = 30;
		else if (selectedFpsMode == 2) opt.frameRate = 60;
"""

exp_content = exp_content.replace(opt_decl, "")
# We need to insert new_opt_decl right BEFORE the commonListener definition
exp_content = exp_content.replace("OnEditorListener commonListener = new OnEditorListener() {", new_opt_decl + "\n\t\tOnEditorListener commonListener = new OnEditorListener() {")

with open(f_exp, "w") as f:
    f.write(exp_content)

print("Done fixing")
