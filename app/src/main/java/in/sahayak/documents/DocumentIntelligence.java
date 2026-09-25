package in.sahayak.documents;

import android.content.Context;
import android.graphics.*;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.*;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions;
import com.google.mlkit.nl.translate.*;
import com.google.mlkit.common.model.DownloadConditions;
import com.tom_roush.pdfbox.pdmodel.*;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.*;
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import com.tom_roush.pdfbox.pdmodel.graphics.state.RenderingMode;
import com.tom_roush.pdfbox.pdmodel.interactive.form.*;
import com.tom_roush.pdfbox.cos.COSName;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** CPU/on-device helpers. Invoke off the UI thread. */
public final class DocumentIntelligence {
 public static String recognize(Bitmap bitmap,boolean hindi)throws Exception{
  TextRecognizer r=hindi?TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build()):TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
  try{return Tasks.await(r.process(InputImage.fromBitmap(bitmap,0)),60,TimeUnit.SECONDS).getText();}finally{r.close();}
 }
 public static String ocr(Context context,File input,String password,String language,File searchable,Consumer<String> progress)throws Exception{
  boolean hindi=language.equals("hi");
  TextRecognizer recognizer=hindi?TextRecognition.getClient(new DevanagariTextRecognizerOptions.Builder().build()):TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
  StringBuilder content=new StringBuilder();boolean foundText=false;
  try(PDDocument source=PdfEngine.load(input,password);PDDocument out=new PDDocument()){
   if(source.getNumberOfPages()>30)throw new Exception("OCR supports up to 30 pages per job. Use Extract pages for a larger file.");
   PDType0Font latin=null,deva=null;
   if(searchable!=null){try(InputStream f=context.getAssets().open("fonts/NotoSans-Regular.ttf")){latin=PDType0Font.load(out,f);}try(InputStream f=context.getAssets().open("fonts/NotoSansDevanagari-Regular.ttf")){deva=PDType0Font.load(out,f);}}
   PDFRenderer renderer=new PDFRenderer(source);
   for(int i=0;i<source.getNumberOfPages();i++){
    if(Thread.currentThread().isInterrupted())throw new InterruptedException("Cancelled");
    progress.accept("Reading page "+(i+1)+" / "+source.getNumberOfPages());
    PDPage original=source.getPage(i);float scale=Math.min(2.5f,2200f/Math.max(original.getMediaBox().getWidth(),original.getMediaBox().getHeight()));
    Bitmap image=renderer.renderImage(i,scale);
    try{Text result=Tasks.await(recognizer.process(InputImage.fromBitmap(image,0)),60,TimeUnit.SECONDS);
     if(!result.getText().trim().isEmpty())foundText=true;content.append("Page ").append(i+1).append('\n').append(result.getText()).append("\n\n");
     if(searchable!=null){PDPage page=new PDPage(new PDRectangle(image.getWidth()/scale,image.getHeight()/scale));out.addPage(page);
      try(PDPageContentStream c=new PDPageContentStream(out,page)){
       c.drawImage(JPEGFactory.createFromImage(out,image,.92f),0,0,page.getMediaBox().getWidth(),page.getMediaBox().getHeight());
       for(Text.TextBlock block:result.getTextBlocks())for(Text.Line line:block.getLines()){
        Rect bounds=line.getBoundingBox();if(bounds==null||bounds.height()==0)continue;
        String value=line.getText();float size=Math.max(1,bounds.height()/scale*.8f);ArrayList<String> spans=new ArrayList<>();ArrayList<PDFont> fonts=new ArrayList<>();StringBuilder span=new StringBuilder();PDFont active=null;
        for(int offset=0;offset<value.length();){int cp=value.codePointAt(offset);offset+=Character.charCount(cp);PDFont font=cp>=0x900&&cp<=0x97f?deva:latin;String glyph=new String(Character.toChars(cp));try{font.getStringWidth(glyph);}catch(IllegalArgumentException unsupported){glyph="?";font=latin;}if(active!=font&&span.length()>0){spans.add(span.toString());fonts.add(active);span.setLength(0);}active=font;span.append(glyph);}
        if(span.length()>0){spans.add(span.toString());fonts.add(active);}float width=0;for(int n=0;n<spans.size();n++)width+=fonts.get(n).getStringWidth(spans.get(n))/1000*size;
        c.beginText();c.setRenderingMode(RenderingMode.NEITHER);c.setHorizontalScaling(width>0?bounds.width()/scale/width*100:100);c.newLineAtOffset(bounds.left/scale,(image.getHeight()-bounds.bottom)/scale);for(int n=0;n<spans.size();n++){c.setFont(fonts.get(n),size);c.showText(spans.get(n));}c.endText();
       }
      }
     }
    }finally{image.recycle();}
   }
   if(!foundText)throw new Exception("No text recognized. Try a sharper scan or choose the other OCR language.");if(searchable!=null)out.save(searchable);
   return content.toString();
  }finally{recognizer.close();}
 }
 public static String translate(String text,String direction,Consumer<String> progress)throws Exception{
  String[] pair=direction.split(">");if(pair.length!=2||!Arrays.asList("en","hi","bn").contains(pair[0])||!Arrays.asList("en","hi","bn").contains(pair[1])||pair[0].equals(pair[1]))throw new Exception("Choose different English, Hindi or Bengali languages");
  if(text.trim().isEmpty())throw new Exception("No text found. Run OCR first for scans.");if(text.length()>50000)throw new Exception("Translate up to 50,000 characters at once; extract fewer pages.");
  Translator client=Translation.getClient(new TranslatorOptions.Builder().setSourceLanguage(pair[0]).setTargetLanguage(pair[1]).build());
  try{progress.accept("Preparing language models. First use needs internet…");Tasks.await(client.downloadModelIfNeeded(new DownloadConditions.Builder().build()),180,TimeUnit.SECONDS);StringBuilder result=new StringBuilder("Machine translation — check important names and numbers.\nPowered by Google Translate\n\n");
   for(int from=0;from<text.length();){if(Thread.currentThread().isInterrupted())throw new InterruptedException("Cancelled");int end=Math.min(text.length(),from+1800);if(end<text.length()){int space=text.lastIndexOf(' ',end);if(space>from+900)end=space;}progress.accept("Translating "+(from*100/text.length())+"%");result.append(Tasks.await(client.translate(text.substring(from,end)),60,TimeUnit.SECONDS)).append('\n');from=end;}
   return result.toString();
  }finally{client.close();}
 }
 public static String summarize(String text){
  if(text.trim().isEmpty())throw new IllegalArgumentException("No text found. Run OCR first for scans.");
  if(text.length()>300000)throw new IllegalArgumentException("Extract fewer pages; summary limit is 300,000 characters.");
  String[] sentences=text.trim().split("(?<=[.!?।])\\s+|\\n+");Map<String,Integer> frequency=new HashMap<>();Set<String> stop=new HashSet<>(Arrays.asList("the","and","that","this","with","from","have","for","was","are","not","you","your","of","to","in","is","a","an","का","की","के","है","और","से","को","में"));
  for(String sentence:sentences)for(String word:sentence.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))if(word.length()>2&&!stop.contains(word))frequency.put(word,frequency.getOrDefault(word,0)+1);
  ArrayList<Integer> indexes=new ArrayList<>();for(int i=0;i<sentences.length;i++)if(!sentences[i].trim().isEmpty())indexes.add(i);
  indexes.sort((a,b)->Double.compare(score(sentences[b],frequency),score(sentences[a],frequency)));int target=Math.min(5,Math.max(1,(int)Math.ceil(indexes.size()*.3)));List<Integer> selected=new ArrayList<>(indexes.subList(0,Math.min(target,indexes.size())));Collections.sort(selected);
  StringBuilder s=new StringBuilder("Automatic key-sentence summary\nSelected from the original text; not a generative AI explanation. Read the full document for context.\n\n");for(int i:selected)s.append("• ").append(sentences[i].trim()).append("\n\n");return s.toString();
 }
 static double score(String sentence,Map<String,Integer> frequency){String[] words=sentence.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+");double score=0;for(String word:words)score+=frequency.getOrDefault(word,0);return score/Math.sqrt(Math.max(1,words.length));}
 public static void createField(File input,String password,String name,File output)throws Exception{
  if(!name.matches("[A-Za-z][A-Za-z0-9_]{0,39}"))throw new Exception("Use a short field name such as EmployeeName (English letters, numbers, underscore).");
  try(PDDocument d=PdfEngine.load(input,password)){PDAcroForm form=d.getDocumentCatalog().getAcroForm();if(form==null){form=new PDAcroForm(d);d.getDocumentCatalog().setAcroForm(form);}if(form.getField(name)!=null)throw new Exception("That field name already exists");PDResources resources=form.getDefaultResources();if(resources==null){resources=new PDResources();form.setDefaultResources(resources);}resources.put(COSName.getPDFName("MaaHelv"),PDType1Font.HELVETICA);
   PDPage page=d.getPage(0);PDRectangle box=page.getCropBox();PDTextField field=new PDTextField(form);field.setPartialName(name);field.setDefaultAppearance("/MaaHelv 12 Tf 0 g");field.setMultiline(false);form.getFields().add(field);com.tom_roush.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget widget=field.getWidgets().get(0);widget.setRectangle(new PDRectangle(box.getLowerLeftX()+24,box.getLowerLeftY()+24,Math.max(20,Math.min(250,box.getWidth()-48)),28));widget.setPage(page);widget.setPrinted(true);page.getAnnotations().add(widget);field.setValue("");
   try(PDPageContentStream c=new PDPageContentStream(d,page,PDPageContentStream.AppendMode.APPEND,true,true)){c.setStrokingColor(80);c.addRect(box.getLowerLeftX()+24,box.getLowerLeftY()+24,Math.max(20,Math.min(250,box.getWidth()-48)),28);c.stroke();c.beginText();c.setFont(PDType1Font.HELVETICA,10);c.newLineAtOffset(box.getLowerLeftX()+24,box.getLowerLeftY()+57);c.showText(name);c.endText();}d.save(output);
  }
 }
}
