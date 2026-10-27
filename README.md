# Yaku's Aniyomi Extensions Repository

Custom Aniyomi anime extension repository hosting high-quality extensions for:
- **[Hanime.moe](https://hanime.moe/)** (v14.2)
- **[Hentaini.com](https://hentaini.com/)** (v14.1)

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
6. Go back to the **Extensions** (Anime) tab — both **Hanime.moe** and **Hentaini** will appear ready to install!

---

## 📦 Direct APK Downloads

You can also install or update the extension APKs directly:

- **[aniyomi-en.hentaini-v14.1.apk](apk/aniyomi-en.hentaini-v14.1.apk)** (Hentaini Signed Release v14.1)
- **[aniyomi-en.hanimemoe-v14.2.apk](apk/aniyomi-en.hanimemoe-v14.2.apk)** (Hanime.moe Signed Release v14.2)

---

## ✨ Extension Features

### 🎬 Hentaini (`hentaini.com`) - v14.1
- **Popular, Latest & Search:** Full browse support backed by the Strapi v4 backend API.
- **Filters:** Censorship filter (All, Censored, Uncensored), Sort filter (Visits, Published Date, Release Date, Title), and 29 Genre filters.
- **Clean Episode Numbering:** Standardized strictly as `Episode X`.
- **Upcoming Episode Detection:** Future or unreleased episodes are labeled `Episode X (Upcoming)` with scanlator tag `Upcoming` to grey them out and clearly indicate pending releases.
- **Strict Newest-to-Oldest Ordering:** Episodes are arranged chronologically from newest release to oldest.
- **Rich Metadata:** Displays Studio / Producer, Censorship status, Status (Ongoing / Completed), Total Views, Episode count, and Alternative Titles.
- **High Performance Video Streaming:** Direct HLS playback (`.m3u8`) with multi-quality resolution detection and fallback support.

### 🌸 Hanime.moe (`hanime.moe`) - v14.2
- **Clean Episode Labeling:** Formatted strictly as `Episode X`.
- **Upcoming Episodes:** Labeled `Episode X (Upcoming)` with scanlator tag `Upcoming` for future air dates.
- **Strict Newest-to-Oldest Ordering:** Episodes ordered newest first.
- **Rich Metadata Display:** Full synopsis, Studio / Brand, Release Year, Censorship status, Rank, Views, Likes/Dislikes rating, Downloads count, and Alternate Titles.
- **Video Extraction:** Multi-quality HLS streams (1080p, 720p, 480p) via Vimbed player with user-configurable preferred resolution.

---

## 🛠️ Building From Source

```bash
# Build Hanime.moe extension:
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :src:en:hanimemoe:assembleRelease

# Build Hentaini extension:
JAVA_HOME=/usr/lib/jvm/java-17-openjdk ./gradlew :src:en:hentaini:assembleRelease
```

Built APKs are located in `src/en/<extension>/build/outputs/apk/release/`.
