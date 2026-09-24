import in.sahayak.documents.IntentParser;
import java.util.Arrays;
public class ParserCheck {
 static void check(String input,String...expected){String[] actual=IntentParser.parse(input);if(!Arrays.equals(actual,expected))throw new AssertionError(input+" => "+Arrays.toString(actual));}
 public static void main(String[]args){
  check("replace Suresh with Ramesh","replace","Suresh","Ramesh");
  check("Suresh ki jagah Ramesh likho","replace","Suresh","Ramesh");
  check("सुरेश की जगह रमेश लिखो","replace","सुरेश","रमेश");
  check("पेज २ खोलो","page","2");
  check("next page","next");check("padho","read");
  check("give Ramesh 5000 rupees","unknown");
  System.out.println("7 Android parser checks passed");
 }
}
