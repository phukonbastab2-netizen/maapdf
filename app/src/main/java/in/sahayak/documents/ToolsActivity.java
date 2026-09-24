package in.sahayak.documents;

import android.app.*;import android.os.*;import android.content.*;import android.net.Uri;import android.graphics.*;import android.view.*;import android.widget.*;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.rendering.PDFRenderer;
import java.io.*;import java.util.*;

public class ToolsActivity extends Activity {
 LinearLayout screen; TextView guide; File input,output;ArrayList<File> extras=new ArrayList<>();String action="",argument="",password="",mime="application/pdf",outputName="maapdf-result.pdf";boolean busy=false;int revision=0;
 String[][] tools={
 {"merge","Join PDFs / जोड़ें","Choose two or more PDFs. The displayed order is the merge order."},
 {"split","Split each page / अलग करें","Creates a ZIP with one PDF per page."},
 {"extract","Keep selected pages","Enter pages in order: 1,3-5"},
 {"remove","Remove pages","Enter pages to remove: 2,4-6. At least one page must remain."},
 {"reorder","Reorder pages","Enter the full new order, for example 3,1,2. Only listed pages are kept."},
 {"rotate","Rotate right / घुमाएँ","Rotates every page 90 degrees clockwise."},
 {"crop","Crop margins","Hide equal margins on all sides. This is NOT secure redaction. Enter margin in points (0–100)."},
 {"images","Photos → PDF","Select multiple images. Each image becomes a page."},
 {"jpg","PDF → pictures","Creates a ZIP with one JPEG per page."},
 {"text","PDF → text / पढ़ें","Extracts existing text. Scanned images need OCR, which is not included."},
 {"markdown","PDF → Markdown text","Plain reading-order text; complex tables and layout are not reconstructed."},
 {"word","PDF → Word text","Creates an editable DOCX containing extracted text. Original layout and images are not retained."},
 {"excel","PDF → spreadsheet text","Creates XLSX with one text line per row. Does not reconstruct tables or formulas."},
 {"slides","PDF → slide images","One PDF page per PowerPoint slide, as an image. Slide text is not editable."},
 {"compress","Smaller image PDF","Rebuilds pages as compressed images. Text selection, links, forms and signatures are lost. File may not become smaller."},
 {"watermark","Add watermark","Enter a short English text stamp, placed across the middle of every page."},
 {"numbers","Add page numbers","Adds page numbers near the bottom of every page."},
 {"addtext","Add text note","Adds English text near the top of page 1; does not replace existing PDF text."},
 {"sign","Add typed signature","Adds a visible typed name to page 1. Not a certificate-based digital signature or identity verification."},
 {"redact","Black out an area","Permanently rebuilds pages as images. Enter page,x,y,width,height as percentages from top-left; e.g. 1,10,10,60,15. Preview carefully; text outside the area also becomes images."},
 {"protect","Add password / पासवर्ड","Enter a new password of at least 6 characters. It is not saved in settings."},
 {"unlock","Remove known password","Enter the existing password below. No password guessing or cracking."},
 {"fields","Show form fields","Lists existing PDF form field names and values."},
 {"form","Fill a form field","Enter fieldName=value. Use Show form fields to find the exact name."},
 {"flatten","Flatten filled form","Makes filled form values permanent and removes editable fields."},
 {"compare","Compare document text","Choose two PDFs. Compares extracted text lines; not a visual or legal equivalence check."},
 {"repair","Try rebuilding PDF","Resaves a readable PDF. Severely damaged files may not be recoverable."}
 };
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
 TextView text(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(Color.rgb(45,61,49));t.setPadding(dp(6),dp(10),dp(6),dp(10));return t;}
 Button button(String title,Runnable run){Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setTextSize(18);b.setMinHeight(dp(56));b.setTextColor(Color.rgb(38,72,53));b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(232,237,216)));b.setOnClickListener(v->run.run());return b;}
 void say(String s){new AlertDialog.Builder(this).setMessage(s).setPositiveButton("OK / ठीक",null).show();}
 @Override public void onCreate(Bundle b){super.onCreate(b);PDFBoxResourceLoader.init(getApplicationContext());home();}
 void shell(String title){screen=new LinearLayout(this);screen.setOrientation(LinearLayout.VERTICAL);screen.setPadding(dp(16),dp(16),dp(16),dp(16));screen.setBackgroundColor(Color.rgb(255,248,235));ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.addView(screen);setContentView(scroll);screen.addView(button("← Back / वापस",()->{if(!busy)home();}));screen.addView(text(title,26));}
 void home(){action="";shell("What shall we do? / क्या करें?");screen.getChildAt(0).setOnClickListener(v->finish());screen.addView(text("Choose one tool. Maa will guide you step by step.\nFiles stay on your phone.",17));String[] groups={"Arrange pages / पेज लगाएँ","Convert files / बदलें","Write & mark / लिखें","Passwords & forms / सुरक्षा","Check & reduce / जाँचें"};for(int g=0;g<groups.length;g++){final int group=g;screen.addView(button(groups[g],()->group(group,groups[group])));}
 screen.addView(button("About advanced conversions",()->say("This release does not yet include scanned-text OCR, Office-to-PDF layout conversion, HTML-to-PDF, PDF/A validation, translation, AI summaries, or creating new form fields. These need additional engines and testing. maapdf is independent of iLovePDF.")));}
 void group(int index,String title){shell(title);String[][] groups={{"merge","split","extract","remove","reorder","rotate","crop"},{"images","jpg","text","markdown","word","excel","slides"},{"watermark","numbers","addtext","sign","redact"},{"protect","unlock","fields","form","flatten"},{"compare","compress","repair"}};for(String[] tool:tools)if(Arrays.asList(groups[index]).contains(tool[0]))screen.addView(button(tool[1],()->setup(tool)));}
 void setup(String[] tool){action=tool[0];input=null;extras.clear();output=null;argument="";password="";shell(tool[1]);screen.addView(text("1  Choose  →  2  Preview  →  3  Save",17));screen.addView(text(tool[2],17));guide=text("No files chosen yet",16);screen.addView(guide);screen.addView(button("Choose file(s) / फाइल चुनें",()->choose()));
 EditText arg=new EditText(this);arg.setTextSize(19);arg.setHint(action.equals("protect")?"New password":"Tool setting / यहाँ लिखें");if(action.equals("protect"))arg.setInputType(129);
 if(Arrays.asList("extract","remove","reorder","crop","watermark","addtext","sign","protect","form","redact").contains(action)){screen.addView(arg);}
 EditText pass=new EditText(this);pass.setTextSize(18);pass.setInputType(129);pass.setHint("Existing PDF password, if any");if(!action.equals("images"))screen.addView(pass);
 screen.addView(button("Make preview / देखें",()->{argument=arg.getText().toString().trim();password=pass.getText().toString();process();}));}
 void choose(){if(busy)return;Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType(action.equals("images")?"image/*":"application/pdf");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,Arrays.asList("merge","compare","images").contains(action));startActivityForResult(i,10);}
 @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(result!=RESULT_OK||data==null)return;
 if(req==11){Uri dest=data.getData();if(dest==null||output==null)return;File resultFile=output;busy=true;new Thread(()->{try(InputStream in=new FileInputStream(resultFile);OutputStream out=getContentResolver().openOutputStream(dest,"w")){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);runOnUiThread(()->{busy=false;say("Saved / सहेज दिया। Your source files are unchanged.");});}catch(Exception e){runOnUiThread(()->{busy=false;say("Save failed: "+e.getMessage());});}}).start();return;}
 if(req!=10)return;try{ArrayList<Uri> uris=new ArrayList<>();if(data.getClipData()!=null){for(int i=0;i<data.getClipData().getItemCount();i++)uris.add(data.getClipData().getItemAt(i).getUri());}else if(data.getData()!=null)uris.add(data.getData());if(uris.size()>20)throw new Exception("Choose 20 files or fewer");input=null;extras.clear();StringBuilder names=new StringBuilder();int number=0;for(Uri uri:uris){File f=new File(getCacheDir(),"tool-"+UUID.randomUUID()+".input");try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(f)){byte[] bytes=new byte[8192];int count,total=0;while((count=in.read(bytes))!=-1){total+=count;if(total>30*1024*1024)throw new Exception("Use files under 30 MB");out.write(bytes,0,count);}}if(input==null)input=f;else extras.add(f);names.append(++number).append(". ").append(displayName(uri)).append('\n');}guide.setText(names.toString());}catch(Exception e){input=null;extras.clear();say(e.getMessage());}}
 String displayName(Uri uri){try(android.database.Cursor c=getContentResolver().query(uri,null,null,null,null)){if(c!=null&&c.moveToFirst())return c.getString(c.getColumnIndexOrThrow(android.provider.OpenableColumns.DISPLAY_NAME));}catch(Exception ignored){}return "Selected file";}
 void process(){if(busy)return;if(input==null){say("Choose a file first / पहले फाइल चुनें");return;}if((action.equals("merge")||action.equals("compare"))&&extras.isEmpty()){say("Select at least two PDFs together in the file picker.");return;}if(action.equals("compare")&&extras.size()!=1){say("Choose exactly two PDFs for comparison.");return;}busy=true;guide.setText("Maa is preparing your preview… / कृपया रुकें");final String job=action;new Thread(()->{try{
  String extension="pdf";mime="application/pdf";
  if(job.equals("jpg")||job.equals("split")){extension="zip";mime="application/zip";}
  if(Arrays.asList("text","fields","compare","markdown").contains(job)){extension=job.equals("markdown")?"md":"txt";mime="text/plain";}
  if(job.equals("word")){extension="docx";mime="application/vnd.openxmlformats-officedocument.wordprocessingml.document";}
  if(job.equals("excel")){extension="xlsx";mime="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";}
  if(job.equals("slides")){extension="pptx";mime="application/vnd.openxmlformats-officedocument.presentationml.presentation";}
  outputName="maapdf-"+job+"."+extension;output=new File(getCacheDir(),UUID.randomUUID()+"."+extension);String content=null;
  if(job.equals("images")){ArrayList<File> all=new ArrayList<>();all.add(input);all.addAll(extras);PdfEngine.images(all,output);}
  else if(job.equals("fields")){content=PdfEngine.fields(input,password);}
  else if(job.equals("compare")){String a=PdfEngine.text(input,password),b=PdfEngine.text(extras.get(0),password);content=PortableExports.compare(a,b);}
  else if(Arrays.asList("text","markdown","word","excel").contains(job)){content=PdfEngine.text(input,password);if(content.trim().isEmpty())throw new Exception("No selectable text found. This may be a scan; OCR is not yet included.");if(job.equals("word")){PortableExports.word(content,output);content=null;}else if(job.equals("excel")){PortableExports.excel(content,output);content=null;}}
  else if(job.equals("slides")){PortableExports.slides(input,password,output);}
  else if(job.equals("redact")){PortableExports.redact(input,password,argument,output);}
  else PdfEngine.run(job,input,extras,output,argument,password);
  if(content!=null)try(FileOutputStream f=new FileOutputStream(output)){f.write(content.getBytes("UTF-8"));}
  Bitmap preview=null;if(extension.equals("pdf")){try(PDDocument d=PDDocument.load(output,job.equals("protect")?argument:"")){int previewPage=job.equals("redact")?Integer.parseInt(argument.split(",")[0].trim())-1:0;preview=new PDFRenderer(d).renderImage(previewPage,Math.min(1f,900f/Math.max(d.getPage(0).getMediaBox().getWidth(),d.getPage(0).getMediaBox().getHeight())));}}
  final Bitmap image=preview;final String textPreview=content;
  runOnUiThread(()->{busy=false;shell("Review your result / परिणाम देखें");screen.addView(text(outputName+" • "+output.length()/1024+" KB",17));if(job.equals("compress"))screen.addView(text("Original: "+input.length()/1024+" KB. Smaller: "+(output.length()<input.length()?"yes":"no"),16));if(image!=null){ImageView v=new ImageView(this);v.setAdjustViewBounds(true);v.setImageBitmap(image);v.setContentDescription("First page preview");screen.addView(v);}if(textPreview!=null)screen.addView(text(textPreview.substring(0,Math.min(12000,textPreview.length())),16));screen.addView(text("Preview shows first page or first 12,000 text characters. Review the exported file before use. Your source remains unchanged.",15));screen.addView(button("Confirm & save copy / सहेजें",()->{Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType(mime);i.putExtra(Intent.EXTRA_TITLE,outputName);startActivityForResult(i,11);}));screen.addView(button("Discard result",()->home()));});
 }catch(Exception e){runOnUiThread(()->{busy=false;guide.setText("Please check your files and settings.");say("Could not complete: "+e.getMessage());});}}).start();}
 @Override public void onBackPressed(){if(busy){say("Please wait for this job to finish.");return;}if(action.isEmpty())finish();else home();}
}
