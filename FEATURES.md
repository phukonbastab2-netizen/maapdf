# maapdf 2.0 feature scope

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

## Not implemented yet

- Scanned PDF OCR/searchable text layer.
- Faithful Word/Excel/PowerPoint-to-PDF layout conversion.
- PDF-to-Office reconstruction of editable original layouts, images and tables.
- HTML-to-PDF, validated PDF/A, new interactive form creation.
- AI summary, document translation, reusable chained workflows.
- Cryptographic digital signatures, remote signature requests and comprehensive visual comparison.

The app does not claim full iLovePDF feature parity. Office exports state their fidelity limits before use. Complex PDFs, unusual fonts, JPEG2000 images and large files can fail or render differently. The download remains a debug-signed preview.

## Privacy and safety boundaries

PDF tools run locally with no uploads. Inputs and results are copied into private app cache; originals stay untouched. Passwords are used in memory, not saved in settings. Uninstall removes cache. Do not use crop as redaction. The typed signature is a visual annotation only. Always inspect the exported file, especially redactions, before sharing it. Raster operations remove interactive content. Existing digital signatures are not preserved as valid signatures through edits.

27 named PDF tools plus text-to-PDF export are exposed. The advanced-functions notice explicitly lists missing engines rather than displaying nonworking action buttons.
