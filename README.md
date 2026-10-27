# Yaku's Aniyomi Extensions Repository

Aniyomi anime extension repository hosting the **[Hanime.moe](https://hanime.moe/)** extension (v14.2).

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
4. Tap **Anime extension repos** (or **Extension repos**).
5. Tap **Add repository** (+), paste the URL, and tap **OK**.
6. Go back to the **Extensions** (Anime) tab — **Hanime.moe** will appear ready to install or update!

---

## 📦 Direct APK Downloads

You can also install or update the extension APK manually:

- **[aniyomi-en.hanimemoe-v14.2.apk](apk/aniyomi-en.hanimemoe-v14.2.apk)** (Signed Release APK v14.2)

---

## ✨ Features & Enhancements (v14.2)

- **Clean Episode Labeling:**
  - Episodes are formatted strictly as `Episode X` (no series name clutter).
  - Upcoming or unreleased episodes are labeled `Episode X (Upcoming)` with scanlator tag `Upcoming` to grey them out and clearly indicate pending releases.
- **Newest-to-Oldest Ordering:**
  - Episodes are strictly ordered from newest to oldest (e.g. Episode 12 down to Episode 1).
- **Rich Metadata Display:**
  - Full plot synopsis.
  - Studio / Brand.
  - Release Year.
  - Censorship status (*Censored* / *Uncensored*).
  - Overall Rank (e.g. `#847`).
  - Total Views (e.g. `2.9M`).
  - Rating & Likes (e.g. `98% (👍 3,783 / 👎 74)`).
  - Download count.
  - Cleaned list of Alternative Titles.
- **Video Extraction & Playback:**
  - Direct HLS playback via Vimbed player (`master.m3u8`).
  - Multiple stream resolutions: **1080p**, **720p**, **480p**.
  - Configurable preferred quality in Aniyomi Settings.

---

## 🛠️ Building From Source

```bash
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :src:en:hanimemoe:assembleRelease
```

The built APK is saved to `src/en/hanimemoe/build/outputs/apk/release/`.
