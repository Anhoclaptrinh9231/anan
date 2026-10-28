import org.h2.tools.RunScript;
import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

/** Kiem tra tren file CSDL thu rieng. Khong cham vao data/hieusach. */
public class IntegrationTest {
    static int checks=0;
    static void check(boolean ok,String name){if(!ok)throw new AssertionError(name);checks++;System.out.println("PASS "+name);}
    static Map<String,List<String>> form(String...pairs){Map<String,List<String>> p=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)p.computeIfAbsent(pairs[i],k->new ArrayList<>()).add(pairs[i+1]);return p;}
    static List<Map<String,Object>> q(String sql,Object...args)throws Exception{try(Connection c=Bookstore.db()){return Bookstore.query(c,sql,args);}}
    static long scalar(String sql,Object...args)throws Exception{return ((Number)q(sql,args).get(0).values().iterator().next()).longValue();}
    static void reject(String name,String path,Map<String,List<String>> p)throws Exception{try{Bookstore.post(path,p);throw new AssertionError("Should reject: "+name);}catch(IllegalArgumentException|SQLException expected){check(true,name);}}
    static Map<String,List<String>> sale(String product,String qty){return form("employee","NV001","customer","KH001","date","2026-10-28","product",product,"quantity",qty);}
    @SuppressWarnings("unchecked") public static void main(String[]args)throws Exception{
        System.setProperty("bookstore.data","test-output/"+UUID.randomUUID()+"/testdb");
        Files.createDirectories(Bookstore.ROOT.resolve(System.getProperty("bookstore.data")).getParent());
        try(Connection c=Bookstore.db();Reader r=Files.newBufferedReader(Path.of("sql/schema.sql"))){RunScript.execute(c,r);Bookstore.seed(c);}
        check(scalar("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE LOWER(TABLE_SCHEMA)='public' AND TABLE_TYPE='BASE TABLE'")==20,"exactly 20 tables");
        check(scalar("SELECT COUNT(*) FROM HANGHOA")==27,"seed products");
        check(scalar("SELECT SUM(SoLuong*DonGiaBan) FROM BANHANG")==1895060,"seed revenue = 1895060 VND");
        Bookstore.post("/api/products",form("code","TEST_BOOK","name","Sách kiểm thử","price","50000","stock","5","type","SACH","author","Nguyễn Nhật Ánh","genre","Giáo dục","edition","Lần 1,Lần 2"));
        check(scalar("SELECT COUNT(*) FROM SACH WHERE MaHHS='TEST_BOOK'")==1,"book parent and subtype");
        check(scalar("SELECT COUNT(*) FROM SACH_TB WHERE MaHHS='TEST_BOOK'")==2,"multivalued editions separated");
        Bookstore.post("/api/customers",form("code","TEST_C","name","Khách kiểm thử","address","Hà Đông - Hà Nội","gender","Nữ","birth","2004-01-01","phone","0909000001,0909000002"));
        check(scalar("SELECT COUNT(*) FROM KHACHHANG_SDT WHERE MaKH='TEST_C'")==2,"multivalued phones separated");
        reject("duplicate code rejected","/api/customers",form("code","TEST_C","name","Duplicate"));
        reject("invalid phone rejected","/api/customers",form("code","BAD_PHONE","name","Bad","phone","abc"));
        check(scalar("SELECT COUNT(*) FROM KHACHHANG WHERE MaKH='BAD_PHONE'")==0,"customer insert rolls back on phone error");
        reject("missing book author rejected","/api/products",form("code","BAD_BOOK","name","Bad","price","1000","stock","1","type","SACH","genre","A"));
        check(scalar("SELECT COUNT(*) FROM HANGHOA WHERE MaHH='BAD_BOOK'")==0,"parent rolls back on subtype error");
        reject("negative quantity rejected","/api/sales",sale("TEST_BOOK","-1"));
        reject("wrong employee role rejected","/api/sales",form("employee","NV006","customer","KH001","date","2026-10-28","product","TEST_BOOK","quantity","1"));
        reject("invalid customer FK rejected","/api/sales",form("employee","NV001","customer","MISSING","date","2026-10-28","product","TEST_BOOK","quantity","1"));
        long oldInvoices=scalar("SELECT COUNT(*) FROM HOADONBAN"),oldStock=scalar("SELECT SoLuong FROM HANGHOA WHERE MaHH='HD001'");
        reject("multi-line insufficient stock rejected","/api/sales",form("employee","NV001","customer","KH001","date","2026-10-28","product","HD001","quantity","2","product","TEST_BOOK","quantity","999"));
        check(scalar("SELECT COUNT(*) FROM HOADONBAN")==oldInvoices,"failed sale leaves no invoice header");
        check(scalar("SELECT SoLuong FROM HANGHOA WHERE MaHH='HD001'")==oldStock,"earlier line stock rolls back");
        check(scalar("SELECT SoLuong FROM HANGHOA WHERE MaHH='TEST_BOOK'")==5,"shortage leaves stock unchanged");
        reject("duplicate invoice product rejected","/api/sales",form("employee","NV001","customer","KH001","date","2026-10-28","product","TEST_BOOK","quantity","1","product","TEST_BOOK","quantity","1"));
        Map<String,Object> result=(Map<String,Object>)Bookstore.post("/api/sales",form("employee","NV001","customer","TEST_C","date","2026-10-28","product","TEST_BOOK","quantity","2","product","HD001","quantity","1"));
        String id=result.get("code").toString();
        check(scalar("SELECT SoLuong FROM HANGHOA WHERE MaHH='TEST_BOOK'")==3,"sale decrements inventory");
        check(scalar("SELECT COUNT(*) FROM BANHANG WHERE MaHDB=?",id)==2,"one invoice has multiple lines");
        check(scalar("SELECT SUM(SoLuong*DonGiaBan) FROM BANHANG WHERE MaHDB=?",id)==112000,"invoice total correct");
        try(Connection c=Bookstore.db()){Bookstore.update(c,"UPDATE HANGHOA SET Gia=75000 WHERE MaHH='TEST_BOOK'");}
        check(scalar("SELECT DonGiaBan FROM BANHANG WHERE MaHDB=? AND MaHH='TEST_BOOK'",id)==50000,"historical price independent of current price");
        long oldImports=scalar("SELECT COUNT(*) FROM HOADONNHAP");
        reject("branch must belong to supplier","/api/purchases",form("employee","NV006","supplier","CD001","branch","Chi nhánh Hải Phòng","date","2026-10-28","product","TEST_BOOK","quantity","2","price","25000"));
        check(scalar("SELECT COUNT(*) FROM HOADONNHAP")==oldImports,"invalid purchase leaves no header");
        Bookstore.post("/api/purchases",form("employee","NV006","supplier","CD001","branch","Chi nhánh Hưng Yên","date","2026-10-28","product","TEST_BOOK","quantity","4","price","25000"));
        check(scalar("SELECT SoLuong FROM HANGHOA WHERE MaHH='TEST_BOOK'")==7,"purchase increases inventory");
        check(scalar("SELECT Gia FROM HANGHOA WHERE MaHH='TEST_BOOK'")==75000,"purchase does not alter retail price");
        List<?> search=(List<?>)Bookstore.get("/api/products",form("q","Nhật Ánh","type","SACH"));
        check(search.size()==3,"Vietnamese author SQL search");
        check(((List<?>)Bookstore.get("/api/products",form("q","%' OR 1=1 --"))).isEmpty(),"SQL injection remains literal search text");
        check(((List<?>)Bookstore.get("/api/products",form("q","%"))).isEmpty(),"LIKE wildcard escaped");
        for(String kind:List.of("revenue","inventory","customers","bestsellers")){
            Map<String,Object> report=(Map<String,Object>)Bookstore.get("/api/reports",form("kind",kind));
            check(!((List<?>)report.get("rows")).isEmpty(),"report "+kind+" returns rows and SQL");
        }
        // Tie case: two customers both buy one item in a separate region.
        for(String k:List.of("TIE_A","TIE_B")){
            Bookstore.post("/api/customers",form("code",k,"name",k,"address","Vùng đồng hạng"));
            Bookstore.post("/api/sales",form("employee","NV001","customer",k,"date","2026-10-28","product","HD001","quantity","1"));
        }
        Map<String,Object> ties=(Map<String,Object>)Bookstore.get("/api/reports",form("kind","customers","area","Vùng đồng hạng"));
        check(((List<?>)ties.get("rows")).size()==2,"report keeps all tied customers");
        Map<String,Object> noRows=(Map<String,Object>)Bookstore.get("/api/reports",form("kind","revenue","from","2000-01-01","to","2000-12-31"));
        check(((List<?>)noRows.get("rows")).isEmpty(),"empty date range report");
        Bookstore.post("/api/products",form("code","LAST_ONE","name","One left","price","1000","stock","1","type","DUNGCU","category","Bút"));
        try(ExecutorService pool=Executors.newFixedThreadPool(2)){
            CountDownLatch go=new CountDownLatch(1);List<Future<Boolean>> tasks=new ArrayList<>();
            for(int i=0;i<2;i++)tasks.add(pool.submit(()->{go.await();try{Bookstore.post("/api/sales",sale("LAST_ONE","1"));return true;}catch(IllegalArgumentException ex){return false;}}));
            go.countDown();int wins=0;for(Future<Boolean> f:tasks)if(f.get(15,TimeUnit.SECONDS))wins++;
            check(wins==1,"concurrent sales: only one can buy the last unit");
        }
        check(scalar("SELECT SoLuong FROM HANGHOA WHERE MaHH='LAST_ONE'")==0,"stock never becomes negative");
        long invoiceCount=scalar("SELECT COUNT(*) FROM HOADONBAN");
        if(Bookstore.DATABASE_URL.isBlank()) {
        try(Connection c=Bookstore.db()){Bookstore.update(c,"SHUTDOWN");}
        check(scalar("SELECT COUNT(*) FROM HOADONBAN")==invoiceCount,"data survives full database shutdown and reopen");
        check(scalar("SELECT DonGiaBan FROM BANHANG WHERE MaHDB=? AND MaHH='TEST_BOOK'",id)==50000,"historical price persists after reopen");
        try(Connection c=Bookstore.db()){Bookstore.update(c,"SHUTDOWN");}
        }
        System.out.println("ALL "+checks+" CHECKS PASSED. Test DB: "+System.getProperty("bookstore.data"));
    }
}
