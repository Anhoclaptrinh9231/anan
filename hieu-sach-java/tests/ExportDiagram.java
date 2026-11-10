import java.nio.file.*;
import java.sql.*;
import java.util.*;

/** Sinh Data Diagram tu metadata schema thuc, khong suy doan ten cot. */
public class ExportDiagram {
    static String html(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
    public static void main(String[] args)throws Exception{
        try(Connection c=Bookstore.db()){
            String catalog=c.getCatalog();
            DatabaseMetaData m=c.getMetaData();
            List<String> tables=new ArrayList<>();
            try(ResultSet rs=m.getTables(catalog,null,"%",new String[]{"TABLE"})){while(rs.next())tables.add(rs.getString("TABLE_NAME"));}
            Collections.sort(tables);
            StringBuilder out=new StringBuilder("digraph DataDiagram {\ngraph [rankdir=LR, bgcolor=white, pad=0.4, nodesep=0.4, ranksep=1.1, fontname=Arial, label=\"DATA DIAGRAM - "+tables.size()+" BẢNG MYSQL\\nPK là khóa chính. FK là khóa ngoại.\", labelloc=t, fontsize=18];\nnode [shape=plain,fontname=Arial,fontsize=10];\nedge [color=\"#9aa99a\",arrowsize=0.6];\n");
            for(String table:tables){
                Set<String> pk=new HashSet<>(),fk=new HashSet<>();
                try(ResultSet rs=m.getPrimaryKeys(catalog,null,table)){while(rs.next())pk.add(rs.getString("COLUMN_NAME"));}
                try(ResultSet rs=m.getImportedKeys(catalog,null,table)){while(rs.next())fk.add(rs.getString("FKCOLUMN_NAME"));}
                out.append(table+" [label=<<TABLE BORDER=\"0\" CELLBORDER=\"1\" CELLSPACING=\"0\" CELLPADDING=\"7\" COLOR=\"#d3dfce\"><TR><TD COLSPAN=\"3\" BGCOLOR=\"#183e3b\"><FONT COLOR=\"white\"><B>"+table.toUpperCase(Locale.ROOT)+"</B></FONT></TD></TR>");
                try (PreparedStatement columns = c.prepareStatement("SELECT COLUMN_NAME,COLUMN_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=? AND TABLE_NAME=? ORDER BY ORDINAL_POSITION")) {
                    columns.setString(1, catalog);
                    columns.setString(2, table);
                    try (ResultSet rs = columns.executeQuery()) {
                        while (rs.next()) {
                            String col = rs.getString("COLUMN_NAME"), type = rs.getString("COLUMN_TYPE");
                            out.append("<TR><TD BGCOLOR=\"#f0f5ea\">"+(pk.contains(col)?"PK":"")+(fk.contains(col)?(pk.contains(col)?",FK":"FK"):"")+"</TD><TD ALIGN=\"LEFT\" PORT=\""+col+"\">"+html(col)+"</TD><TD ALIGN=\"LEFT\"><FONT COLOR=\"#71816d\">"+html(type)+"</FONT></TD></TR>");
                        }
                    }
                }
                out.append("</TABLE>>];\n");
            }
            for(String table:tables){try(ResultSet rs=m.getImportedKeys(catalog,null,table)){while(rs.next())out.append(rs.getString("PKTABLE_NAME")+":"+rs.getString("PKCOLUMN_NAME")+":e -> "+table+":"+rs.getString("FKCOLUMN_NAME")+":w;\n");}}
            out.append("}\n");
            Path output=args.length==0?Path.of("docs/data-diagram.dot"):Path.of(args[0]);
            Files.writeString(output,out.toString());
            System.out.println("Data diagram: "+tables.size()+" tables");
        }
    }
}
