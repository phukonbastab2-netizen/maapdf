package in.sahayak.documents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.*;import org.junit.runner.RunWith;import static org.junit.Assert.*;
import android.app.Instrumentation;import android.content.Intent;
import java.util.concurrent.atomic.AtomicBoolean;
@RunWith(AndroidJUnit4.class)
public class HtmlPrintTest {
 @Test public void localHtmlPreviewEnablesPrintAndBlocksExternalAccess() throws Exception {
  Instrumentation i=InstrumentationRegistry.getInstrumentation();
  HtmlActivity a=(HtmlActivity)i.startActivitySync(new Intent(i.getTargetContext(),HtmlActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  try {
   i.runOnMainSync(()->{assertFalse(a.web.getSettings().getJavaScriptEnabled());assertFalse(a.web.getSettings().getAllowFileAccess());assertFalse(a.web.getSettings().getAllowContentAccess());assertTrue(a.web.getSettings().getBlockNetworkLoads());a.web.loadDataWithBaseURL(null,"<html><body><h1>Maa HTML test</h1><p>Local document 5000</p></body></html>","text/html","UTF-8",null);});
   AtomicBoolean ready=new AtomicBoolean(false);for(int n=0;n<100&&!ready.get();n++){i.runOnMainSync(()->ready.set(a.print.isEnabled()));Thread.sleep(100);}assertTrue("HTML preview did not load",ready.get());
  } finally {i.runOnMainSync(a::finish);}
 }
}
