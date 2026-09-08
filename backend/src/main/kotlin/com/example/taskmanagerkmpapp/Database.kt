package com.example.taskmanagerkmpapp
import java.nio.file.*
import java.sql.*
import java.security.MessageDigest

class Database(private val file: String = "data/taskmanager.db") {
    private val conn = run {
        Path.of(file).parent?.let { Files.createDirectories(it) }
        DriverManager.getConnection("jdbc:sqlite:$file").also { db -> db.createStatement().use { it.executeUpdate("PRAGMA foreign_keys=ON; CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,username TEXT UNIQUE,email TEXT UNIQUE,password_hash TEXT); CREATE TABLE IF NOT EXISTS tasks(id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER,title TEXT,description TEXT,priority TEXT,is_completed INTEGER DEFAULT 0,FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)") } }
    }
    fun register(r: RegisterRequest) = conn.prepareStatement("INSERT INTO users(name,username,email,password_hash) VALUES(?,?,?,?)").use { s -> s.setString(1,r.name); s.setString(2,r.username); s.setString(3,r.email.lowercase()); s.setString(4,hash(r.password)); try { s.executeUpdate(); find(r.username) } catch (e: Exception) { null } }
    // Update this function to search both columns
    fun authenticate(u: String, p: String) =
        conn.prepareStatement("SELECT * FROM users WHERE (username=? OR email=?) AND password_hash=?").use { s ->
            s.setString(1, u)
            s.setString(2, u.lowercase()) // Check email column (case-insensitive)
            s.setString(3, hash(p))
            s.executeQuery().use { if (it.next()) it.toU() else null }
        }
    fun resetPassword(e: String, p: String) = conn.prepareStatement("UPDATE users SET password_hash=? WHERE email=?").use { s -> s.setString(1,hash(p)); s.setString(2,e.lowercase()); s.executeUpdate()==1 }
    fun tasks(uid: Long) = conn.prepareStatement("SELECT * FROM tasks WHERE user_id=?").use { s -> s.setLong(1,uid); s.executeQuery().use { r -> buildList { while (r.next()) add(r.toT()) } } }
    fun addTask(uid: Long, t: TaskRequest) = conn.prepareStatement("INSERT INTO tasks(user_id,title,description,priority) VALUES(?,?,?,?)", arrayOf("id")).use { s -> s.setLong(1,uid); s.setString(2,t.title); s.setString(3,t.description); s.setString(4,t.priority); s.executeUpdate(); s.generatedKeys.use { if (it.next()) Task(it.getLong(1),t.title,t.description,t.priority,false) else null } }
    fun updateTask(uid: Long, id: Long, c: Boolean) = conn.prepareStatement("UPDATE tasks SET is_completed=? WHERE id=? AND user_id=?").use { s -> s.setInt(1,if(c) 1 else 0); s.setLong(2,id); s.setLong(3,uid); s.executeUpdate()==1 }
    fun deleteTask(uid: Long, id: Long) = conn.prepareStatement("DELETE FROM tasks WHERE id=? AND user_id=?").use { s -> s.setLong(1,id); s.setLong(2,uid); s.executeUpdate()==1 }
    fun deleteUser(uid: Long) = conn.prepareStatement("DELETE FROM users WHERE id=?").use { s -> s.setLong(1,uid); s.executeUpdate()==1 }
    fun getOrCreateByEmail(email: String, name: String): User? {
        val existing = findByEmail(email)
        if (existing != null) return existing

        // Create new user automatically if they have the right domain
        val username = email.substringBefore("@")
        return register(RegisterRequest(name, username, email, "bhrish_auto_pass"))
    }

    private fun findByEmail(email: String): User? =
        conn.prepareStatement("SELECT * FROM users WHERE email=?").use { s ->
            s.setString(1, email.lowercase())
            s.executeQuery().use { if (it.next()) it.toU() else null }
        }
    private fun find(u: String) = conn.prepareStatement("SELECT * FROM users WHERE username=?").use { s -> s.setString(1,u); s.executeQuery().use { if (it.next()) it.toU() else null } }
    private fun ResultSet.toU() = User(getLong("id"),getString("name"),getString("username"),getString("email"))
    private fun ResultSet.toT() = Task(getLong("id"),getString("title"),getString("description"),getString("priority"),getInt("is_completed")==1)
    private fun hash(v: String) = MessageDigest.getInstance("SHA-256").digest(v.toByteArray()).joinToString("") { "%02x".format(it) }
}
