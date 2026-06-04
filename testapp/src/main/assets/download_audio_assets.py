import os
import urllib.request

def download_file(url, filepath):
    try:
        print(f"Downloading {url} to {filepath}...")
        headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)'}
        req = urllib.request.Request(url, headers=headers)
        with urllib.request.urlopen(req, timeout=15) as response, open(filepath, 'wb') as out_file:
            out_file.write(response.read())
        print(f"Successfully downloaded {filepath}")
        return True
    except Exception as e:
        print(f"Failed to download {url}: {e}")
        return False

def main():
    assets_dir = os.path.dirname(os.path.abspath(__file__))
    audio_dir = os.path.join(assets_dir, "audio")
    os.makedirs(audio_dir, exist_ok=True)

    tracks = {
        "track_acoustic.mp3": [
            "https://www.learningcontainer.com/wp-content/uploads/2020/02/Kalimba.mp3",
            "https://github.com/prof3ssorSt3v3/media-sample-files/raw/master/creative-common.mp3"
        ],
        "track_lofi.mp3": [
            "https://filesamples.com/samples/audio/mp3/sample1.mp3",
            "https://github.com/prof3ssorSt3v3/media-sample-files/raw/master/sample.mp3"
        ],
        "track_electronic.mp3": [
            "https://dl.espressif.com/dl/audio/ff-16b-2c-44100hz.mp3",
            "https://github.com/ybrid/test-files/raw/main/fec/sine-1kHz-stereo-1s.mp3"
        ]
    }

    for filename, urls in tracks.items():
        filepath = os.path.join(audio_dir, filename)
        if os.path.exists(filepath) and os.path.getsize(filepath) > 0:
            print(f"{filename} already exists, skipping.")
            continue
        
        success = False
        for url in urls:
            if download_file(url, filepath):
                success = True
                break
        
        if not success:
            # Fallback: create a dummy non-empty file so compilation doesn't fail
            print(f"Creating empty fallback for {filename}...")
            with open(filepath, 'wb') as f:
                f.write(b'\x00' * 1024)

if __name__ == "__main__":
    main()
