import java.nio.file.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import org.h2.tools.RunScript;

/** Sinh Data Diagram tu metadata schema thuc, khong suy doan ten cot. */
public class ExportDiagram {
    static String html(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    public static void main(String[] args)throws Exception{
        try(Connection c=DriverManager.getConnection("jdbc:h2:mem:diagram","sa","")){
            try(Reader r=Files.newBufferedReader(Path.of("sql/schema.sql"))){RunScript.execute(c,r);}
            DatabaseMetaData m=c.getMetaData();
            StringBuilder out=new StringBuilder("digraph DataDiagram {\ngraph [rankdir=LR, bgcolor=white, pad=0.4, nodesep=0.4, ranksep=1.1, fontname=Arial, label=\"DATA DIAGRAM - 20 BẢNG H2\\nSinh từ metadata schema.sql; PK: khóa chính, FK: khóa ngoại; cạnh đi từ bảng cha tới bảng tham chiếu\", labelloc=t, fontsize=18];\nnode [shape=plain,fontname=Arial,fontsize=10];\nedge [color=\"#9aa99a\",arrowsize=0.6];\n");
            List<String> tables=new ArrayList<>();
            try(ResultSet rs=m.getTables(null,"PUBLIC",null,new String[]{"BASE TABLE"})){while(rs.next())tables.add(rs.getString("TABLE_NAME"));}
            Collections.sort(tables);
            for(String table:tables){
                Set<String> pk=new HashSet<>(),fk=new HashSet<>();
                try(ResultSet rs=m.getPrimaryKeys(null,"PUBLIC",table)){while(rs.next())pk.add(rs.getString("COLUMN_NAME"));}
                try(ResultSet rs=m.getImportedKeys(null,"PUBLIC",table)){while(rs.next())fk.add(rs.getString("FKCOLUMN_NAME"));}
                out.append(table+" [label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"7\" COLOR=\"#d3dfce\"><TR><TD COLSPAN=\"3\" BGCOLOR=\"#183e3b\"><FONT COLOR=\"white\"><B>"+table+"</B></FONT></TD></TR>");
                try(ResultSet rs=m.getColumns(null,"PUBLIC",table,null)){while(rs.next()){
                    String col=rs.getString("COLUMN_NAME"),type=rs.getString("TYPE_NAME");
                    out.append("<TR><TD BGCOLOR=\"#f0f5ea\">"+(pk.contains(col)?"PK":"")+(fk.contains(col)?(pk.contains(col)?",FK":"FK"):"")+"</TD><TD ALIGN=\"LEFT\" PORT=\""+col+"\">"+html(col)+"</TD><TD ALIGN=\"LEFT\"><FONT COLOR=\"#71816d\">"+html(type)+"</FONT></TD></TR>");
                }}
                out.append("</TABLE>>];\n");
            }
            for(String table:tables){try(ResultSet rs=m.getImportedKeys(null,"PUBLIC",table)){while(rs.next())out.append(rs.getString("PKTABLE_NAME")+":"+rs.getString("PKCOLUMN_NAME")+":e -> "+table+":"+rs.getString("FKCOLUMN_NAME")+":w;\n");}}
            out.append("}\n");Files.writeString(Path.of("docs/data-diagram.dot"),out.toString());
            System.out.println("Data diagram: "+tables.size()+" tables");
        }
    }
}
