import java.sql.*;
import java.util.*;
public class CatalogActionsTest {
 static Map<String,List<String>> f(String...a){var p=new HashMap<String,List<String>>();for(int i=0;i<a.length;i+=2)p.put(a[i],List.of(a[i+1]));return p;}
 static void ok(boolean v){if(!v)throw new AssertionError();}
 static void post(String path,String...a)throws Exception{Bookstore.post("/api/"+path,f(a));}
 static void reject(String path,String id)throws Exception{try{post(path,"code",id);throw new AssertionError();}catch(IllegalArgumentException expected){}}
 @SuppressWarnings("unchecked") static List<Map<String,Object>> books()throws Exception{return (List<Map<String,Object>>)Bookstore.get("/api/products",f());}
 public static void main(String[]a)throws Exception{
  MysqlTests.initialize();
  post("genres","code","TLTEST","name","Demo");post("genres/delete","code","TLTEST");reject("genres/delete","TL01");
  post("branches","code","CNTEST","name","Demo","address","Demo","phone","123");
  post("employees","code","NVTEST","name","Demo","birth","2000-01-01","phone","124","joined","2026-01-01","branch","CNTEST");
  reject("branches/delete","CNTEST");post("employees/delete","code","NVTEST");post("branches/delete","code","CNTEST");post("employees/delete","code","NV02");
  post("suppliers","code","NCCTEST","name","Demo","address","Demo","phone","125");post("suppliers/delete","code","NCCTEST");reject("suppliers/delete","NCC01");
  post("products/stop","code","S001");ok(books().stream().anyMatch(b->b.get("code").equals("S001")&&b.get("status").equals("Ngừng kinh doanh")));
  post("products/resume","code","S001");post("products/delete","code","S001");ok(books().stream().noneMatch(b->b.get("code").equals("S001")));reject("products/resume","S001");
  try(Connection c=Bookstore.db()){ok(((Number)Bookstore.query(c,"SELECT COUNT(*) AS n FROM HOA_DON_BAN").get(0).get("n")).intValue()==3);}
  System.out.println("PASS: four catalog deletes / reference guards; stop, resume and delete are distinct; short demo phone accepted.");
 }
}
