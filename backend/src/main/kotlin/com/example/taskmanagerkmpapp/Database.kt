package com.example.taskmanagerkmpapp

import java.nio.file.Files
import java.nio.file.Path
import java.sql.DriverManager
import java.security.MessageDigest

class Database(private val file: String = "data/taskmanager.db") {
    private val connection = DriverManager.getConnection("jdbc:sqlite:$file").also { db ->
        Path.of(file).parent?.let { Files.createDirectories(it) }
        db.createStatement().use { it.executeUpdate("PRAGMA foreign_keys=ON"); it.executeUpdate("CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,username TEXT UNIQUE NOT NULL,email TEXT UNIQUE NOT NULL,password_hash TEXT NOT NULL)"); it.executeUpdate("CREATE TABLE IF NOT EXISTS tasks(id INTEGER PRIMARY KEY AUTOINCREMENT,user_id INTEGER NOT NULL,title TEXT NOT NULL,description TEXT NOT NULL,priority TEXT NOT NULL,is_completed INTEGER NOT NULL DEFAULT 0,FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)") }
    }

    fun register(r: RegisterRequest): User? = connection.prepareStatement("INSERT INTO users(name,username,email,password_hash) VALUES(?,?,?,?)").use { s ->
        s.setString(1,r.name.trim()); s.setString(2,r.username.trim()); s.setString(3,r.email.trim().lowercase()); s.setString(4,hash(r.password)); try { s.executeUpdate(); findUser(r.username) } catch (_: Exception) { null }
    }

    fun authenticate(username: String, password: String): User? = connection.prepareStatement("SELECT id,name,username,email FROM users WHERE username=? AND password_hash=?").use { s -> s.setString(1,username.trim()); s.setString(2,hash(password)); s.executeQuery().use { if (it.next()) it.toUser() else null } }

    fun resetPassword(email: String, password: String) = connection.prepareStatement("UPDATE users SET password_hash=? WHERE email=?").use { s -> s.setString(1,hash(password)); s.setString(2,email.trim().lowercase()); s.executeUpdate()==1 }

    fun tasks(userId: Long): List<Task> = connection.prepareStatement("SELECT id,title,description,priority,is_completed FROM tasks WHERE user_id=? ORDER BY id").use { s -> s.setLong(1,userId); s.executeQuery().use { r -> buildList { while (r.next()) add(r.toTask()) } } }

    fun addTask(userId: Long, t: TaskRequest): Task? = connection.prepareStatement("INSERT INTO tasks(user_id,title,description,priority) VALUES(?,?,?,?)",arrayOf("id")).use { s -> s.setLong(1,userId); s.setString(2,t.title); s.setString(3,t.description); s.setString(4,t.priority); s.executeUpdate(); s.generatedKeys.use { if (it.next()) Task(it.getLong(1),t.title,t.description,t.priority,false) else null } }

    fun updateTask(userId: Long,id: Long,completed: Boolean) = connection.prepareStatement("UPDATE tasks SET is_completed=? WHERE id=? AND user_id=?").use { s -> s.setInt(1,if(completed) 1 else 0); s.setLong(2,id); s.setLong(3,userId); s.executeUpdate()==1 }

    fun deleteTask(userId: Long,id: Long) = connection.prepareStatement("DELETE FROM tasks WHERE id=? AND user_id=?").use { s -> s.setLong(1,id); s.setLong(2,userId); s.executeUpdate()==1 }

    private fun findUser(username: String): User? = connection.prepareStatement("SELECT id,name,username,email FROM users WHERE username=?").use { s -> s.setString(1,username.trim()); s.executeQuery().use { if (it.next()) it.toUser() else null } }

    private fun java.sql.ResultSet.toUser() = User(getLong("id"),getString("name"),getString("username"),getString("email"))
    private fun java.sql.ResultSet.toTask() = Task(getLong("id"),getString("title"),getString("description"),getString("priority"),getInt("is_completed")==1)
    private fun hash(value: String) = MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it) }
}
