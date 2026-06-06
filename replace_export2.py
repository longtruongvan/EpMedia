file_path = "testapp/src/main/java/com/joe/epmediademo/Activity/ExportActivity.java"
with open(file_path, "r") as f:
    lines = f.readlines()

out_lines = []
i = 0
while i < len(lines):
    line = lines[i]
    if "private String videoUrl;" in line:
        out_lines.append(line.replace("private String videoUrl;", "private java.util.ArrayList<String> videoUrls;"))
    elif "videoUrl = intent.getStringExtra(\"VIDEO_PATH\");" in line:
        out_lines.append(line.replace('videoUrl = intent.getStringExtra("VIDEO_PATH");', 'videoUrls = intent.getStringArrayListExtra("VIDEO_PATHS");'))
    elif "EpVideo epVideo = new EpVideo(videoUrl);" in line:
        # Start of replacement block
        out_lines.append("\t\tjava.util.List<EpVideo> epVideos = new java.util.ArrayList<>();\n")
        out_lines.append("\t\tif (videoUrls != null && !videoUrls.isEmpty()) {\n")
        out_lines.append("\t\t\tfor (int i = 0; i < videoUrls.size(); i++) {\n")
        out_lines.append("\t\t\t\tEpVideo epVideo = new EpVideo(videoUrls.get(i));\n")
        
        # We need to apply everything up to "EpEditor.exec" inside this loop.
        # So we continue reading lines and keeping them, until we hit EpEditor.exec
        i += 1
        while i < len(lines):
            inner_line = lines[i]
            if "EpEditor.exec(epVideo, opt, new OnEditorListener() {" in inner_line:
                # End of configuration block for epVideo
                out_lines.append("\t\t\t\tepVideos.add(epVideo);\n")
                out_lines.append("\t\t\t}\n")
                out_lines.append("\t\t}\n")
                
                # Now write the exec part
                out_lines.append("\t\tOnEditorListener commonListener = new OnEditorListener() {\n")
                
                # Copy the listener contents
                i += 1
                open_braces = 1
                while i < len(lines):
                    l = lines[i]
                    out_lines.append(l)
                    if "{" in l:
                        open_braces += l.count("{")
                    if "}" in l:
                        open_braces -= l.count("}")
                    i += 1
                    if open_braces == 0 and "});" in l:
                        # Listener ended!
                        # Remove the last line because it was });
                        out_lines.pop()
                        out_lines.append("\t\t};\n\n")
                        break
                
                # Now append the merge or exec call
                out_lines.append("\t\tif (epVideos.size() == 1) {\n")
                out_lines.append("\t\t\tEpEditor.exec(epVideos.get(0), opt, commonListener);\n")
                out_lines.append("\t\t} else if (epVideos.size() > 1) {\n")
                out_lines.append("\t\t\tEpEditor.merge(epVideos, opt, commonListener);\n")
                out_lines.append("\t\t}\n")
                break
            else:
                # Replace specific stuff
                if "epVideo.clip(trimStartSec, trimEndSec - trimStartSec);" in inner_line:
                    out_lines.append("\t\t\t\tif (i == 0 && (trimStartSec > 0 || trimEndSec > 0)) epVideo.clip(trimStartSec, trimEndSec - trimStartSec);\n")
                else:
                    out_lines.append("\t\t" + inner_line)
            i += 1
    else:
        out_lines.append(line)
    i += 1

with open(file_path, "w") as f:
    f.writelines(out_lines)

print("Done")
