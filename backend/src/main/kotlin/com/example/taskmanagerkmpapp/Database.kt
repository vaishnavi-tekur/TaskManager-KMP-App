package com.example.taskmanagerkmpapp
import java.nio.file.*
import java.sql.*
import java.security.MessageDigest

class Database(private val file: String = "data/taskmanager.db") {
    private val conn = run {
        Path.of(file).parent?.let { Files.createDirectories(it) }
        DriverManager.getConnection("jdbc:sqlite:$file").also { db -> db.createStatement().use { it.executeUpdate("PRAGMA foreign_keys=ON; CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT,username TEXT UNIQUE,email TEXT UNIQUE,password_hash TEXT)") } }
    }
    fun register(r: RegisterRequest) = conn.prepareStatement("INSERT INTO users(name,username,email,password_hash) VALUES(?,?,?,?)").use { s -> s.setString(1,r.name); s.setString(2,r.username); s.setString(3,r.email.lowercase()); s.setString(4,hash(r.password)); try { s.executeUpdate(); find(r.username) } catch (e: Exception) { null } }
    fun authenticate(u: String, p: String) = conn.prepareStatement("SELECT * FROM users WHERE username=? AND password_hash=?").use { s -> s.setString(1,u); s.setString(2,hash(p)); s.executeQuery().use { if (it.next()) it.toU() else null } }
    private fun find(u: String) = conn.prepareStatement("SELECT * FROM users WHERE username=?").use { s -> s.setString(1,u); s.executeQuery().use { if (it.next()) it.toU() else null } }
    private fun ResultSet.toU() = User(getLong("id"),getString("name"),getString("username"),getString("email"))
    private fun hash(v: String) = MessageDigest.getInstance("SHA-256").digest(v.toByteArray()).joinToString("") { "%02x".format(it) }
}
