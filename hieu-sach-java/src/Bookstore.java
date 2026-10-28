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
    static final List<String> TABLES = List.of("CHUCH","CHU_SDT","CUAHANG","CUAHANG_SDT","NHANVIEN","NHANVIEN_SDT","LAM","HANGHOA","DUNGCU","SACH","SACH_TB","KHACHHANG","KHACHHANG_SDT","NHACUNGCAP","NHACUNGCAP_SDT","CHINHANH","HOADONBAN","BANHANG","HOADONNHAP","NHAPHANG");
    static Connection db() throws SQLException {
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

    public static void main(String[] args) throws Exception {
        if(PUBLIC_DEMO&&DATABASE_URL.isBlank()) throw new IllegalArgumentException("Bản công khai cần DATABASE_URL PostgreSQL để dữ liệu không bị mất khi máy chủ khởi động lại.");
        if(DATABASE_URL.isBlank()) Files.createDirectories(ROOT.resolve(System.getProperty("bookstore.data", "data/hieusach")).getParent());
        try (Connection c = db()) {
            try (Reader r = Files.newBufferedReader(ROOT.resolve("sql/schema.sql"))) { RunScript.execute(c,r); }
            if (((Number)query(c,"SELECT COUNT(*) AS n FROM CHUCH").get(0).get("n")).intValue()==0) seed(c);
        }
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
            if("23505".equals(state)) message="Mã đã tồn tại hoặc có dòng bị trùng. Hãy dùng mã khác.";
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
                case "/api/health" -> Map.of("status","ok","application","tri-thuc-bookstore","database",DATABASE_URL.isBlank()?"H2":"PostgreSQL","publicDemo",PUBLIC_DEMO);
                case "/api/dashboard" -> {
                    Map<String,Object> m=new LinkedHashMap<>();
                    m.put("metrics",query(c,"SELECT (SELECT COUNT(*) FROM HANGHOA) AS products, (SELECT COUNT(*) FROM KHACHHANG) AS customers, (SELECT COUNT(*) FROM HOADONBAN) AS invoices, (SELECT COALESCE(SUM(SoLuong*DonGiaBan),0) FROM BANHANG) AS revenue, (SELECT COUNT(*) FROM HANGHOA WHERE SoLuong<=20) AS lowstock").get(0));
                    m.put("recent",sales(c));
                    m.put("popular",query(c,"SELECT h.MaHH AS code,h.Ten AS name,SUM(b.SoLuong) AS quantity,SUM(b.SoLuong*b.DonGiaBan) AS amount FROM BANHANG b JOIN HANGHOA h ON h.MaHH=b.MaHH GROUP BY h.MaHH,h.Ten ORDER BY quantity DESC,h.MaHH FETCH FIRST 5 ROWS ONLY"));
                    yield m;
                }
                case "/api/products" -> {
                    String term="%"+escapeLike(val(p,"q","")) .toLowerCase(Locale.ROOT)+"%";
                    yield query(c,"SELECT h.MaHH AS code,h.Ten AS name,h.Gia AS price,h.SoLuong AS stock,h.Loai AS type,s.TacGia AS author,s.TheLoai AS genre,d.Loai AS category FROM HANGHOA h LEFT JOIN SACH s ON s.MaHHS=h.MaHH LEFT JOIN DUNGCU d ON d.MaHHD=h.MaHH WHERE (LOWER(h.MaHH) LIKE ? ESCAPE '!' OR LOWER(h.Ten) LIKE ? ESCAPE '!' OR LOWER(COALESCE(s.TacGia,'')) LIKE ? ESCAPE '!' OR LOWER(COALESCE(s.TheLoai,d.Loai,'')) LIKE ? ESCAPE '!') AND (?='' OR h.Loai=?) ORDER BY h.MaHH",term,term,term,term,val(p,"type",""),val(p,"type",""));
                }
                case "/api/customers" -> query(c,"SELECT k.MaKH AS code,k.Ten AS name,k.DiaChi AS address,k.GioiTinh AS gender,k.NgaySinh AS birth,(SELECT "+(DATABASE_URL.isBlank()?"LISTAGG(s.SDT, ', ') WITHIN GROUP(ORDER BY s.SDT)":"STRING_AGG(s.SDT, ', ' ORDER BY s.SDT)")+" FROM KHACHHANG_SDT s WHERE s.MaKH=k.MaKH) AS phone FROM KHACHHANG k ORDER BY k.MaKH");
                case "/api/employees" -> query(c,"SELECT MaNV AS code,Ten AS name,NhiemVu AS role FROM NHANVIEN ORDER BY MaNV");
                case "/api/suppliers" -> query(c,"SELECT n.MaCC AS code,n.Ten AS name,c.TenChiNhanh AS branch,c.DiaChi AS address FROM NHACUNGCAP n JOIN CHINHANH c ON c.MaCC=n.MaCC ORDER BY n.MaCC,c.TenChiNhanh");
                case "/api/sales" -> sales(c);
                case "/api/purchases" -> query(c,"SELECT h.MaHDN AS code,h.NgayNhap AS date,n.Ten AS supplier,h.TenChiNhanh AS branch,v.Ten AS employee,SUM(d.SoLuong*d.DonGiaNhap) AS amount FROM HOADONNHAP h JOIN NHACUNGCAP n ON n.MaCC=h.MaCC JOIN NHANVIEN v ON v.MaNV=h.MaNV JOIN NHAPHANG d ON d.MaHDN=h.MaHDN GROUP BY h.MaHDN,h.NgayNhap,n.Ten,h.TenChiNhanh,v.Ten ORDER BY h.NgayNhap DESC,h.MaHDN DESC");
                case "/api/invoice" -> {
                    String code=required(p,"code");
                    List<Map<String,Object>> head=query(c,"SELECT h.MaHDB AS code,h.NgayBan AS date,k.Ten AS customer,k.DiaChi AS address,v.Ten AS employee FROM HOADONBAN h JOIN KHACHHANG k ON k.MaKH=h.MaKH JOIN NHANVIEN v ON v.MaNV=h.MaNV WHERE h.MaHDB=?",code);
                    if(head.isEmpty()) throw new IllegalArgumentException("Không tìm thấy hóa đơn.");
                    yield Map.of("header",head.get(0),"lines",query(c,"SELECT b.MaHH AS code,h.Ten AS name,b.SoLuong AS quantity,b.DonGiaBan AS price,b.SoLuong*b.DonGiaBan AS amount FROM BANHANG b JOIN HANGHOA h ON h.MaHH=b.MaHH WHERE b.MaHDB=? ORDER BY b.MaHH",code));
                }
                case "/api/reports" -> report(c,p);
                case "/api/database" -> {
                    List<Map<String,Object>> tables=new ArrayList<>();
                    for(String t:TABLES) tables.add(Map.of("name",t,"rows",query(c,"SELECT COUNT(*) AS n FROM "+t).get(0).get("n")));
                    yield Map.of("engine",c.getMetaData().getDatabaseProductName()+" "+c.getMetaData().getDatabaseProductVersion()+" · JDBC · "+(DATABASE_URL.isBlank()?"lưu tại máy":"CSDL riêng trên máy chủ"),"tables",tables);
                }
                default -> throw new IllegalArgumentException("Đường dẫn API không tồn tại.");
            };
        }
    }

    static List<Map<String,Object>> sales(Connection c) throws SQLException {
        return query(c,"SELECT h.MaHDB AS code,h.NgayBan AS date,k.Ten AS customer,v.Ten AS employee,SUM(b.SoLuong) AS quantity,SUM(b.SoLuong*b.DonGiaBan) AS amount FROM HOADONBAN h JOIN KHACHHANG k ON k.MaKH=h.MaKH JOIN NHANVIEN v ON v.MaNV=h.MaNV JOIN BANHANG b ON b.MaHDB=h.MaHDB GROUP BY h.MaHDB,h.NgayBan,k.Ten,v.Ten ORDER BY h.NgayBan DESC,h.MaHDB DESC");
    }

    static Object post(String path,Map<String,List<String>> p) throws Exception {
        try(Connection c=db()) {
            c.setAutoCommit(false);
            try {
                String result;
                switch(path) {
                    case "/api/products/delete" -> {
                        String product=code(p,"code");
                        if(query(c,"SELECT MaHH FROM HANGHOA WHERE MaHH=? FOR UPDATE",product).isEmpty())
                            throw new IllegalArgumentException("Hàng hóa không tồn tại hoặc đã được xóa.");
                        if(!query(c,"SELECT MaHH FROM BANHANG WHERE MaHH=? UNION ALL SELECT MaHH FROM NHAPHANG WHERE MaHH=?",product,product).isEmpty())
                            throw new IllegalArgumentException("Không thể xóa hàng hóa đã có giao dịch nhập hoặc bán. Cần giữ lại để bảo toàn hóa đơn và báo cáo.");
                        update(c,"DELETE FROM SACH_TB WHERE MaHHS=?",product);
                        update(c,"DELETE FROM SACH WHERE MaHHS=?",product);
                        update(c,"DELETE FROM DUNGCU WHERE MaHHD=?",product);
                        update(c,"DELETE FROM HANGHOA WHERE MaHH=?",product);
                        result=product;
                    }
                    case "/api/products" -> {
                        String code=code(p,"code"),name=required(p,"name"),type=required(p,"type");
                        if(!Set.of("SACH","DUNGCU").contains(type)) throw new IllegalArgumentException("Loại hàng không hợp lệ.");
                        update(c,"INSERT INTO HANGHOA VALUES(?,?,?,?,?)",code,name,money(required(p,"price")),integer(required(p,"stock"),0,1000000),type);
                        if(type.equals("SACH")) {
                            update(c,"INSERT INTO SACH VALUES(?,?,?)",code,required(p,"author"),required(p,"genre"));
                            for(String edition:split(val(p,"edition",""))) update(c,"INSERT INTO SACH_TB VALUES(?,?)",code,edition);
                        } else update(c,"INSERT INTO DUNGCU VALUES(?,?)",code,required(p,"category"));
                        result=code;
                    }
                    case "/api/customers" -> {
                        String code=code(p,"code"),birth=val(p,"birth",""),gender=val(p,"gender","");
                        if(!Set.of("","Nam","Nữ","Khác").contains(gender)) throw new IllegalArgumentException("Giới tính không hợp lệ.");
                        LocalDate dob=birth.isBlank()?null:date(birth);
                        if(dob!=null&&dob.isAfter(LocalDate.now())) throw new IllegalArgumentException("Ngày sinh không được ở tương lai.");
                        update(c,"INSERT INTO KHACHHANG VALUES(?,?,?,?,?)",code,required(p,"name"),val(p,"address",""),gender,dob);
                        for(String phone:split(val(p,"phone",""))) {
                            if(!phone.matches("\\+?[0-9]{8,15}")) throw new IllegalArgumentException("Số điện thoại phải gồm 8–15 chữ số, có thể bắt đầu bằng +.");
                            update(c,"INSERT INTO KHACHHANG_SDT VALUES(?,?)",code,phone);
                        }
                        result=code;
                    }
                    case "/api/sales", "/api/purchases" -> {
                        boolean sale=path.endsWith("sales");
                        String id=(sale?"HDB":"HDN")+UUID.randomUUID().toString().replace("-","").substring(0,12).toUpperCase(Locale.ROOT);
                        String employee=required(p,"employee");
                        String role=sale?"Bán hàng":"Nhập hàng";
                        if(query(c,"SELECT MaNV FROM NHANVIEN WHERE MaNV=? AND NhiemVu=?",employee,role).isEmpty()) throw new IllegalArgumentException("Hãy chọn nhân viên có nhiệm vụ "+role.toLowerCase(Locale.ROOT)+".");
                        LocalDate day=date(required(p,"date"));
                        List<String> products=p.getOrDefault("product",List.of()),quantities=p.getOrDefault("quantity",List.of()),prices=p.getOrDefault("price",List.of());
                        if(products.isEmpty()||products.size()>50||products.size()!=quantities.size()||(!sale&&products.size()!=prices.size())) throw new IllegalArgumentException("Hóa đơn cần từ 1 đến 50 dòng hợp lệ.");
                        if(new HashSet<>(products).size()!=products.size()) throw new IllegalArgumentException("Mỗi hàng hóa chỉ xuất hiện một lần. Hãy gộp số lượng.");
                        if(sale) update(c,"INSERT INTO HOADONBAN VALUES(?,?,?,?)",id,day,employee,required(p,"customer"));
                        else update(c,"INSERT INTO HOADONNHAP VALUES(?,?,?,?,?)",id,day,employee,required(p,"supplier"),required(p,"branch"));
                        // Sap theo ma de cac giao dich dong thoi khoa hang theo cung thu tu.
                        List<Integer> order=new ArrayList<>();for(int i=0;i<products.size();i++) order.add(i);
                        order.sort(Comparator.comparing(products::get));
                        for(int i:order) {
                            String product=products.get(i);int qty=integer(quantities.get(i),1,1000000);
                            List<Map<String,Object>> found=query(c,"SELECT Gia AS price,Ten AS name FROM HANGHOA WHERE MaHH=? FOR UPDATE",product);
                            if(found.isEmpty()) throw new IllegalArgumentException("Không tìm thấy hàng hóa "+product+".");
                            if(sale) {
                                if(update(c,"UPDATE HANGHOA SET SoLuong=SoLuong-? WHERE MaHH=? AND SoLuong>=?",qty,product,qty)==0) throw new IllegalArgumentException("Không đủ tồn kho: "+found.get(0).get("name")+". Hóa đơn chưa được lưu.");
                                update(c,"INSERT INTO BANHANG VALUES(?,?,?,?)",id,product,qty,found.get(0).get("price"));
                            } else {
                                update(c,"INSERT INTO NHAPHANG VALUES(?,?,?,?)",id,product,qty,money(prices.get(i)));
                                update(c,"UPDATE HANGHOA SET SoLuong=SoLuong+? WHERE MaHH=?",qty,product);
                            }
                        }
                        result=id;
                    }
                    default -> throw new IllegalArgumentException("Thao tác không tồn tại.");
                }
                c.commit();return Map.of("ok",true,"code",result);
            } catch(Exception ex) { c.rollback();throw ex; }
        }
    }

    static Object report(Connection c,Map<String,List<String>> p) throws Exception {
        String kind=val(p,"kind","revenue"),sql,title;
        List<Object> args=new ArrayList<>();
        LocalDate from=date(val(p,"from","2026-01-01")),to=date(val(p,"to","2026-12-31"));
        if(to.isBefore(from)) throw new IllegalArgumentException("Ngày kết thúc phải từ ngày bắt đầu trở đi.");
        String area=val(p,"area","Hà Đông");
        switch(kind) {
            case "revenue" -> {
                title="Doanh thu bán hàng theo ngày";
                sql="SELECT h.NgayBan AS \"Ngày bán\", COUNT(DISTINCT h.MaHDB) AS \"Số hóa đơn\", SUM(b.SoLuong) AS \"Số lượng bán\", SUM(b.SoLuong*b.DonGiaBan) AS \"Doanh thu (đ)\" FROM HOADONBAN h JOIN BANHANG b ON b.MaHDB=h.MaHDB WHERE h.NgayBan BETWEEN ? AND ? GROUP BY h.NgayBan ORDER BY h.NgayBan";
                args.add(from);args.add(to);
            }
            case "inventory" -> {
                title="Hàng hóa có tồn kho dưới ngưỡng";
                sql="SELECT MaHH AS \"Mã hàng\", Ten AS \"Tên hàng\", Loai AS \"Loại\", SoLuong AS \"Tồn kho\", Gia AS \"Giá bán (đ)\", SoLuong*Gia AS \"Giá trị theo giá bán (đ)\" FROM HANGHOA WHERE SoLuong<=? ORDER BY SoLuong,MaHH";
                args.add(integer(val(p,"threshold","20"),0,1000000));
            }
            case "customers" -> {
                title="Khách mua nhiều nhất theo khu vực · câu 2 bài mẫu";
                sql="WITH T AS (SELECT k.MaKH,k.Ten,SUM(b.SoLuong) AS Tong FROM KHACHHANG k JOIN HOADONBAN h ON h.MaKH=k.MaKH JOIN BANHANG b ON b.MaHDB=h.MaHDB WHERE LOWER(k.DiaChi) LIKE ? ESCAPE '!' AND h.NgayBan BETWEEN ? AND ? GROUP BY k.MaKH,k.Ten) SELECT MaKH AS \"Mã khách\",Ten AS \"Tên khách\",Tong AS \"Số lượng mua\" FROM T WHERE Tong=(SELECT MAX(Tong) FROM T) ORDER BY MaKH";
                args.add("%"+escapeLike(area.toLowerCase(Locale.ROOT))+"%");args.add(from);args.add(to);
            }
            case "bestsellers" -> {
                title="Hàng bán nhiều nhất theo khu vực · câu 7 bài mẫu";
                sql="WITH T AS (SELECT g.MaHH,g.Ten,SUM(b.SoLuong) AS Tong FROM HANGHOA g JOIN BANHANG b ON b.MaHH=g.MaHH JOIN HOADONBAN h ON h.MaHDB=b.MaHDB JOIN KHACHHANG k ON k.MaKH=h.MaKH WHERE LOWER(k.DiaChi) LIKE ? ESCAPE '!' AND h.NgayBan BETWEEN ? AND ? GROUP BY g.MaHH,g.Ten) SELECT MaHH AS \"Mã hàng\",Ten AS \"Tên hàng\",Tong AS \"Số lượng bán\" FROM T WHERE Tong=(SELECT MAX(Tong) FROM T) ORDER BY MaHH";
                args.add("%"+escapeLike(area.toLowerCase(Locale.ROOT))+"%");args.add(from);args.add(to);
            }
            default -> throw new IllegalArgumentException("Loại báo cáo không hợp lệ.");
        }
        return Map.of("title",title,"sql",sql,"parameters",args,"rows",query(c,sql,args.toArray()));
    }

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
    static BigDecimal money(String s) {try {BigDecimal n=new BigDecimal(s);if(n.signum()<0||n.scale()>0||n.compareTo(new BigDecimal("99999999999999"))>0) throw new NumberFormatException();return n;}catch(NumberFormatException e){throw new IllegalArgumentException("Đơn giá phải là số nguyên không âm, tối đa 14 chữ số.");}}
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
