import re

f_edit = "testapp/src/main/java/com/joe/epmediademo/Activity/EditActivity.java"
with open(f_edit, "r") as f:
    content = f.read()

# Replace the signature
content = content.replace("private void loadTimelineThumbnails(final String videoPath) {", 
                          "private void loadTimelineThumbnails(final String videoPath) {\n\t\tloadTimelineThumbnails(videoPath, true);\n\t}\n\n\tprivate void loadTimelineThumbnails(final String videoPath, final boolean clearOld) {")

content = content.replace("thumbnailContainer.removeAllViews();", "if (clearOld) thumbnailContainer.removeAllViews();")

with open(f_edit, "w") as f:
    f.write(content)

print("Fixed loadTimelineThumbnails")
