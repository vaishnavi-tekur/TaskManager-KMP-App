package com.example.taskmanagerkmpapp
import java.nio.file.*; import java.sql.*; import java.security.MessageDigest
class Database(private val file: String = "data/taskmanager.db") {
    private val conn = run { Path.of(file).parent?.let { Files.createDirectories(it) }; DriverManager.getConnection("jdbc:sqlite:$file").also { db -> db.createStatement().use { it.executeUpdate("PRAGMA foreign_keys=ON; CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,username TEXT UNIQUE,email TEXT UNIQUE,password_hash TEXT); CREATE TABLE IF NOT EXISTS tasks(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, title TEXT, description TEXT, completed INTEGER DEFAULT 0, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)") } } }
    fun getTasks(uid: Long): List<TaskResponse> = conn.prepareStatement("SELECT * FROM tasks WHERE user_id=?").use { s -> s.setLong(1,uid); s.executeQuery().use { val l = mutableListOf<TaskResponse>(); while(it.next()) l.add(it.toT()); l } }
    fun addTask(uid: Long, t: String, d: String) = conn.prepareStatement("INSERT INTO tasks(user_id,title,description) VALUES(?,?,?)").use { s -> s.setLong(1,uid); s.setString(2,t); s.setString(3,d); s.executeUpdate()==1 }
    fun updateTask(tid: Long, uid: Long, t: String, d: String, c: Boolean) = conn.prepareStatement("UPDATE tasks SET title=?, description=?, completed=? WHERE id=? AND user_id=?").use { s -> s.setString(1,t); s.setString(2,d); s.setInt(3,if(c) 1 else 0); s.setLong(4,tid); s.setLong(5,uid); s.executeUpdate()==1 }
    fun deleteTask(tid: Long, uid: Long) = conn.prepareStatement("DELETE FROM tasks WHERE id=? AND user_id=?").use { s -> s.setLong(1,tid); s.setLong(2,uid); s.executeUpdate()==1 }
    fun register(r: RegisterRequest) = conn.prepareStatement("INSERT INTO users(name,username,email,password_hash) VALUES(?,?,?,?)").use { s -> s.setString(1,r.name); s.setString(2,r.username); s.setString(3,r.email.lowercase()); s.setString(4,hash(r.password)); try { s.executeUpdate(); find(r.username) } catch (e: Exception) { null } }
    fun authenticate(u: String, p: String) = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password_hash=?").use { s -> s.setString(1,u); s.setString(2,hash(p)); s.executeQuery().use { if (it.next()) it.toU() else null } }
    fun resetPassword(e: String, p: String) = conn.prepareStatement("UPDATE users SET password_hash=? WHERE email=?").use { s -> s.setString(1,hash(p)); s.setString(2,e.lowercase()); s.executeUpdate()==1 }
    fun deleteUser(uid: Long) = conn.prepareStatement("DELETE FROM users WHERE id=?").use { s -> s.setLong(1,uid); s.executeUpdate()==1 }
    fun getOrCreateByEmail(email: String, name: String): User? = findByEmail(email) ?: register(RegisterRequest(name, email.substringBefore("@"), email, "bhrish_auto_pass"))
    private fun findByEmail(email: String): User? = conn.prepareStatement("SELECT * FROM users WHERE email=?").use { s -> s.setString(1, email.lowercase()); s.executeQuery().use { if (it.next()) it.toU() else null } }
    fun find(u: String) = conn.prepareStatement("SELECT * FROM users WHERE username=?").use { s -> s.setString(1,u); s.executeQuery().use { if (it.next()) it.toU() else null } }
    private fun ResultSet.toU() = User(getLong("id"),getString("name"),getString("username"),getString("email"))
    private fun ResultSet.toT() = TaskResponse(getLong("id"), getString("title"), getString("description"), getInt("completed") == 1)
    private fun hash(v: String) = MessageDigest.getInstance("SHA-256").digest(v.toByteArray()).joinToString("") { "%02x".format(it) }
}
