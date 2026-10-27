# Yaku's Aniyomi Extensions Repository

Aniyomi anime extension repository hosting the **[Hanime.moe](https://hanime.moe/)** extension.

---

## 📲 How to Add to Aniyomi

### Ready Link to Add Repository:
```text
https://raw.githubusercontent.com/yakuyakuku/aniyomi-repo/main/index.min.json
```

### Steps:
1. Copy the URL above:
   ```text
   https://raw.githubusercontent.com/yakuyakuku/aniyomi-repo/main/index.min.json
   ```
2. Open **Aniyomi**.
3. Go to **More** > **Settings** > **Browse**.
4. Tap **Extension repos** (or **Anime extension repos**).
5. Tap **Add repository** (+), paste the URL, and tap **OK**.
6. Go back to the **Extensions** (Anime) tab — **Hanime.moe** will appear ready to install!

---

## 📦 Direct APK Downloads

You can also install the extension APK manually:

- **[aniyomi-en.hanimemoe-v14.1.apk](apk/aniyomi-en.hanimemoe-v14.1.apk)** (Signed Release APK)

---

## 🚀 Extension Features

- **Browse Popular:** Videos sorted by most views (`/browse/videos?sort=views`).
- **Latest Updates:** Newest releases and uploads (`/browse/videos?sort=recent`).
- **Search & Filter:**
  - Search query support.
  - Sort by: *Recent uploads*, *Newest releases*, *Most views*, *Most likes*, *Trending*, *A – Z*.
  - Filter by Censorship (*All*, *Censored*, *Uncensored*).
  - Filter by 75+ tags (*Harem*, *Vanilla*, *Creampie*, *HD*, etc.).
- **Rich Anime & Series Details:**
  - Full title, HD cover image, studio/brand name, tags/genres, completion status, and synopsis.
- **Episode List:**
  - Aggregates franchise/series episodes (`siblings`) in chronological order.
  - Handles both multi-episode series and standalone video entries.
- **Video Extraction & Playback:**
  - Direct HLS playback via Vimbed player (`master.m3u8`).
  - Supports multiple stream resolutions: **1080p**, **720p**, **480p**.
  - Configurable preferred quality in Aniyomi Settings.

---

## 🛠️ Building From Source

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :src:en:hanimemoe:assembleRelease
```

The built APK will be located in `src/en/hanimemoe/build/outputs/apk/release/`.
