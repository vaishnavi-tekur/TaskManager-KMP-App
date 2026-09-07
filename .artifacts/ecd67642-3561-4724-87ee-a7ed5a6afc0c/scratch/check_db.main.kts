@file:DependsOn("org.xerial:sqlite-jdbc:3.50.3.0")
import java.sql.DriverManager
import java.io.File

fun check(filePath: String) {
    val dbFile = File(filePath)
    if (!dbFile.exists()) {
        println("File not found: ${dbFile.absolutePath}")
        return
    }
    
    println("\n--- Checking: ${dbFile.absolutePath} ---")
    try {
        DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}").use { conn ->
            println("Table: users")
            conn.createStatement().executeQuery("SELECT id, name, username, email FROM users").use { rs ->
                while (rs.next()) {
                    println("  ID: ${rs.getInt("id")}, User: ${rs.getString("username")} (${rs.getString("name")})")
                }
            }
            
            println("\nTable: tasks")
            conn.createStatement().executeQuery("SELECT title, user_id, is_completed FROM tasks").use { rs ->
                while (rs.next()) {
                    println("  Task: ${rs.getString("title")} (UID: ${rs.getInt("user_id")}) - Done: ${rs.getInt("is_completed") == 1}")
                }
            }
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

// Updated with absolute paths for your project
check("D:/TaskManagerKMPApp/data/taskmanager.db")
check("D:/TaskManagerKMPApp/backend/data/taskmanager.db")
