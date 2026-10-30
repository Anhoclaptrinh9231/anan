import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.h2.tools.RunScript;
import java.io.*;
import java.math.BigDecimal;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Executors;

/** Java 21 + JDBC. Khong can Node, Maven hay may chu CSDL rieng. */
public class Bookstore {
    static final Path ROOT = Path.of(System.getProperty("bookstore.root", ".")).toAbsolutePath().normalize();
    static final String URL = "jdbc:h2:file:" + ROOT.resolve(System.getProperty("bookstore.data", "data/hieusach")).toString().replace('\\','/') + ";DB_CLOSE_DELAY=-1";
    static final String DATABASE_URL = System.getenv().getOrDefault("DATABASE_URL", "");
    static final boolean PUBLIC_DEMO = Boolean.parseBoolean(System.getenv().getOrDefault("BOOKSTORE_PUBLIC", "false"));
    static final List<String> TABLES = List.of("CHI_NHANH","NHAN_VIEN","KHACH_HANG","THE_LOAI","SACH","NHA_CUNG_CAP","PHIEU_NHAP","CT_PHIEU_NHAP","HOA_DON_BAN","CT_HOA_DON_BAN","TON_KHO");
    static Connection rawDatabase() throws SQLException {
        if (DATABASE_URL.isBlank()) return DriverManager.getConnection(URL, "sa", "");
        try {
            URI u=URI.create(DATABASE_URL);
            if(!Set.of("postgres","postgresql").contains(u.getScheme())||u.getHost()==null||u.getRawUserInfo()==null)
                throw new IllegalArgumentException("DATABASE_URL phải là địa chỉ kết nối PostgreSQL.");
            String[] account=u.getRawUserInfo().split(":",2);
            Properties props=new Properties();
            props.setProperty("user",URLDecoder.decode(account[0].replace("+","%2B"),StandardCharsets.UTF_8));
            props.setProperty("password",account.length>1?URLDecoder.decode(account[1].replace("+","%2B"),StandardCharsets.UTF_8):"");
            props.setProperty("connectTimeout","15");props.setProperty("socketTimeout","45");
            String jdbc="jdbc:postgresql://"+u.getHost()+(u.getPort()<0?"":":"+u.getPort())+u.getRawPath()+(u.getRawQuery()==null?"":"?"+u.getRawQuery());
            return DriverManager.getConnection(jdbc,props);
        } catch(IllegalArgumentException ex) { throw new SQLException("Cấu hình DATABASE_URL không hợp lệ. Kiểm tra biến môi trường trên máy chủ.",ex); }
    }

    static Connection db() throws SQLException {
        Connection c=rawDatabase();
        try(Statement s=c.createStatement()) { c.setSchema(DATABASE_URL.isBlank()?"BOOKSTORE_V2":"bookstore_v2"); }
        catch(SQLException ex) { c.close();throw ex; }
        return c;
    }
    static void initialize() throws Exception {
        try(Connection c=rawDatabase();Statement s=c.createStatement()) {
            s.execute("CREATE SCHEMA IF NOT EXISTS bookstore_v2");
            c.setSchema(DATABASE_URL.isBlank()?"BOOKSTORE_V2":"bookstore_v2");
            try(Reader r=Files.newBufferedReader(ROOT.resolve("sql/schema.sql"))) {RunScript.execute(c,r);}
            s.execute("ALTER TABLE KHACH_HANG ADD COLUMN IF NOT EXISTS NgaySinh DATE");
            boolean bookDeleteColumn=false;
            try(ResultSet cols=c.getMetaData().getColumns(null,c.getSchema(),DATABASE_URL.isBlank()?"SACH":"sach",null)) {
                while(cols.next())if("DaXoa".equalsIgnoreCase(cols.getString("COLUMN_NAME")))bookDeleteColumn=true;
            }
            if(!bookDeleteColumn) {
                s.execute("ALTER TABLE SACH ADD COLUMN DaXoa BOOLEAN NOT NULL DEFAULT FALSE");
                s.execute("UPDATE SACH SET DaXoa=TRUE WHERE TrangThai='Ngừng kinh doanh'");
            }
            boolean customerCascade=false;
            try(ResultSet keys=c.getMetaData().getImportedKeys(null,c.getSchema(),DATABASE_URL.isBlank()?"HOA_DON_BAN":"hoa_don_ban")) {
                while(keys.next())if("FK_HDB_KHACHHANG".equalsIgnoreCase(keys.getString("FK_NAME"))) customerCascade=keys.getShort("UPDATE_RULE")==DatabaseMetaData.importedKeyCascade&&keys.getShort("DELETE_RULE")==DatabaseMetaData.importedKeySetNull;
            }
            if(!customerCascade) {
                s.execute("ALTER TABLE HOA_DON_BAN DROP CONSTRAINT FK_HDB_KHACHHANG");
                s.execute("ALTER TABLE HOA_DON_BAN ADD CONSTRAINT FK_HDB_KHACHHANG FOREIGN KEY(MaKH) REFERENCES KHACH_HANG(MaKH) ON UPDATE CASCADE ON DELETE SET NULL");
            }
            if(((Number)query(c,"SELECT COUNT(*) AS n FROM CHI_NHANH").get(0).get("n")).intValue()==0) seed(c);
        }
    }

    public static void main(String[] args) throws Exception {
        if(PUBLIC_DEMO&&DATABASE_URL.isBlank()) throw new IllegalArgumentException("Bản công khai cần DATABASE_URL PostgreSQL để dữ liệu không bị mất khi máy chủ khởi động lại.");
        if(DATABASE_URL.isBlank()) Files.createDirectories(ROOT.resolve(System.getProperty("bookstore.data", "data/hieusach")).getParent());
        initialize();
        int port = Integer.parseInt(System.getProperty("bookstore.port", System.getenv().getOrDefault("PORT","8080")));
        String bind=System.getProperty("bookstore.bind",System.getenv().getOrDefault("BOOKSTORE_BIND","127.0.0.1"));
        HttpServer server=HttpServer.create(new InetSocketAddress(bind,port),0);
        server.createContext("/", Bookstore::handle);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
        System.out.println("Hieu sach Tri Thuc - http://localhost:"+port);
        System.out.println("CSDL: "+(DATABASE_URL.isBlank()?"H2 lưu tại máy":"PostgreSQL ngoài máy chủ web")+" | Dung: Ctrl+C");
    }

    static void handle(HttpExchange x) throws IOException {
        try {
            String path=x.getRequestURI().getPath();
            if(path.startsWith("/api/")) {
                if(!Set.of("GET","POST").contains(x.getRequestMethod())) { send(x,405,Map.of("error","Phương thức không được hỗ trợ.")); return; }
                if(x.getRequestMethod().equals("POST")) {
                    // Chi chap nhan form cung origin, khong cap CORS cho website khac.
                    String origin=x.getRequestHeaders().getFirst("Origin");
                    String host=x.getRequestHeaders().getFirst("Host");
                    String publicOrigin=System.getenv().getOrDefault("PUBLIC_ORIGIN",System.getenv().getOrDefault("RENDER_EXTERNAL_URL",""));
                    if(origin!=null&&!origin.equals("http://"+host)&&!origin.equals("https://"+host)&&!origin.equals(publicOrigin)) throw new IllegalArgumentException("Nguồn yêu cầu không hợp lệ.");
                    if(!Optional.ofNullable(x.getRequestHeaders().getFirst("Content-Type")).orElse("").startsWith("application/x-www-form-urlencoded")) throw new IllegalArgumentException("Định dạng form không hợp lệ.");
                    byte[] body=x.getRequestBody().readNBytes(65537);
                    if(body.length>65536) throw new IllegalArgumentException("Form quá lớn.");
                    send(x,201,post(path,params(new String(body,StandardCharsets.UTF_8))));
                } else send(x,200,get(path,params(x.getRequestURI().getRawQuery())));
                return;
            }
            if(!x.getRequestMethod().equals("GET")) { send(x,405,Map.of("error","Chỉ hỗ trợ GET.")); return; }
            if(path.startsWith("/sql/")) {
                String name=path.substring(5);
                if(!Set.of("schema.sql","queries.sql","data.sql","tham-khao-7-cau.sql").contains(name)) { send(x,404,Map.of("error","Không tìm thấy.")); return; }
                file(x,ROOT.resolve("sql").resolve(name),"text/plain; charset=utf-8"); return;
            }
            Path f=ROOT.resolve("web").resolve(path.equals("/")?"index.html":path.substring(1)).normalize();
            if(!f.startsWith(ROOT.resolve("web"))||!Files.isRegularFile(f)) { send(x,404,Map.of("error","Không tìm thấy trang.")); return; }
            file(x,f,f.toString().endsWith(".css")?"text/css; charset=utf-8":f.toString().endsWith(".js")?"text/javascript; charset=utf-8":f.toString().endsWith(".svg")?"image/svg+xml":"text/html; charset=utf-8");
        } catch(IllegalArgumentException ex) { send(x,400,Map.of("error",ex.getMessage()==null?"Dữ liệu không hợp lệ.":ex.getMessage())); }
        catch(SQLException ex) {
            String state=ex.getSQLState();
            String message="Không thể lưu dữ liệu. Vui lòng kiểm tra lại.";
            if("23505".equals(state)) message="Mã, ISBN, tên thể loại hoặc số điện thoại đã tồn tại. Vui lòng kiểm tra lại.";
            else if("23503".equals(state)||"23506".equals(state)) message="Khách hàng, nhân viên, hàng hóa hoặc chi nhánh không tồn tại.";
            else if("23513".equals(state)) message="Dữ liệu vi phạm ràng buộc của CSDL.";
            else if(state!=null&&state.startsWith("22")) message="Dữ liệu quá dài hoặc sai định dạng.";
            else ex.printStackTrace();
            send(x,400,Map.of("error",message));
        } catch(Exception ex) { ex.printStackTrace(); send(x,500,Map.of("error","Có lỗi hệ thống. Xem cửa sổ Java để biết chi tiết.")); }
        finally { x.close(); }
    }

    static Object get(String path,Map<String,List<String>> p) throws Exception {
        try(Connection c=db()) {
            return switch(path) {
                case "/api/health" -> Map.of("status","ok","application","tri-thuc-bookstore","model","11-tables-v2","database",DATABASE_URL.isBlank()?"H2":"PostgreSQL","publicDemo",PUBLIC_DEMO);
                case "/api/dashboard" -> {
                    Map<String,Object> out=new LinkedHashMap<>();
                    out.put("metrics",query(c,"SELECT (SELECT COUNT(*) FROM SACH WHERE DaXoa=FALSE) AS products,(SELECT COUNT(*) FROM KHACH_HANG) AS customers,(SELECT COUNT(*) FROM HOA_DON_BAN WHERE TrangThai='Hoàn tất') AS invoices,(SELECT COALESCE(SUM(d.SoLuongBan*d.DonGiaBanThucTe),0) FROM CT_HOA_DON_BAN d JOIN HOA_DON_BAN h ON h.MaHDB=d.MaHDB WHERE h.TrangThai='Hoàn tất') AS revenue,(SELECT COUNT(*) FROM TON_KHO t JOIN SACH s ON s.MaSach=t.MaSach WHERE t.SoLuongTon<=5 AND s.TrangThai='Đang kinh doanh') AS lowstock").get(0));
                    out.put("recent",sales(c));
                    out.put("popular",query(c,"SELECT s.MaSach AS code,s.TenSach AS name,SUM(d.SoLuongBan) AS quantity FROM SACH s JOIN CT_HOA_DON_BAN d ON d.MaSach=s.MaSach JOIN HOA_DON_BAN h ON h.MaHDB=d.MaHDB WHERE h.TrangThai='Hoàn tất' GROUP BY s.MaSach,s.TenSach ORDER BY quantity DESC,s.MaSach FETCH FIRST 5 ROWS ONLY"));
                    yield out;
                }
                case "/api/products" -> books(c,p,false);
                case "/api/monthly" -> monthly(c,p);
                case "/api/search" -> books(c,p,true);
                case "/api/branches" -> query(c,"SELECT MaCN AS code,TenCN AS name,DiaChi AS address,SoDienThoai AS phone,TrangThai AS status FROM CHI_NHANH ORDER BY MaCN");
                case "/api/genres" -> query(c,"SELECT MaTL AS code,TenTheLoai AS name,MoTa AS description FROM THE_LOAI ORDER BY TenTheLoai");
                case "/api/customers" -> query(c,"SELECT MaKH AS code,HoTen AS name,SoDienThoai AS phone,Email AS email,DiaChi AS address,NgayDangKy AS registered,NgaySinh AS birth FROM KHACH_HANG ORDER BY MaKH");
                case "/api/employees" -> query(c,"SELECT n.MaNV AS code,n.HoTen AS name,n.NgaySinh AS birth,n.GioiTinh AS gender,n.SoDienThoai AS phone,n.ChucVu AS role,n.NgayVaoLam AS joined,n.TrangThai AS status,n.MaCN AS branchcode,c.TenCN AS branch FROM NHAN_VIEN n JOIN CHI_NHANH c ON c.MaCN=n.MaCN ORDER BY n.MaNV");
                case "/api/suppliers" -> query(c,"SELECT MaNCC AS code,TenNCC AS name,DiaChi AS address,SoDienThoai AS phone,Email AS email FROM NHA_CUNG_CAP ORDER BY MaNCC");
                case "/api/sales" -> sales(c);
                case "/api/purchases" -> query(c,"SELECT p.MaPN AS code,p.NgayNhap AS date,p.TrangThai AS status,n.TenNCC AS supplier,c.TenCN AS branch,v.HoTen AS employee,SUM(d.SoLuongNhap*d.DonGiaNhap) AS amount FROM PHIEU_NHAP p JOIN NHA_CUNG_CAP n ON n.MaNCC=p.MaNCC JOIN CHI_NHANH c ON c.MaCN=p.MaCN JOIN NHAN_VIEN v ON v.MaNV=p.MaNV JOIN CT_PHIEU_NHAP d ON d.MaPN=p.MaPN WHERE p.TrangThai<>'Đã hủy' GROUP BY p.MaPN,p.NgayNhap,p.TrangThai,n.TenNCC,c.TenCN,v.HoTen ORDER BY p.NgayNhap DESC,p.MaPN DESC");
                case "/api/invoice" -> invoice(c,required(p,"code"),val(p,"type","sale").equals("purchase"));
                case "/api/reports" -> report(c,p);
                default -> throw new IllegalArgumentException("Đường dẫn API không tồn tại.");
            };
        }
    }

    static Object monthly(Connection c,Map<String,List<String>> p) throws Exception {
        int year=integer(val(p,"year",String.valueOf(LocalDate.now().getYear())),1900,9998);
        String branch=val(p,"branch","");
        String sql="SELECT EXTRACT(MONTH FROM h.NgayBan) AS \"month\",COUNT(DISTINCT h.MaHDB) AS invoices,SUM(d.SoLuongBan) AS quantity,SUM(d.SoLuongBan*d.DonGiaBanThucTe) AS revenue FROM HOA_DON_BAN h JOIN CT_HOA_DON_BAN d ON d.MaHDB=h.MaHDB WHERE h.TrangThai='Hoàn tất' AND h.NgayBan>=? AND h.NgayBan<? AND (?='' OR h.MaCN=?) GROUP BY EXTRACT(MONTH FROM h.NgayBan) ORDER BY EXTRACT(MONTH FROM h.NgayBan)";
        List<Object> args=List.of(LocalDate.of(year,1,1).atStartOfDay(),LocalDate.of(year+1,1,1).atStartOfDay(),branch,branch);
        List<Map<String,Object>> data=query(c,sql,args.toArray()),months=new ArrayList<>();
        for(int m=1;m<=12;m++) {
            Map<String,Object> row=new LinkedHashMap<>();row.put("month",m);row.put("invoices",0);row.put("quantity",0);row.put("revenue",BigDecimal.ZERO);
            for(Map<String,Object> item:data)if(((Number)item.get("month")).intValue()==m){row.putAll(item);break;}
            months.add(row);
        }
        return Map.of("year",year,"months",months,"sql",sql,"parameters",args);
    }

    static List<Map<String,Object>> books(Connection c,Map<String,List<String>> p,boolean search) throws Exception {
        String term="%"+escapeLike(val(p,"q","").toLowerCase(Locale.ROOT))+"%",branch=val(p,"branch","");
        String sql="SELECT s.MaSach AS code,s.ISBN AS isbn,s.TenSach AS name,s.TacGia AS author,s.NhaXuatBan AS publisher,s.NamXuatBan AS \"year\",s.DonGiaBan AS price,s.TrangThai AS status,s.MaTL AS genrecode,l.TenTheLoai AS genre,";
        List<Object> args=new ArrayList<>();
        if(search) sql+="c.MaCN AS branchcode,c.TenCN AS branch,COALESCE(t.SoLuongTon,0) AS stock FROM SACH s JOIN THE_LOAI l ON l.MaTL=s.MaTL CROSS JOIN CHI_NHANH c LEFT JOIN TON_KHO t ON t.MaCN=c.MaCN AND t.MaSach=s.MaSach WHERE (?='' OR c.MaCN=?)";
        else sql+="(SELECT COALESCE(SUM(t.SoLuongTon),0) FROM TON_KHO t WHERE t.MaSach=s.MaSach AND (?='' OR t.MaCN=?)) AS stock FROM SACH s JOIN THE_LOAI l ON l.MaTL=s.MaTL WHERE s.DaXoa=FALSE";
        if(search) sql+=" AND s.DaXoa=FALSE";
        args.add(branch);args.add(branch);
        sql+=" AND (LOWER(s.TenSach) LIKE ? ESCAPE '!' OR LOWER(s.TacGia) LIKE ? ESCAPE '!' OR LOWER(COALESCE(s.ISBN,'')) LIKE ? ESCAPE '!' OR LOWER(s.MaSach) LIKE ? ESCAPE '!') AND (?='' OR s.MaTL=?) AND (?='' OR s.TrangThai=?) AND s.DonGiaBan BETWEEN ? AND ? ORDER BY s.MaSach"+(search?",c.MaCN":"");
        args.addAll(List.of(term,term,term,term,val(p,"genre",""),val(p,"genre",""),val(p,"status",""),val(p,"status",""),money(val(p,"min","0")),money(val(p,"max","9999999999.99"))));
        return query(c,sql,args.toArray());
    }

    static List<Map<String,Object>> sales(Connection c) throws SQLException {
        return query(c,"SELECT h.MaHDB AS code,h.NgayBan AS date,COALESCE(k.HoTen,'Khách lẻ') AS customer,v.HoTen AS employee,c.TenCN AS branch,h.TrangThai AS status,h.PhuongThucThanhToan AS payment,SUM(d.SoLuongBan) AS quantity,SUM(d.SoLuongBan*d.DonGiaBanThucTe) AS amount FROM HOA_DON_BAN h LEFT JOIN KHACH_HANG k ON k.MaKH=h.MaKH JOIN NHAN_VIEN v ON v.MaNV=h.MaNV JOIN CHI_NHANH c ON c.MaCN=h.MaCN JOIN CT_HOA_DON_BAN d ON d.MaHDB=h.MaHDB GROUP BY h.MaHDB,h.NgayBan,k.HoTen,v.HoTen,c.TenCN,h.TrangThai,h.PhuongThucThanhToan ORDER BY h.NgayBan DESC,h.MaHDB DESC");
    }

    static Object invoice(Connection c,String code,boolean purchase) throws Exception {
        String sql=purchase?"SELECT p.MaPN AS code,p.NgayNhap AS date,p.TrangThai AS status,n.TenNCC AS customer,v.HoTen AS employee,c.TenCN AS branch FROM PHIEU_NHAP p JOIN NHA_CUNG_CAP n ON n.MaNCC=p.MaNCC JOIN NHAN_VIEN v ON v.MaNV=p.MaNV JOIN CHI_NHANH c ON c.MaCN=p.MaCN WHERE p.MaPN=?":"SELECT h.MaHDB AS code,h.NgayBan AS date,h.TrangThai AS status,h.PhuongThucThanhToan AS payment,COALESCE(k.HoTen,'Khách lẻ') AS customer,v.HoTen AS employee,c.TenCN AS branch FROM HOA_DON_BAN h LEFT JOIN KHACH_HANG k ON k.MaKH=h.MaKH JOIN NHAN_VIEN v ON v.MaNV=h.MaNV JOIN CHI_NHANH c ON c.MaCN=h.MaCN WHERE h.MaHDB=?";
        List<Map<String,Object>> head=query(c,sql,code);
        if(head.isEmpty()) throw new IllegalArgumentException("Không tìm thấy chứng từ.");
        String table=purchase?"CT_PHIEU_NHAP":"CT_HOA_DON_BAN",key=purchase?"MaPN":"MaHDB",qty=purchase?"SoLuongNhap":"SoLuongBan",price=purchase?"DonGiaNhap":"DonGiaBanThucTe";
        return Map.of("header",head.get(0),"lines",query(c,"SELECT d.MaSach AS code,s.TenSach AS name,d."+qty+" AS quantity,d."+price+" AS price,d."+qty+"*d."+price+" AS amount FROM "+table+" d JOIN SACH s ON s.MaSach=d.MaSach WHERE d."+key+"=? ORDER BY d.MaSach",code));
    }

    static Object post(String path,Map<String,List<String>> p) throws Exception {
        try(Connection c=db()) {
            c.setAutoCommit(false);
            try {
                String id;
                switch(path) {
                    case "/api/products" -> {
                        id=sizedCode(p,"code",15);
                        Object[] args={nullable(p,"isbn"),required(p,"name"),required(p,"author"),required(p,"publisher"),integer(required(p,"year"),1900,9999),money(required(p,"price")),choice(p,"status","Đang kinh doanh","Đang kinh doanh","Ngừng kinh doanh"),required(p,"genre"),id};
                        if(editing(c,p,"SACH","MaSach",id)) update(c,"UPDATE SACH SET ISBN=?,TenSach=?,TacGia=?,NhaXuatBan=?,NamXuatBan=?,DonGiaBan=?,TrangThai=?,MaTL=? WHERE MaSach=?",args);
                        else {
                            update(c,"INSERT INTO SACH(ISBN,TenSach,TacGia,NhaXuatBan,NamXuatBan,DonGiaBan,TrangThai,MaTL,MaSach) VALUES(?,?,?,?,?,?,?,?,?)",args);
                            update(c,"INSERT INTO TON_KHO(MaCN,MaSach,SoLuongTon) SELECT MaCN,?,0 FROM CHI_NHANH",id);
                        }
                    }
                    case "/api/products/delete" -> {
                        id=sizedCode(p,"code",15);
                        if(query(c,"SELECT MaSach FROM SACH WHERE MaSach=? FOR UPDATE",id).isEmpty()) throw new IllegalArgumentException("Không tìm thấy sách.");
                        if(query(c,"SELECT MaSach FROM CT_HOA_DON_BAN WHERE MaSach=? UNION ALL SELECT MaSach FROM CT_PHIEU_NHAP WHERE MaSach=?",id,id).isEmpty()) {
                            update(c,"DELETE FROM TON_KHO WHERE MaSach=?",id);
                            update(c,"DELETE FROM SACH WHERE MaSach=?",id);
                        } else update(c,"UPDATE SACH SET TrangThai='Ngừng kinh doanh',DaXoa=TRUE WHERE MaSach=?",id);
                    }
                    case "/api/products/stop", "/api/products/resume" -> {
                        id=sizedCode(p,"code",15);
                        String status=path.endsWith("stop")?"Ngừng kinh doanh":"Đang kinh doanh";
                        if(update(c,"UPDATE SACH SET TrangThai=? WHERE MaSach=? AND DaXoa=FALSE",status,id)==0)throw new IllegalArgumentException("Sách không tồn tại hoặc đã được xóa.");
                    }
                    case "/api/genres/delete", "/api/branches/delete", "/api/employees/delete", "/api/suppliers/delete" -> {
                        id=sizedCode(p,"code",10);deleteCatalog(c,path,id);
                    }
                    case "/api/customers/delete" -> {
                        id=sizedCode(p,"code",10);
                        if(update(c,"DELETE FROM KHACH_HANG WHERE MaKH=?",id)==0)throw new IllegalArgumentException("Khách hàng không tồn tại hoặc đã được xóa.");
                    }
                    case "/api/customers" -> {
                        id=sizedCode(p,"code",10);
                        String original=val(p,"original",id);
                        Object[] args={required(p,"name"),phone(p),nullable(p,"email"),nullable(p,"address"),date(val(p,"registered",LocalDate.now().toString())),nullable(p,"birth")==null?null:date(required(p,"birth")),id};
                        if(editing(c,p,"KHACH_HANG","MaKH",original)) {
                            Object[] edited=Arrays.copyOf(args,args.length+1);edited[args.length]=original;
                            update(c,"UPDATE KHACH_HANG SET HoTen=?,SoDienThoai=?,Email=?,DiaChi=?,NgayDangKy=?,NgaySinh=?,MaKH=? WHERE MaKH=?",edited);
                        } else update(c,"INSERT INTO KHACH_HANG(HoTen,SoDienThoai,Email,DiaChi,NgayDangKy,NgaySinh,MaKH) VALUES(?,?,?,?,?,?,?)",args);
                    }
                    case "/api/genres" -> {
                        id=sizedCode(p,"code",10);
                        if(editing(c,p,"THE_LOAI","MaTL",id)) update(c,"UPDATE THE_LOAI SET TenTheLoai=?,MoTa=? WHERE MaTL=?",required(p,"name"),nullable(p,"description"),id);
                        else update(c,"INSERT INTO THE_LOAI VALUES(?,?,?)",id,required(p,"name"),nullable(p,"description"));
                    }
                    case "/api/branches" -> {
                        id=sizedCode(p,"code",10);
                        Object[] args={required(p,"name"),required(p,"address"),phone(p),choice(p,"status","Hoạt động","Hoạt động","Tạm ngừng"),id};
                        if(editing(c,p,"CHI_NHANH","MaCN",id)) update(c,"UPDATE CHI_NHANH SET TenCN=?,DiaChi=?,SoDienThoai=?,TrangThai=? WHERE MaCN=?",args);
                        else {
                            update(c,"INSERT INTO CHI_NHANH(TenCN,DiaChi,SoDienThoai,TrangThai,MaCN) VALUES(?,?,?,?,?)",args);
                            update(c,"INSERT INTO TON_KHO(MaCN,MaSach,SoLuongTon) SELECT ?,MaSach,0 FROM SACH",id);
                        }
                    }
                    case "/api/suppliers" -> {
                        id=sizedCode(p,"code",10);
                        Object[] args={required(p,"name"),required(p,"address"),phone(p),nullable(p,"email"),id};
                        if(editing(c,p,"NHA_CUNG_CAP","MaNCC",id)) update(c,"UPDATE NHA_CUNG_CAP SET TenNCC=?,DiaChi=?,SoDienThoai=?,Email=? WHERE MaNCC=?",args);
                        else update(c,"INSERT INTO NHA_CUNG_CAP(TenNCC,DiaChi,SoDienThoai,Email,MaNCC) VALUES(?,?,?,?,?)",args);
                    }
                    case "/api/employees" -> {
                        id=sizedCode(p,"code",10);
                        LocalDate birth=date(required(p,"birth"));
                        if(birth.isAfter(LocalDate.now())) throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
                        Object[] args={required(p,"name"),birth,choice(p,"gender","Khác","Nam","Nữ","Khác"),phone(p),choice(p,"role","Nhân viên bán hàng","Quản trị viên","Nhân viên bán hàng","Nhân viên kho","Nhân viên nhập hàng"),date(required(p,"joined")),choice(p,"status","Đang làm việc","Đang làm việc","Tạm nghỉ","Đã nghỉ việc"),required(p,"branch"),id};
                        if(editing(c,p,"NHAN_VIEN","MaNV",id)) update(c,"UPDATE NHAN_VIEN SET HoTen=?,NgaySinh=?,GioiTinh=?,SoDienThoai=?,ChucVu=?,NgayVaoLam=?,TrangThai=?,MaCN=? WHERE MaNV=?",args);
                        else update(c,"INSERT INTO NHAN_VIEN(HoTen,NgaySinh,GioiTinh,SoDienThoai,ChucVu,NgayVaoLam,TrangThai,MaCN,MaNV) VALUES(?,?,?,?,?,?,?,?,?)",args);
                    }
                    case "/api/sales", "/api/purchases" -> id=createTransaction(c,p,path.endsWith("sales"));
                    case "/api/purchases/delete", "/api/purchases/confirm", "/api/purchases/restore", "/api/sales/cancel", "/api/sales/confirm" -> {
                        id=required(p,"code");
                        changeTransaction(c,id,path.contains("/purchases/"),path.endsWith("confirm")?"confirm":path.endsWith("restore")?"restore":"cancel");
                    }
                    default -> throw new IllegalArgumentException("Thao tác không tồn tại.");
                }
                c.commit();return Map.of("ok",true,"code",id);
            } catch(Exception ex) {c.rollback();throw ex;}
        }
    }

    static void deleteCatalog(Connection c,String path,String id) throws Exception {
        String table,key,label,refs;
        switch(path) {
            case "/api/genres/delete" -> {table="THE_LOAI";key="MaTL";label="Thể loại";refs="SELECT MaSach FROM SACH WHERE MaTL=?";}
            case "/api/branches/delete" -> {table="CHI_NHANH";key="MaCN";label="Chi nhánh";refs="SELECT MaNV FROM NHAN_VIEN WHERE MaCN=? UNION ALL SELECT MaHDB FROM HOA_DON_BAN WHERE MaCN=? UNION ALL SELECT MaPN FROM PHIEU_NHAP WHERE MaCN=? UNION ALL SELECT MaSach FROM TON_KHO WHERE MaCN=? AND SoLuongTon>0";}
            case "/api/employees/delete" -> {table="NHAN_VIEN";key="MaNV";label="Nhân viên";refs="SELECT MaHDB FROM HOA_DON_BAN WHERE MaNV=? UNION ALL SELECT MaPN FROM PHIEU_NHAP WHERE MaNV=?";}
            case "/api/suppliers/delete" -> {table="NHA_CUNG_CAP";key="MaNCC";label="Nhà cung cấp";refs="SELECT MaPN FROM PHIEU_NHAP WHERE MaNCC=?";}
            default -> throw new IllegalArgumentException("Danh mục không hợp lệ.");
        }
        if(query(c,"SELECT "+key+" FROM "+table+" WHERE "+key+"=? FOR UPDATE",id).isEmpty())throw new IllegalArgumentException(label+" không tồn tại hoặc đã được xóa.");
        Object[] args=new Object[(int)refs.chars().filter(ch->ch=='?').count()];Arrays.fill(args,id);
        if(!query(c,refs,args).isEmpty())throw new IllegalArgumentException(label+" đang được sử dụng bởi sách, nhân viên, tồn kho hoặc chứng từ. Cần chuyển dữ liệu liên quan trước khi xóa để giữ đúng báo cáo.");
        if(table.equals("CHI_NHANH"))update(c,"DELETE FROM TON_KHO WHERE MaCN=?",id);
        update(c,"DELETE FROM "+table+" WHERE "+key+"=?",id);
    }

    static String createTransaction(Connection c,Map<String,List<String>> p,boolean sale) throws Exception {
        String id=(sale?"HDB":"PN")+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase(Locale.ROOT);
        String branch=required(p,"branch"),employee=required(p,"employee");
        if(query(c,"SELECT MaCN FROM CHI_NHANH WHERE MaCN=? AND TrangThai='Hoạt động' FOR UPDATE",branch).isEmpty()) throw new IllegalArgumentException("Chi nhánh đang tạm ngừng hoặc không tồn tại.");
        List<Map<String,Object>> staff=query(c,"SELECT MaCN AS branch,ChucVu AS role FROM NHAN_VIEN WHERE MaNV=? AND TrangThai='Đang làm việc' FOR UPDATE",employee);
        if(staff.isEmpty()) throw new IllegalArgumentException("Nhân viên không đang làm việc.");
        String role=String.valueOf(staff.get(0).get("role"));
        if(!role.equals("Quản trị viên")&&(!branch.equals(staff.get(0).get("branch"))||!(sale?role.equals("Nhân viên bán hàng"):Set.of("Nhân viên nhập hàng","Nhân viên kho").contains(role)))) throw new IllegalArgumentException("Hãy chọn nhân viên đúng chi nhánh và vai trò.");
        List<String> products=p.getOrDefault("product",List.of()),quantities=p.getOrDefault("quantity",List.of()),prices=p.getOrDefault("price",List.of());
        if(products.isEmpty()||products.size()>50||products.size()!=quantities.size()||products.size()!=prices.size()) throw new IllegalArgumentException("Chứng từ cần từ 1 đến 50 dòng đủ số lượng và đơn giá.");
        if(new HashSet<>(products).size()!=products.size()) throw new IllegalArgumentException("Mỗi sách chỉ xuất hiện một lần. Hãy gộp số lượng.");
        java.time.LocalDateTime time=timestamp(required(p,"date"));
        String status=sale?choice(p,"status","Hoàn tất","Hoàn tất","Chờ xử lý"):choice(p,"status","Đã xác nhận","Đã xác nhận","Chờ xác nhận");
        if(sale) update(c,"INSERT INTO HOA_DON_BAN VALUES(?,?,?,?,?,?,?)",id,time,status,choice(p,"payment","Tiền mặt","Tiền mặt","Chuyển khoản","Thẻ"),employee,branch,nullable(p,"customer"));
        else update(c,"INSERT INTO PHIEU_NHAP VALUES(?,?,?,?,?,?)",id,time,status,required(p,"supplier"),employee,branch);
        List<Integer> order=new ArrayList<>();for(int i=0;i<products.size();i++)order.add(i);order.sort(Comparator.comparing(products::get));
        boolean apply=status.equals(sale?"Hoàn tất":"Đã xác nhận");
        for(int i:order) {
            String product=products.get(i);int qty=integer(quantities.get(i),1,1000000);BigDecimal price=money(prices.get(i));
            List<Map<String,Object>> found=query(c,"SELECT TrangThai AS status FROM SACH WHERE MaSach=? AND DaXoa=FALSE FOR UPDATE",product);
            if(found.isEmpty()||!found.get(0).get("status").equals("Đang kinh doanh")) throw new IllegalArgumentException("Sách "+product+" không còn kinh doanh hoặc không tồn tại.");
            update(c,sale?"INSERT INTO CT_HOA_DON_BAN VALUES(?,?,?,?)":"INSERT INTO CT_PHIEU_NHAP VALUES(?,?,?,?)",id,product,qty,price);
            if(apply) stock(c,branch,product,sale?-qty:qty);
        }
        return id;
    }

    static void changeTransaction(Connection c,String id,boolean purchase,String action) throws Exception {
        String table=purchase?"PHIEU_NHAP":"HOA_DON_BAN",key=purchase?"MaPN":"MaHDB",detail=purchase?"CT_PHIEU_NHAP":"CT_HOA_DON_BAN",qty=purchase?"SoLuongNhap":"SoLuongBan";
        List<Map<String,Object>> found=query(c,"SELECT TrangThai AS status,MaCN AS branch FROM "+table+" WHERE "+key+"=? FOR UPDATE",id);
        if(found.isEmpty()) throw new IllegalArgumentException("Không tìm thấy chứng từ.");
        String current=String.valueOf(found.get(0).get("status")),branch=String.valueOf(found.get(0).get("branch"));
        String pending=purchase?"Chờ xác nhận":"Chờ xử lý",done=purchase?"Đã xác nhận":"Hoàn tất",next;
        int factor=0;
        if(action.equals("restore")) {
            if(!current.equals("Đã hủy")) throw new IllegalArgumentException("Chỉ khôi phục phiếu đã xóa.");
            next=pending;
        } else if(action.equals("confirm")) {
            if(!current.equals(pending)) throw new IllegalArgumentException("Chứng từ không ở trạng thái chờ.");
            if(query(c,"SELECT MaCN FROM CHI_NHANH WHERE MaCN=? AND TrangThai='Hoạt động'",branch).isEmpty()) throw new IllegalArgumentException("Chi nhánh đang tạm ngừng.");
            next=done;factor=purchase?1:-1;
        } else {
            if(current.equals("Đã hủy")) throw new IllegalArgumentException("Chứng từ đã được hủy trước đó.");
            next="Đã hủy";if(current.equals(done)) factor=purchase?-1:1;
        }
        List<Map<String,Object>> lines=query(c,"SELECT MaSach AS code,"+qty+" AS quantity FROM "+detail+" WHERE "+key+"=? ORDER BY MaSach",id);
        for(Map<String,Object> line:lines) {
            String book=String.valueOf(line.get("code"));
            Map<String,Object> b=query(c,"SELECT TrangThai AS status,DaXoa AS deleted FROM SACH WHERE MaSach=? FOR UPDATE",book).get(0);
            if(action.equals("confirm")&&(!b.get("status").equals("Đang kinh doanh")||Boolean.TRUE.equals(b.get("deleted")))) throw new IllegalArgumentException("Sách "+book+" đã ngừng kinh doanh.");
            if(factor!=0) stock(c,branch,book,factor*((Number)line.get("quantity")).intValue());
        }
        if(purchase&&action.equals("cancel")) update(c,"DELETE FROM PHIEU_NHAP WHERE MaPN=?",id);
        else update(c,"UPDATE "+table+" SET TrangThai=? WHERE "+key+"=?",next,id);
    }

    static void stock(Connection c,String branch,String book,int delta) throws Exception {
        update(c,"INSERT INTO TON_KHO(MaCN,MaSach,SoLuongTon) SELECT ?,?,0 WHERE NOT EXISTS (SELECT 1 FROM TON_KHO WHERE MaCN=? AND MaSach=?)",branch,book,branch,book);
        if(update(c,"UPDATE TON_KHO SET SoLuongTon=SoLuongTon+? WHERE MaCN=? AND MaSach=? AND SoLuongTon+?>=0",delta,branch,book,delta)==0) throw new IllegalArgumentException(delta<0?"Không đủ tồn kho của "+book+" tại "+branch+". Toàn bộ thao tác chưa được lưu; không thể xóa phiếu nhập nếu phần tồn còn lại không đủ để hoàn tác.":"Không thể cập nhật tồn kho.");
    }

    static boolean editing(Connection c,Map<String,List<String>> p,String table,String key,String id) throws Exception {
        if(!val(p,"edit","0").equals("1"))return false;
        if(query(c,"SELECT "+key+" FROM "+table+" WHERE "+key+"=? FOR UPDATE",id).isEmpty())throw new IllegalArgumentException("Bản ghi cần sửa không tồn tại.");
        return true;
    }
    static String sizedCode(Map<String,List<String>> p,String key,int max) {String v=code(p,key);if(v.length()>max)throw new IllegalArgumentException("Mã tối đa "+max+" ký tự.");return v;}
    static String nullable(Map<String,List<String>> p,String key) {String v=val(p,key,"");return v.isBlank()?null:v;}
    static String phone(Map<String,List<String>> p) {String v=required(p,"phone");if(v.length()>15||!v.matches("\\+?[0-9]+")) throw new IllegalArgumentException("Số điện thoại chỉ gồm chữ số, có thể bắt đầu bằng + và tối đa 15 ký tự.");return v;}
    static String choice(Map<String,List<String>> p,String key,String fallback,String...options) {String v=val(p,key,fallback);if(!List.of(options).contains(v))throw new IllegalArgumentException("Giá trị "+key+" không hợp lệ.");return v;}
    static java.time.LocalDateTime timestamp(String value) {try{return value.length()==10?date(value).atStartOfDay():java.time.LocalDateTime.parse(value.replace(' ','T'));}catch(Exception ex){throw new IllegalArgumentException("Ngày giờ không hợp lệ.");}}

    static Object report(Connection c,Map<String,List<String>> p) throws Exception {
        LocalDate from=date(val(p,"from","2026-01-01")),to=date(val(p,"to","2026-12-31"));
        if(to.isBefore(from)||to.equals(LocalDate.MAX))throw new IllegalArgumentException("Khoảng ngày không hợp lệ.");
        String branch=val(p,"branch","");
        List<Map<String,Object>> sections=new ArrayList<>();String title;
        if(val(p,"kind","revenue").equals("revenue")) {
            title="Doanh thu và chi phí nhập theo chi nhánh";
            String sql="WITH DT AS (SELECT h.MaCN,COUNT(DISTINCT h.MaHDB) AS SoHD,SUM(d.SoLuongBan) AS SLBan,SUM(d.SoLuongBan*d.DonGiaBanThucTe) AS DoanhThu FROM HOA_DON_BAN h JOIN CT_HOA_DON_BAN d ON d.MaHDB=h.MaHDB WHERE h.TrangThai='Hoàn tất' AND h.NgayBan>=? AND h.NgayBan<? GROUP BY h.MaCN), CP AS (SELECT p.MaCN,COUNT(DISTINCT p.MaPN) AS SoPN,SUM(d.SoLuongNhap) AS SLNhap,SUM(d.SoLuongNhap*d.DonGiaNhap) AS ChiPhi FROM PHIEU_NHAP p JOIN CT_PHIEU_NHAP d ON d.MaPN=p.MaPN WHERE p.TrangThai='Đã xác nhận' AND p.NgayNhap>=? AND p.NgayNhap<? GROUP BY p.MaCN) SELECT c.MaCN AS \"Mã CN\",c.TenCN AS \"Chi nhánh\",COALESCE(DT.SoHD,0) AS \"Hóa đơn hoàn tất\",COALESCE(DT.SLBan,0) AS \"Số sách bán\",COALESCE(DT.DoanhThu,0) AS \"Doanh thu (đ)\",COALESCE(CP.SoPN,0) AS \"Phiếu đã xác nhận\",COALESCE(CP.SLNhap,0) AS \"Số sách nhập\",COALESCE(CP.ChiPhi,0) AS \"Chi phí nhập (đ)\" FROM CHI_NHANH c LEFT JOIN DT ON DT.MaCN=c.MaCN LEFT JOIN CP ON CP.MaCN=c.MaCN WHERE (?='' OR c.MaCN=?) ORDER BY COALESCE(DT.DoanhThu,0) DESC,c.MaCN";
            sections.add(reportSection(c,title,sql,List.of(from.atStartOfDay(),to.plusDays(1).atStartOfDay(),from.atStartOfDay(),to.plusDays(1).atStartOfDay(),branch,branch)));
        } else if(val(p,"kind","").equals("inventory")) {
            title="Cảnh báo tồn kho và sách bán chạy";
            String sql="SELECT c.MaCN AS \"Mã CN\",c.TenCN AS \"Chi nhánh\",s.MaSach AS \"Mã sách\",s.ISBN AS \"ISBN\",s.TenSach AS \"Tên sách\",l.TenTheLoai AS \"Thể loại\",t.SoLuongTon AS \"Tồn kho\" FROM TON_KHO t JOIN CHI_NHANH c ON c.MaCN=t.MaCN JOIN SACH s ON s.MaSach=t.MaSach JOIN THE_LOAI l ON l.MaTL=s.MaTL WHERE t.SoLuongTon<=? AND s.TrangThai='Đang kinh doanh' AND (?='' OR c.MaCN=?) ORDER BY t.SoLuongTon,c.MaCN,s.MaSach";
            sections.add(reportSection(c,"Sách sắp hết theo chi nhánh",sql,List.of(integer(val(p,"threshold","5"),0,1000000),branch,branch)));
            sql="WITH T AS (SELECT s.MaSach,s.ISBN,s.TenSach,l.TenTheLoai,SUM(d.SoLuongBan) AS Tong,SUM(d.SoLuongBan*d.DonGiaBanThucTe) AS Tien FROM SACH s JOIN THE_LOAI l ON l.MaTL=s.MaTL JOIN CT_HOA_DON_BAN d ON d.MaSach=s.MaSach JOIN HOA_DON_BAN h ON h.MaHDB=d.MaHDB WHERE h.TrangThai='Hoàn tất' AND h.NgayBan>=? AND h.NgayBan<? AND (?='' OR h.MaCN=?) GROUP BY s.MaSach,s.ISBN,s.TenSach,l.TenTheLoai) SELECT MaSach AS \"Mã sách\",ISBN AS \"ISBN\",TenSach AS \"Tên sách\",TenTheLoai AS \"Thể loại\",Tong AS \"Số lượng bán\",Tien AS \"Doanh thu (đ)\" FROM T WHERE Tong>=(SELECT AVG(CAST(Tong AS DECIMAL(18,2))) FROM T) ORDER BY Tong DESC,Tien DESC,MaSach";
            sections.add(reportSection(c,"Sách bán từ mức trung bình trở lên",sql,List.of(from.atStartOfDay(),to.plusDays(1).atStartOfDay(),branch,branch)));
        } else throw new IllegalArgumentException("Loại báo cáo không hợp lệ.");
        return Map.of("title",title,"sections",sections);
    }
    static Map<String,Object> reportSection(Connection c,String title,String sql,List<Object> args) throws Exception {return Map.of("title",title,"sql",sql,"parameters",args,"rows",query(c,sql,args.toArray()));}

    static List<Map<String,Object>> query(Connection c,String sql,Object...args) throws SQLException {
        try(PreparedStatement s=c.prepareStatement(sql)) {
            bind(s,args);try(ResultSet rs=s.executeQuery()) {
                List<Map<String,Object>> rows=new ArrayList<>();ResultSetMetaData md=rs.getMetaData();
                while(rs.next()) { Map<String,Object> row=new LinkedHashMap<>();for(int i=1;i<=md.getColumnCount();i++) {String label=md.getColumnLabel(i);if(label.equals(label.toUpperCase(Locale.ROOT))) label=label.toLowerCase(Locale.ROOT);row.put(label,rs.getObject(i));}rows.add(row); }
                return rows;
            }
        }
    }
    static int update(Connection c,String sql,Object...args) throws SQLException { try(PreparedStatement s=c.prepareStatement(sql)) {bind(s,args);return s.executeUpdate();} }
    static void bind(PreparedStatement s,Object...args) throws SQLException {for(int i=0;i<args.length;i++) s.setObject(i+1,args[i]);}
    static Map<String,List<String>> params(String raw) {
        Map<String,List<String>> map=new LinkedHashMap<>();if(raw==null||raw.isEmpty()) return map;
        for(String pair:raw.split("&")) {String[] kv=pair.split("=",2);String k=URLDecoder.decode(kv[0],StandardCharsets.UTF_8),v=kv.length==2?URLDecoder.decode(kv[1],StandardCharsets.UTF_8):"";map.computeIfAbsent(k,a->new ArrayList<>()).add(v.trim());}return map;
    }
    static String val(Map<String,List<String>> p,String k,String fallback) {return p.getOrDefault(k,List.of(fallback)).get(0);}
    static String required(Map<String,List<String>> p,String k) {String v=val(p,k,"");if(v.isBlank()) throw new IllegalArgumentException("Vui lòng điền đủ thông tin bắt buộc ("+k+").");return v;}
    static String code(Map<String,List<String>> p,String k) {String v=required(p,k).toUpperCase(Locale.ROOT);if(!v.matches("[A-Z0-9_-]{2,30}")) throw new IllegalArgumentException("Mã gồm 2–30 ký tự: chữ không dấu, số, _ hoặc -.");return v;}
    static int integer(String s,int min,int max) {try {int n=Integer.parseInt(s);if(n<min||n>max) throw new NumberFormatException();return n;}catch(NumberFormatException e){throw new IllegalArgumentException("Số lượng phải là số nguyên từ "+min+" đến "+max+".");}}
    static BigDecimal money(String s) {try {BigDecimal n=new BigDecimal(s);if(n.signum()<0||n.scale()>2||n.compareTo(new BigDecimal("9999999999.99"))>0) throw new NumberFormatException();return n;}catch(NumberFormatException e){throw new IllegalArgumentException("Đơn giá phải không âm, tối đa 9.999.999.999,99 đ và 2 chữ số thập phân.");}}
    static LocalDate date(String s) {try{return LocalDate.parse(s);}catch(Exception e){throw new IllegalArgumentException("Ngày không hợp lệ; dùng định dạng năm-tháng-ngày.");}}
    static Set<String> split(String s) {Set<String> values=new LinkedHashSet<>();for(String v:s.split("[,;\\n]")) if(!v.isBlank()) values.add(v.trim());return values;}
    static String escapeLike(String s) {return s.replace("!","!!").replace("%","!%").replace("_","!_");}
    static void file(HttpExchange x,Path file,String type) throws IOException {
        byte[] b=Files.readAllBytes(file);headers(x,type);x.sendResponseHeaders(200,b.length);x.getResponseBody().write(b);
    }
    static void headers(HttpExchange x,String type) {x.getResponseHeaders().set("Content-Type",type);x.getResponseHeaders().set("X-Content-Type-Options","nosniff");x.getResponseHeaders().set("Cache-Control","no-store");x.getResponseHeaders().set("Content-Security-Policy","default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:; object-src 'none'; base-uri 'none'; frame-ancestors 'none'");}
    static void send(HttpExchange x,int status,Object data) throws IOException {byte[] bytes=json(data).getBytes(StandardCharsets.UTF_8);headers(x,"application/json; charset=utf-8");x.sendResponseHeaders(status,bytes.length);x.getResponseBody().write(bytes);}
    static String json(Object o) {
        if(o==null) return "null";
        if(o instanceof Number||o instanceof Boolean) return o.toString();
        if(o instanceof Map<?,?> m) {List<String> a=new ArrayList<>();m.forEach((k,v)->a.add(json(k.toString())+":"+json(v)));return "{"+String.join(",",a)+"}";}
        if(o instanceof Iterable<?> list) {List<String> a=new ArrayList<>();for(Object v:list)a.add(json(v));return "["+String.join(",",a)+"]";}
        StringBuilder b=new StringBuilder("\"");for(char ch:o.toString().toCharArray()) {switch(ch){case '"'->b.append("\\\"");case '\\'->b.append("\\\\");case '\n'->b.append("\\n");case '\r'->b.append("\\r");case '\t'->b.append("\\t");default->{if(ch<32)b.append(String.format("\\u%04x",(int)ch));else b.append(ch);}}}return b.append('"').toString();
    }
    static void seed(Connection c) throws Exception {
        c.setAutoCommit(false);
        try(Reader r=Files.newBufferedReader(ROOT.resolve("sql/data.sql"))) {RunScript.execute(c,r);c.commit();}
        catch(Exception e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
    }
}
