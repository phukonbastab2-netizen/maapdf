package in.sahayak.documents;
import java.util.*;
import java.util.regex.*;
public final class IntentParser {
 public static String[] parse(String input) {
  String s=input.trim(); String l=s.toLowerCase(Locale.ROOT);
  Matcher m=Pattern.compile("(?i)^replace (.+?) with (.+)$").matcher(s);
  if(m.matches()) return new String[]{"replace",m.group(1),m.group(2)};
  m=Pattern.compile("^(.+?) (?:ki jagah|की जगह) (.+?)(?: likho| लिखो)?$",Pattern.CASE_INSENSITIVE).matcher(s);
  if(m.matches()) return new String[]{"replace",m.group(1),m.group(2)};
  if(l.matches(".*(next|agla|अगला).*$")) return new String[]{"next"};
  if(l.matches(".*(previous|pichla|पिछला).*$")) return new String[]{"previous"};
  if(l.matches(".*(read|padho|पढ़ो|सुनाओ).*$")) return new String[]{"read"};
  if(l.matches(".*(save|bachao|सेव|सहेज).*$")) return new String[]{"save"};
  if(l.matches(".*(open|kholo|खोलो).*$") && !l.contains("page") && !l.contains("पेज")) return new String[]{"open"};
  m=Pattern.compile("(?:page|पेज)\\s*([0-9०-९]+)",Pattern.CASE_INSENSITIVE).matcher(s);
  if(m.find()) {String n=m.group(1); for(int i=0;i<10;i++)n=n.replace((char)('०'+i),(char)('0'+i)); return new String[]{"page",n};}
  return new String[]{"unknown"};
 }
}
