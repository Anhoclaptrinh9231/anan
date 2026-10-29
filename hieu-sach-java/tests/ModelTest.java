import java.nio.file.*;
import java.sql.*;
import java.util.*;

public class ModelTest {
 static int checks;
 static Map<String,List<String>> f(String...v){Map<String,List<String>> p=new LinkedHashMap<>();for(int i=0;i<v.length;i+=2)p.computeIfAbsent(v[i],k->new ArrayList<>()).add(v[i+1]);return p;}
 static void ok(boolean b,String message){if(!b)throw new AssertionError(message);checks++;System.out.println("PASS "+message);}
 static long number(String sql,Object...args)throws Exception{try(Connection c=Bookstore.db()){return ((Number)Bookstore.query(c,sql,args).get(0).get("n")).longValue();}}
 static void reject(String path,Map<String,List<String>> p)throws Exception{try{Bookstore.post(path,p);throw new AssertionError("accepted invalid action: "+path);}catch(IllegalArgumentException|SQLException expected){checks++;}}
 @SuppressWarnings("unchecked") static String create(String path,Map<String,List<String>> p)throws Exception{return String.valueOf(((Map<String,Object>)Bookstore.post(path,p)).get("code"));}
 static Map<String,List<String>> purchase(String status){return f("branch","CN01","employee","NV03","supplier","NCC02","date","2026-10-29T12:00","status",status,"product","S001","quantity","5","price","90000");}
 public static void main(String[]a)throws Exception{
  if(System.getenv("DATABASE_URL")==null){System.setProperty("bookstore.data","test-output/model-"+UUID.randomUUID()+"/db");Files.createDirectories(Bookstore.ROOT.resolve(System.getProperty("bookstore.data")).getParent());}
  try(Connection c=Bookstore.rawDatabase()){Bookstore.update(c,"CREATE TABLE IF NOT EXISTS LEGACY_SENTINEL(Id INT PRIMARY KEY)");}
  Bookstore.initialize();
  ok(number("SELECT COUNT(*) AS n FROM SACH")==4&&number("SELECT COUNT(*) AS n FROM CHI_NHANH")==2,"new 11-table model and specified sample data initialized");
  try(Connection c=Bookstore.rawDatabase()){ok(Bookstore.query(c,"SELECT COUNT(*) AS n FROM LEGACY_SENTINEL").size()==1,"legacy schema remains intact");}
  ok(number("SELECT SoLuongTon AS n FROM TON_KHO WHERE MaCN='CN01' AND MaSach='S001'")==47,"branch-specific initial inventory matches specification");
  String pending=create("/api/purchases",purchase("Chờ xác nhận"));
  ok(number("SELECT SoLuongTon AS n FROM TON_KHO WHERE MaCN='CN01' AND MaSach='S001'")==47,"pending purchase does not add stock");
  Bookstore.post("/api/purchases/confirm",f("code",pending));
  ok(number("SELECT SoLuongTon AS n FROM TON_KHO WHERE MaCN='CN01' AND MaSach='S001'")==52,"confirmed purchase adds stock at receiving branch");
  Bookstore.post("/api/purchases/delete",f("code",pending));
  ok(number("SELECT SoLuongTon AS n FROM TON_KHO WHERE MaCN='CN01' AND MaSach='S001'")==47&&number("SELECT COUNT(*) AS n FROM PHIEU_NHAP WHERE MaPN=? AND TrangThai='Đã hủy'",pending)==1,"delete purchase reverses stock and retains cancelled document");
  reject("/api/purchases/delete",f("code",pending));
  Bookstore.post("/api/purchases/restore",f("code",pending));
  ok(number("SELECT SoLuongTon AS n FROM TON_KHO WHERE MaCN='CN01' AND MaSach='S001'")==47,"restoring purchase leaves stock unchanged until confirmed");
  Bookstore.post("/api/purchases/delete",f("code",pending));
  String sale=create("/api/sales",f("branch","CN01","employee","NV02","date","2026-10-29T13:00","product","S001","quantity","2","price","120000.50","payment","Chuyển khoản"));
  ok(number("SELECT SoLuongTon AS n FROM TON_KHO WHERE MaCN='CN01' AND MaSach='S001'")==45&&number("SELECT SoLuongTon AS n FROM TON_KHO WHERE MaCN='CN02' AND MaSach='S001'")==30,"sale only reduces selected branch");
  ok(number("SELECT COUNT(*) AS n FROM HOA_DON_BAN WHERE MaHDB=? AND MaKH IS NULL",sale)==1,"walk-in customer uses nullable customer key");
  Bookstore.post("/api/products",f("edit","1","code","S001","name","Bài giảng Cơ sở dữ liệu","author","Nguyễn Quỳnh Chi","publisher","NXB Thông tin và Truyền thông","year","2024","price","130000","genre","TL01","isbn","978-604-80-1001"));
  ok(number("SELECT COUNT(*) AS n FROM CT_HOA_DON_BAN WHERE MaHDB=? AND DonGiaBanThucTe=120000.50",sale)==1,"editing listed price preserves historical actual price");
  Bookstore.post("/api/sales/cancel",f("code",sale));
  ok(number("SELECT SoLuongTon AS n FROM TON_KHO WHERE MaCN='CN01' AND MaSach='S001'")==47,"cancelling completed sale returns stock");
  reject("/api/purchases/delete",f("code","PN20261001"));
  ok(number("SELECT SoLuongTon AS n FROM TON_KHO WHERE MaCN='CN01' AND MaSach='S001'")==47&&number("SELECT COUNT(*) AS n FROM PHIEU_NHAP WHERE MaPN='PN20261001' AND TrangThai='Đã xác nhận'")==1,"insufficient stock rolls back entire purchase cancellation");
  reject("/api/sales",f("branch","CN01","employee","NV02","date","2026-10-29","product","S001","quantity","1","price","100000","product","S002","quantity","999","price","150000"));
  ok(number("SELECT SoLuongTon AS n FROM TON_KHO WHERE MaCN='CN01' AND MaSach='S001'")==47,"multi-line oversell rolls back earlier stock change");
  reject("/api/sales",f("branch","CN02","employee","NV02","date","2026-10-29","product","S001","quantity","1","price","1"));
  try(Connection c=Bookstore.db()){
   @SuppressWarnings("unchecked") Map<String,Object> monthly=(Map<String,Object>)Bookstore.monthly(c,f("year","2026"));
   @SuppressWarnings("unchecked") List<Map<String,Object>> months=(List<Map<String,Object>>)monthly.get("months");
   ok(months.size()==12&&((Number)months.get(9).get("quantity")).intValue()==18&&((Number)months.get(0).get("revenue")).intValue()==0,"monthly chart includes zero months and aggregates completed sales only");
   ok(Bookstore.json(Bookstore.monthly(c,f("year","2026","branch","CN02"))).contains("380000.00"),"monthly chart filters the selected branch");
   ok(Bookstore.books(c,f("q","DỮ LIỆU","branch","CN01","min","120000","max","200000"),true).size()==2,"Vietnamese search plus price and branch filters");
   @SuppressWarnings("unchecked") Map<String,Object> report=(Map<String,Object>)Bookstore.report(c,f("kind","revenue","from","2026-10-01","to","2026-10-29"));
   ok(Bookstore.json(report).contains("1855000.00")&&Bookstore.json(report).contains("380000.00"),"revenue report only includes completed sales, without join multiplication");
   ok(Bookstore.json(Bookstore.report(c,f("kind","inventory"))).contains("S002"),"inventory and bestseller report queries execute");
  }
  Bookstore.post("/api/products/delete",f("code","S001"));
  ok(number("SELECT COUNT(*) AS n FROM SACH WHERE MaSach='S001' AND TrangThai='Ngừng kinh doanh'")==1,"stopping a book preserves historical references");
  reject("/api/sales",f("branch","CN01","employee","NV02","date","2026-10-29","product","S001","quantity","1","price","1"));
  System.out.println(checks+" critical checks passed.");
 }
}
