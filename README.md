# Yaku's Aniyomi Extensions Repository

Custom Aniyomi anime extension repository hosting high-quality extensions for:
- **[Hanime.moe](https://hanime.moe/)** (v14.4)
- **[Hentaini.com](https://hentaini.com/)** (v14.3)

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
6. Go back to the **Extensions** (Anime) tab — both **Hanime.moe** and **Hentaini** will appear ready to install or update!

---

## 📦 Direct APK Downloads

You can also install or update the extension APKs directly:

- **[aniyomi-en.hanimemoe-v14.4.apk](apk/aniyomi-en.hanimemoe-v14.4.apk)** (Hanime.moe Signed Release v14.4)
- **[aniyomi-en.hentaini-v14.3.apk](apk/aniyomi-en.hentaini-v14.3.apk)** (Hentaini Signed Release v14.3)

---

## ✨ Extension Features

### 🎬 Hentaini (`hentaini.com`) - v14.3
- **Direct Video Downloads (100% Native In-App):** Extracts direct high-speed MP4 video files from **Mp4Upload**, **BurstCloud (`BR`)**, and **YourUpload** so episodes download natively within Aniyomi's built-in downloader (zero FFmpeg timestamp errors) as well as external downloaders.
- **Clean Episode Numbering:** Standardized strictly as `Episode X`.
- **Upcoming Episode Detection:** Future or unreleased episodes are labeled `Episode X (Upcoming)` with scanlator tag `Upcoming` to grey them out and clearly indicate pending releases.
- **Strict Newest-to-Oldest Ordering:** Episodes are arranged chronologically from newest release to oldest.
- **Rich Metadata:** Displays Studio / Producer, Censorship status, Status (Ongoing / Completed), Total Views, Episode count, and Alternative Titles.
- **Browse & Filters:** Popular, Latest, Search queries, Censorship filters, Sort filters, and 29 Genre filters.

### 🌸 Hanime.moe (`hanime.moe`) - v14.4
- **Direct Video Downloads (100% Native In-App):** Extracts direct high-speed MKV video files (1080p, 720p, 480p) from Vimbed so downloads complete natively inside Aniyomi at full bandwidth without HLS segment issues.
- **Clean Episode Labeling:** Formatted strictly as `Episode X`.
- **Upcoming Episodes:** Labeled `Episode X (Upcoming)` with scanlator tag `Upcoming` for future air dates.
- **Strict Newest-to-Oldest Ordering:** Episodes ordered newest first.
- **Rich Metadata Display:** Full synopsis, Studio / Brand, Release Year, Censorship status, Rank, Views, Likes/Dislikes rating, Downloads count, and Alternate Titles.
- **Multi-Quality Playback:** 1080p, 720p, and 480p with configurable preferred resolution.

---

## 🛠️ Building From Source

```bash
# Build Hanime.moe extension:
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :src:en:hanimemoe:assembleRelease

# Build Hentaini extension:
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :src:en:hentaini:assembleRelease
```

Built APKs are located in `src/en/<extension>/build/outputs/apk/release/`.
