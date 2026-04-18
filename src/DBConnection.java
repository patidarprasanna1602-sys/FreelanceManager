import java.sql.*;

public class DBConnection {
    public static Connection getConnection() {
        try {
            Class.forName("org.postgresql.Driver");  // 🔥 ADD THIS

            Connection con = DriverManager.getConnection(
                "jdbc:postgresql://ep-frosty-cake-ao2fzuul-pooler.c-2.ap-southeast-1.aws.neon.tech:5432/neondb?sslmode=require",
                "neondb_owner",
                "npg_eHPJG2n1qxip"
            );

            System.out.println("Connected to Neon DB ");
            return con;

        } catch (Exception e) {
            System.out.println("DB ERROR:");
            e.printStackTrace();
            return null;
        }
    }
}