#!/usr/bin/env python3
"""
Verification script for the Hanime.moe Aniyomi Extension.
Tests all endpoints, parsers, and stream resolution to ensure 100% operational status.
"""

import urllib.request
import re
import html
import json
import sys

BASE_URL = "https://hanime.moe"
HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
    "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8",
    "Accept-Language": "en-US,en;q=0.9",
    "Referer": f"{BASE_URL}/",
}

DATA_PAGE_REGEX = re.compile(r'data-page\s*=\s*["\']([^"\']+)["\']')

def get(url, extra_headers=None):
    h = dict(HEADERS)
    if extra_headers:
        h.update(extra_headers)
    req = urllib.request.Request(url, headers=h)
    with urllib.request.urlopen(req) as resp:
        return resp.read().decode("utf-8", errors="ignore")

def extract_inertia(html_text):
    m = DATA_PAGE_REGEX.search(html_text)
    if not m:
        raise ValueError("Could not find data-page in HTML")
    return json.loads(html.unescape(m.group(1)))

def fix_url(url):
    if not url:
        return ""
    if url.startswith("http://") or url.startswith("https://"):
        return url
    if url.startswith("//"):
        return f"https:{url}"
    if url.startswith("/"):
        return f"{BASE_URL}{url}"
    return f"{BASE_URL}/{url}"

def test_popular():
    print("[1/6] Testing Popular Anime (/browse/videos?sort=views)...")
    html_content = get(f"{BASE_URL}/browse/videos?sort=views&page=1")
    data = extract_inertia(html_content)
    props = data.get("props", {})
    videos = props.get("videos", [])
    meta = props.get("meta", {})
    assert len(videos) > 0, "No popular videos found"
    assert meta.get("current_page") == 1, "Expected page 1"
    first = videos[0]
    print(f"  ✓ Found {len(videos)} videos (Total: {meta.get('total')})")
    print(f"  ✓ Top video: '{first.get('title')}' (Slug: {first.get('slug')})")
    print(f"  ✓ Thumbnail: {fix_url(first.get('poster') or first.get('thumbnail'))}")
    return first.get("slug")

def test_latest():
    print("\n[2/6] Testing Latest Updates (/browse/videos?sort=recent)...")
    html_content = get(f"{BASE_URL}/browse/videos?sort=recent&page=1")
    data = extract_inertia(html_content)
    videos = data.get("props", {}).get("videos", [])
    assert len(videos) > 0, "No latest videos found"
    first = videos[0]
    print(f"  ✓ Found {len(videos)} latest uploads")
    print(f"  ✓ Latest: '{first.get('title')}' (Released: {first.get('released_at')})")

def test_search():
    print("\n[3/6] Testing Search Anime (/search?q=demon&sort=views)...")
    html_content = get(f"{BASE_URL}/search?q=demon&sort=views&page=1")
    data = extract_inertia(html_content)
    videos = data.get("props", {}).get("videos", [])
    assert len(videos) > 0, "Search returned 0 results"
    print(f"  ✓ Search 'demon' returned {len(videos)} results")
    print(f"  ✓ First match: '{videos[0].get('title')}'")

def test_anime_details(slug):
    print(f"\n[4/6] Testing Anime Details (/videos/{slug})...")
    html_content = get(f"{BASE_URL}/videos/{slug}")
    data = extract_inertia(html_content)
    props = data.get("props", {})
    video = props.get("video")
    series = props.get("series")
    assert video is not None, "Failed to get video props"

    title = series.get("title") if series else video.get("title")
    cover = fix_url(series.get("cover") if series else (video.get("poster") or video.get("thumbnail")))
    desc = series.get("synopsis") if series else video.get("blurb")
    brand = (series.get("brand") or {}).get("name") if series else (video.get("brand") or {}).get("name")
    tags = [t.get("name") for t in (series.get("tags") or [])] if series else []

    print(f"  ✓ Title: {title}")
    print(f"  ✓ Brand: {brand}")
    print(f"  ✓ Cover: {cover}")
    print(f"  ✓ Synopsis: {desc[:100] if desc else 'N/A'}...")
    print(f"  ✓ Tags: {', '.join(tags[:5])} (Total {len(tags)})")
    return props

def test_episodes(props):
    print("\n[5/6] Testing Episodes List...")
    siblings = props.get("siblings") or []
    video = props.get("video")
    if siblings:
        print(f"  ✓ Series with multiple episodes ({len(siblings)} episodes):")
        for ep in siblings[:3]:
            print(f"    - #{ep.get('number', '?')}: {ep.get('title')} (/videos/{ep.get('slug')})")
        if len(siblings) > 3:
            print(f"    ... and {len(siblings) - 3} more episodes")
    else:
        print(f"  ✓ Standalone video episode: {video.get('title')}")

def test_video_sources(props):
    print("\n[6/6] Testing Video Stream Extraction (Vimbed HLS master playlist)...")
    video = props.get("video")
    sources = video.get("sources", [])
    assert len(sources) > 0, "No video sources found"

    vimbed_src = next((s for s in sources if "vimbed.com" in s.get("url", "") or s.get("type") == "iframe"), None)
    assert vimbed_src is not None, "No Vimbed iframe source found"

    vimbed_url = vimbed_src.get("url")
    print(f"  ✓ Found player URL: {vimbed_url}")

    # Fetch vimbed page
    v_html = get(vimbed_url, {"Referer": f"{BASE_URL}/"})
    v_data = extract_inertia(v_html)
    stream_url = v_data.get("props", {}).get("video", {}).get("stream_url")
    assert stream_url is not None, "Failed to resolve stream_url from Vimbed"
    print(f"  ✓ Master M3U8 URL: {stream_url}")

    # Fetch master m3u8
    v_headers = {
        "Referer": vimbed_url,
        "Origin": "https://demo.vimbed.com",
    }
    m3u8_content = get(stream_url, v_headers)
    assert "#EXTM3U" in m3u8_content, "Invalid M3U8 response"

    # Parse resolutions
    resolutions = re.findall(r"RESOLUTION=\d+x(\d+)", m3u8_content)
    print(f"  ✓ Available Stream Qualities: {', '.join(f'{r}p' for r in resolutions)}")
    assert len(resolutions) > 0, "No stream resolutions found in master playlist"

    # Verify stream playlist chunk is accessible
    sub_url = stream_url.rsplit("/", 1)[0] + f"/{resolutions[0]}p/playlist.m3u8"
    sub_content = get(sub_url, v_headers)
    assert "#EXTINF" in sub_content, "Sub playlist did not contain video segments"
    print(f"  ✓ Stream playlist ({resolutions[0]}p) verified accessible with segments!")

def main():
    print("=" * 60)
    print("   Hanime.moe Aniyomi Extension Verification Suite")
    print("=" * 60)

    slug = test_popular()
    test_latest()
    test_search()
    # Test details and video streaming on a known multi-episode series
    test_slug = "enjo-kouhai-12"
    props = test_anime_details(test_slug)
    test_episodes(props)
    test_video_sources(props)

    print("\n" + "=" * 60)
    print("  ALL VERIFICATION CHECKS PASSED (6/6) - 100% OPERATIONAL")
    print("=" * 60)

if __name__ == "__main__":
    main()
