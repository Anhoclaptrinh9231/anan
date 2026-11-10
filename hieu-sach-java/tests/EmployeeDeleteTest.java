import java.util.*;
public class EmployeeDeleteTest {
 public static void main(String[]a)throws Exception{
  MysqlTests.initialize();
  Bookstore.post("/api/employees/delete",Map.of("code",List.of("NV03")));
  @SuppressWarnings("unchecked") var list=(List<Map<String,Object>>)Bookstore.get("/api/employees",Map.of());
  if(list.stream().anyMatch(n->n.get("code").equals("NV03")))throw new AssertionError("Deleted employee remains selectable");
  @SuppressWarnings("unchecked") var imports=(List<Map<String,Object>>)Bookstore.get("/api/purchases",Map.of());
  if(imports.size()!=2||imports.stream().noneMatch(i->i.get("employee").equals("Đoàn Mạnh Sơn")))throw new AssertionError("Import history lost");
  var p=new HashMap<String,List<String>>();String[] v={"branch","CN01","employee","NV03","supplier","NCC01","date","2026-10-30","product","S004","quantity","1","price","10000"};for(int i=0;i<v.length;i+=2)p.put(v[i],List.of(v[i+1]));
  try{Bookstore.post("/api/purchases",p);throw new AssertionError("Deleted employee accepted");}catch(IllegalArgumentException expected){}
  System.out.println("PASS: NV03 disappears, old import history remains, new documents reject deleted staff.");
 }
}
