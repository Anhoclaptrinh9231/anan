import java.util.*;
public class QuickChangesTest {
 static Map<String,List<String>> f(String...v){var p=new LinkedHashMap<String,List<String>>();for(int i=0;i<v.length;i+=2)p.put(v[i],List.of(v[i+1]));return p;}
 static long n(String sql)throws Exception{try(var c=Bookstore.db()){return ((Number)Bookstore.query(c,sql).get(0).get("n")).longValue();}}
 static void ok(boolean b){if(!b)throw new AssertionError();}
 public static void main(String[]a)throws Exception{
  MysqlTests.initialize();
  Bookstore.post("/api/customers",f("edit","1","original","KH01","code","KHNEW","name","Demo","phone","0901111111"));
  ok(n("SELECT COUNT(*) AS n FROM HOA_DON_BAN WHERE MaKH='KHNEW'")==1);
  Bookstore.post("/api/customers/delete",f("code","KHNEW"));
  ok(n("SELECT COUNT(*) AS n FROM KHACH_HANG WHERE MaKH='KHNEW'")==0&&n("SELECT COUNT(*) AS n FROM HOA_DON_BAN WHERE MaHDB='HDB20261001' AND MaKH IS NULL")==1);
  @SuppressWarnings("unchecked") var created=(Map<String,Object>)Bookstore.post("/api/purchases",f("branch","CN01","employee","NV03","supplier","NCC01","date","2026-10-29T12:00","product","S004","quantity","5","price","60000"));
  String id=(String)created.get("code");Bookstore.post("/api/purchases/delete",f("code",id));
  ok(n("SELECT COUNT(*) AS n FROM PHIEU_NHAP WHERE MaPN='"+id+"'")==0&&n("SELECT SoLuongTon AS n FROM TON_KHO WHERE MaCN='CN01' AND MaSach='S004'")==0);
  Bookstore.post("/api/products/delete",f("code","S001"));
  @SuppressWarnings("unchecked") var books=(List<Map<String,Object>>)Bookstore.get("/api/products",f());ok(books.stream().noneMatch(b->b.get("code").equals("S001")));
  System.out.println("PASS: customer code cascade, customer deletion, receipt deletion / stock reversal, removed book disappears.");
 }
}
