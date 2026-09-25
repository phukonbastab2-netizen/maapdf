package in.sahayak.documents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.*;import org.junit.runner.RunWith;import static org.junit.Assert.*;
import android.content.Context;import android.graphics.*;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.*;import com.tom_roush.pdfbox.pdmodel.common.PDRectangle;import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory;
import java.io.*;import java.util.*;

@RunWith(AndroidJUnit4.class)
public class IntelligenceTest {
 Context context;File dir;
 @Before public void setup(){context=InstrumentationRegistry.getInstrumentation().getTargetContext();PDFBoxResourceLoader.init(context);dir=new File(context.getCacheDir(),"intelligence-tests");dir.mkdirs();}
 Bitmap page(String text){Bitmap b=Bitmap.createBitmap(1200,600,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);c.drawColor(Color.WHITE);Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);p.setColor(Color.BLACK);p.setTextSize(54);c.drawText(text,70,170,p);return b;}
 File scan(String text)throws Exception{File f=new File(dir,"scan.pdf");Bitmap b=page(text);try(PDDocument d=new PDDocument()){PDPage p=new PDPage(new PDRectangle(600,300));d.addPage(p);try(PDPageContentStream c=new PDPageContentStream(d,p)){c.drawImage(JPEGFactory.createFromImage(d,b,.95f),0,0,600,300);}d.save(f);}b.recycle();return f;}
 @Test public void englishOcrAndSearchLayer()throws Exception{File input=scan("Invoice total 5000 rupees");assertTrue(PdfEngine.text(input,"").trim().isEmpty());File out=new File(dir,"searchable.pdf");String text=DocumentIntelligence.ocr(context,input,"","en",out,s->{});assertTrue(text,text.contains("5000"));assertTrue(PdfEngine.text(out,""),PdfEngine.text(out,"").contains("5000"));try(PDDocument d=PDDocument.load(out)){assertEquals(1,d.getNumberOfPages());assertEquals(600,d.getPage(0).getMediaBox().getWidth(),1);}}
 @Test public void hindiOcr()throws Exception{Bitmap b=page("नमस्ते भारत ५०००");try{String text=DocumentIntelligence.recognize(b,true);assertTrue(text,text.contains("भारत"));}finally{b.recycle();}}
 @Test public void summaryUsesSourceSentences(){String summary=DocumentIntelligence.summarize("Employees submit reports every Monday. Managers review reports on Tuesday. Approved reports are archived. The office opens at nine. Reports must include receipts. Missing receipts delay approval.");assertTrue(summary.contains("Automatic key-sentence summary"));assertTrue(summary.contains("reports"));assertFalse(summary.contains("Friday"));}
 @Test public void newFormFieldCanBeFilled()throws Exception{File input=scan("Form example");File form=new File(dir,"form.pdf");DocumentIntelligence.createField(input,"","EmployeeName",form);assertTrue(PdfEngine.fields(form,"").contains("EmployeeName"));File filled=new File(dir,"filled.pdf");PdfEngine.run("form",form,Collections.emptyList(),filled,"EmployeeName=Ramesh","");assertTrue(PdfEngine.fields(filled,"").contains("Ramesh"));}
 @Test public void translationDownloadsAndRuns()throws Exception{String result=DocumentIntelligence.translate("Hello. This is a document.","en>hi",s->{});assertTrue(result,result.matches("(?s).*[\\u0900-\\u097F].*"));assertTrue(result.contains("Powered by Google Translate"));}
 @Test public void blankScanDoesNotClaimSuccessfulOcr()throws Exception{File input=scan("");try{DocumentIntelligence.ocr(context,input,"","en",new File(dir,"blank.pdf"),s->{});fail("Blank scan reported success");}catch(Exception e){assertTrue(e.getMessage(),e.getMessage().contains("No text recognized"));}}
 @Test public void savedWorkflowRunsAllSteps()throws Exception{android.app.Instrumentation instrumentation=InstrumentationRegistry.getInstrumentation();ToolsActivity activity=(ToolsActivity)instrumentation.startActivitySync(new android.content.Intent(context,ToolsActivity.class).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));try{activity.input=scan("Workflow example");activity.output=new File(dir,"workflow.pdf");activity.argument="rotate,numbers,watermark";activity.password="";activity.runWorkflow();try(PDDocument d=PDDocument.load(activity.output)){assertEquals(90,d.getPage(0).getRotation());}String text=PdfEngine.text(activity.output,"");assertTrue(text,text.replaceAll("\\s+", "").contains("DRAFT"));assertTrue(text,text.replaceAll("\\s+", "").contains("1/1"));}finally{instrumentation.runOnMainSync(activity::finish);}}
}
