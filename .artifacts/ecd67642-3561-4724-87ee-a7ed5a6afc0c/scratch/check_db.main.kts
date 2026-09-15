@file:DependsOn("org.xerial:sqlite-jdbc:3.50.3.0")
import java.sql.DriverManager

fun check(file: String) {
    println("Checking $file...")
    try {
        DriverManager.getConnection("jdbc:sqlite:$file").use { conn ->
            conn.createStatement().executeQuery("SELECT * FROM users").use { rs ->
                while (rs.next()) {
                    println("User: ${rs.getString("username")} (${rs.getString("email")})")
                }
            }
            conn.createStatement().executeQuery("SELECT * FROM tasks").use { rs ->
                while (rs.next()) {
                    println("Task: ${rs.getString("title")} (User ID: ${rs.getInt("user_id")})")
                }
            }
        }
    } catch (e: Exception) {
        println("Error checking $file: ${e.message}")
    }
}

check("data/taskmanager.db")
check("backend/data/taskmanager.db")
