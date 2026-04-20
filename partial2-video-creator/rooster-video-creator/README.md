# Rooster's video creator

## Architecture plan (minimal one-way flow)

```mermaid
flowchart LR
	A[User input folder\nPhotos + Videos with GPS] --> B[MetadataService\nExifToolCliService]
	B --> C[Media timeline builder\nSort oldest to newest\nFind first and last GPS]
	C --> D[AIService\nGeminiApiService]
	D --> E[AI outputs\nEssence image prompt\nNarration script\nInspirational phrase]
	E --> F[Image generation step\nGemini image API or fallback provider]
	E --> G[TTS step\nNarration audio file]
	C --> H[FFmpegService\nPortrait cover normalization\nPer-media visual clips]
	C --> I[MapService\nMapboxOsmService\nStatic map with first/last markers]
	F --> J[FFmpeg final assembly]
	G --> J
	H --> J
	I --> J
	J --> K[Final portrait video output\n1080x1920, chronological, narrated]
```

## Architecture & Design Decisions

The application manages a strictly linear, one-way pipeline prioritizing observability and separation of concerns. It delegates heavy lifting entirely to proven external tools (ExifTool, FFmpeg, Gemini, Mapbox) encapsulated behind uniform interfaces. Processing occurs across five isolated steps: extracting metadata alongside AI prompt generation, producing per-scene TTS narrations, assembling visual portrait clips synced to that audio, mastering audio loudness norms, and appending an AI-quoted closing map sequence.

## Recommended tools for this exact project

- ExifTool: robust GPS/date extraction for mixed camera formats.
- FFmpeg: scaling, crop-to-cover portrait, concat, audio attach, loudness normalization.
- Gemini: script and phrase generation, plus first-image prompt generation.
- Mapbox Static API: deterministic map image with custom first/last pin styles.

## Optional upgrades (only if needed)

- Better timeline metadata fallback: ExifTool + Apache Tika MIME detection for rare files.
- Better speech quality: ElevenLabs or Azure Speech instead of basic/free TTS.
- Better loudness compliance: add explicit FFmpeg loudnorm two-pass to target YouTube-like levels.
- Better rendering throughput: keep FFmpeg operations as one consolidated filter graph when pipeline is stable.

### Run the project

From the project root, run:

```bash
./run.sh
```

### Example Usage

You can run the pipeline with multiple images to generate a narrated video with an outro map and a custom phrase overlay. Here is a simple example using three provided test images:

```bash
# Make sure to load your API keys into the environment first, for example:
# set -a && source .env.prod && set +a

mvn compile exec:java -Dexec.mainClass="edu.up.cg.Main" -Dexec.args="--out output-example bin/testcase/gdl2.jpg bin/testcase/cdmx.jpg bin/testcase/img-santiago.jpg"
```
