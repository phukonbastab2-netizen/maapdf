package in.sahayak.documents;

import android.graphics.Bitmap;
import com.tom_roush.pdfbox.pdmodel.*;
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.pdmodel.encryption.*;
import com.tom_roush.pdfbox.pdmodel.graphics.image.*;
import com.tom_roush.pdfbox.pdmodel.interactive.form.*;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility;
import java.io.*;import java.util.*;import java.util.zip.*;

/** Every operation produces a separate file. Never writes to source documents. */
public final class PdfEngine {
 public static int[] pages(String text,int count)throws Exception{
  ArrayList<Integer> result=new ArrayList<>();
  for(String token:text.replace(" ","").split(",")){
   String[] range=token.split("-");if(range.length<1||range.length>2)throw new Exception("Use pages like 1,3-5");
   int a=Integer.parseInt(range[0]),b=range.length==2?Integer.parseInt(range[1]):a;
   if(a<1||b>count||b<a)throw new Exception("Page numbers must be between 1 and "+count);
   for(int i=a;i<=b;i++){result.add(i-1);if(result.size()>200)throw new Exception("Maximum 200 pages per job");}
  }
  if(result.isEmpty())throw new Exception("Choose at least one page");return result.stream().mapToInt(i->i).toArray();
 }
 static PDDocument load(File file,String password)throws Exception{
  if(file.length()>30*1024*1024)throw new Exception("Use PDFs under 30 MB on this phone");
  PDDocument d=PDDocument.load(file,password);
  if(d.getNumberOfPages()>200){d.close();throw new Exception("Maximum 200 pages per job");}return d;
 }
 public static String text(File f,String password)throws Exception{try(PDDocument d=load(f,password)){return new PDFTextStripper().getText(d);}}
 public static void run(String action,File input,List<File> extras,File output,String arg,String password)throws Exception{
  try(PDDocument d=load(input,password)){
   if(action.equals("extract")||action.equals("remove")||action.equals("reorder")){
    int[] chosen=pages(arg,d.getNumberOfPages());Set<Integer> removed=new HashSet<>();for(int p:chosen)removed.add(p);
    try(PDDocument out=new PDDocument()){if(action.equals("remove")){for(int i=0;i<d.getNumberOfPages();i++)if(!removed.contains(i))out.importPage(d.getPage(i));}else for(int p:chosen)out.importPage(d.getPage(p));
     if(out.getNumberOfPages()==0)throw new Exception("Keep at least one page");out.save(output);}return;
   }
   if(action.equals("split")){try(ZipOutputStream zip=new ZipOutputStream(new FileOutputStream(output))){for(int i=0;i<d.getNumberOfPages();i++){try(PDDocument out=new PDDocument()){out.importPage(d.getPage(i));zip.putNextEntry(new ZipEntry("page-"+(i+1)+".pdf"));ByteArrayOutputStream pageBytes=new ByteArrayOutputStream();out.save(pageBytes);zip.write(pageBytes.toByteArray());zip.closeEntry();}}}return;}
   if(action.equals("merge")){PDFMergerUtility merger=new PDFMergerUtility();for(File f:extras)try(PDDocument extra=load(f,password)){if(d.getNumberOfPages()+extra.getNumberOfPages()>200)throw new Exception("Maximum 200 combined pages");merger.appendDocument(d,extra);}d.save(output);return;}
   if(action.equals("jpg")){PDFRenderer renderer=new PDFRenderer(d);try(ZipOutputStream zip=new ZipOutputStream(new FileOutputStream(output))){for(int i=0;i<d.getNumberOfPages();i++){Bitmap b=renderer.renderImage(i,Math.min(1.5f,1800f/Math.max(d.getPage(i).getMediaBox().getWidth(),d.getPage(i).getMediaBox().getHeight())));zip.putNextEntry(new ZipEntry("page-"+(i+1)+".jpg"));b.compress(Bitmap.CompressFormat.JPEG,85,zip);zip.closeEntry();b.recycle();}}return;}
   if(action.equals("compress")){PDFRenderer renderer=new PDFRenderer(d);try(PDDocument out=new PDDocument()){for(int i=0;i<d.getNumberOfPages();i++){Bitmap b=renderer.renderImage(i,Math.min(1.2f,1400f/Math.max(d.getPage(i).getMediaBox().getWidth(),d.getPage(i).getMediaBox().getHeight())));PDPage page=new PDPage(new PDRectangle(b.getWidth(),b.getHeight()));out.addPage(page);PDImageXObject image=JPEGFactory.createFromImage(out,b,.65f);try(PDPageContentStream c=new PDPageContentStream(out,page)){c.drawImage(image,0,0,b.getWidth(),b.getHeight());}b.recycle();}out.save(output);}return;}
   if(action.equals("protect")){if(arg.length()<6)throw new Exception("Use at least 6 characters");StandardProtectionPolicy p=new StandardProtectionPolicy(UUID.randomUUID().toString(),arg,new AccessPermission());p.setEncryptionKeyLength(256);d.protect(p);}
   else if(action.equals("unlock")){d.setAllSecurityToBeRemoved(true);}
   else if(action.equals("rotate")){for(PDPage p:d.getPages())p.setRotation((p.getRotation()+90)%360);}
   else if(action.equals("crop")){float margin=Float.parseFloat(arg);if(margin<0||margin>100)throw new Exception("Margin must be 0–100 points");for(PDPage p:d.getPages()){PDRectangle r=p.getCropBox();if(r.getWidth()<=margin*2+20||r.getHeight()<=margin*2+20)throw new Exception("Margin is too large");p.setCropBox(new PDRectangle(r.getLowerLeftX()+margin,r.getLowerLeftY()+margin,r.getWidth()-2*margin,r.getHeight()-2*margin));}}
   else if(action.equals("watermark")||action.equals("numbers")||action.equals("addtext")||action.equals("sign")){
    int i=0;for(PDPage p:d.getPages()){i++;if((action.equals("addtext")||action.equals("sign"))&&i>1)break;String value=action.equals("numbers")?i+" / "+d.getNumberOfPages():arg;
     if(value.length()>100)throw new Exception("Use 100 characters or fewer");
     for(char ch:value.toCharArray())if(ch<32||ch>126)throw new Exception("This PDF stamp currently supports English letters and numbers. Hindi text can be exported using the text editor's PDF tool.");
     PDRectangle r=p.getCropBox();try(PDPageContentStream c=new PDPageContentStream(d,p,PDPageContentStream.AppendMode.APPEND,true,true)){
      c.beginText();c.setFont(action.equals("sign")?PDType1Font.HELVETICA_OBLIQUE:PDType1Font.HELVETICA,action.equals("numbers")?11:18);c.setNonStrokingColor(action.equals("watermark")?150:30);c.newLineAtOffset(r.getLowerLeftX()+24,r.getLowerLeftY()+(action.equals("watermark")?r.getHeight()/2:action.equals("addtext")?r.getHeight()-40:26));c.showText(value);c.endText();}
    }
   }else if(action.equals("flatten")){if(d.getDocumentCatalog().getAcroForm()==null)throw new Exception("No form fields found");d.getDocumentCatalog().getAcroForm().flatten();}
   else if(action.equals("form")){PDAcroForm form=d.getDocumentCatalog().getAcroForm();if(form==null)throw new Exception("No form fields found");int equal=arg.indexOf('=');if(equal<1)throw new Exception("Enter fieldName=value");PDField field=form.getField(arg.substring(0,equal));if(field==null)throw new Exception("Field name not found");field.setValue(arg.substring(equal+1));}
   else if(action.equals("repair")){d.setAllSecurityToBeRemoved(true);}
   else throw new Exception("Unknown tool");
   d.save(output);
  }
 }
 public static String fields(File f,String password)throws Exception{try(PDDocument d=load(f,password)){PDAcroForm form=d.getDocumentCatalog().getAcroForm();if(form==null)return "No form fields found";StringBuilder s=new StringBuilder();for(PDField field:form.getFieldTree())s.append(field.getFullyQualifiedName()).append(" = ").append(field.getValueAsString()).append('\n');return s.toString();}}
 public static void images(List<File> files,File output)throws Exception{try(PDDocument d=new PDDocument()){for(File file:files){android.graphics.BitmapFactory.Options o=new android.graphics.BitmapFactory.Options();o.inJustDecodeBounds=true;android.graphics.BitmapFactory.decodeFile(file.getPath(),o);o.inSampleSize=Math.max(1,Math.max(o.outWidth,o.outHeight)/1800);o.inJustDecodeBounds=false;Bitmap b=android.graphics.BitmapFactory.decodeFile(file.getPath(),o);if(b==null)throw new Exception("Unsupported image");PDPage p=new PDPage(new PDRectangle(b.getWidth(),b.getHeight()));d.addPage(p);try(PDPageContentStream c=new PDPageContentStream(d,p)){c.drawImage(JPEGFactory.createFromImage(d,b,.9f),0,0,b.getWidth(),b.getHeight());}b.recycle();}d.save(output);}}
}
