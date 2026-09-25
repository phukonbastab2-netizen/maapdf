# maapdf 3.0 feature scope

Reference reviewed: https://www.ilovepdf.com/ on 2026-09-25. maapdf is independent and does not call iLovePDF APIs or copy its branding.

## Implemented on the phone

- Merge selected PDFs in displayed order.
- Split every page to a PDF ZIP.
- Extract, remove or reorder selected pages; rotate all pages; crop equal margins.
- Multiple JPG/PNG images to PDF; PDF pages to a JPG ZIP.
- PDF existing text to TXT, simple Markdown text, DOCX paragraphs or XLSX rows.
- PDF pages to image-based PowerPoint slides.
- TXT/CSV and Office text previews to a newly laid-out PDF.
- Raster compression: loses text selection, forms, links and digital signatures; size reduction is not guaranteed.
- English text watermark, page numbers, first-page text annotation, first-page typed signature.
- Permanent rectangular blackout through raster reconstruction; sources are never modified. The rectangle uses page and percentage coordinates. All output pages become images.
- Add password, remove a known password, list/fill existing form fields, flatten existing forms.
- Compare extracted text line by line; best-effort resave of readable damaged PDFs.
- Read selectable PDF text aloud, in addition to text previews.
- Original 2D Maa caricature, gently animated; grouped tools and preview-before-save workflow.

## Added in V3

- English/Latin and Hindi/Devanagari OCR on-device, to editable TXT or image-based searchable PDF; up to 30 pages per job. Empty scans report failure instead of a false success. OCR is imperfect, especially handwriting and complex tables.
- Automatic key-sentence summaries of existing text. These select original sentences; they are not generative AI explanations.
- Text translation between English, Hindi and Bengali using Google ML Kit. First use downloads language models (about 30 MB each), potentially using mobile data, after the user confirms. Translation outputs TXT and does not preserve PDF layout. Powered by Google Translate; review important content.
- Create a named text form field near the bottom-left of the first page. Existing fill/flatten tools work on it. Preview to avoid overlapping page content.
- Local HTML preview and Android print-to-PDF; JavaScript and remote resources are blocked. Requires an available Android print service.
- A reusable saved workflow combining rotation, page numbering and a DRAFT watermark in a fixed order.
- Recognized/translated text can be opened in the simple editor for correction before saving.

## Still not implemented

- Faithful Word/Excel/PowerPoint-to-PDF layout conversion and PDF-to-Office layout reconstruction.
- Validated PDF/A export.
- Generative AI summaries, arbitrary workflow chains, cryptographic digital signatures and remote signature requests.
- Comprehensive visual document comparison; existing comparison is text only.

The app does not claim full iLovePDF feature parity. Office exports state their fidelity limits before use. Complex PDFs, unusual fonts, JPEG2000 images and large files can fail or render differently. The download remains a debug-signed preview.

## Privacy and safety boundaries

PDF processing and language inference run locally. Google ML Kit may download models and sends performance/utilization metrics to Google, but does not upload input document content or results. See https://developers.google.com/ml-kit/terms and https://cloud.google.com/translate for attribution and service details. Inputs and results are copied into private app cache; originals stay untouched. Passwords are used in memory, not saved in settings. Uninstall removes cache. Do not use crop as redaction. The typed signature is a visual annotation only. Always inspect the exported file, especially redactions, before sharing it. Raster operations remove interactive content. Existing digital signatures are not preserved as valid signatures through edits.

34 named tools plus text-to-PDF export are exposed. The advanced-functions notice explicitly lists missing engines rather than displaying nonworking action buttons.
