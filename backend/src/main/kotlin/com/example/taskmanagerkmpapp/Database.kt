package com.example.taskmanagerkmpapp

import java.nio.file.*
import java.sql.*
import java.security.MessageDigest

class Database(file: String = "data/taskmanager.db") {
    private val conn = createConnection(file)

    private fun createConnection(filePath: String): Connection {
        val path = Path.of(filePath)
        path.parent?.let { Files.createDirectories(it) }

        return try {
            initConnection(filePath)
        } catch (e: Exception) {
            val isCorrupt = e.message?.contains("SQLITE_CORRUPT", ignoreCase = true) == true ||
                            e.message?.contains("malformed", ignoreCase = true) == true
            
            if (isCorrupt) {
                println("DATABASE CORRUPTION DETECTED: ${e.message}. Re-creating clean database file...")
                System.gc()
                
                try {
                    Files.deleteIfExists(path)
                    Files.deleteIfExists(Path.of("$filePath-journal"))
                    Files.deleteIfExists(Path.of("$filePath-wal"))
                    Files.deleteIfExists(Path.of("$filePath-shm"))
                } catch (delEx: Exception) {
                    println("Failed to delete corrupt db files via Files: ${delEx.message}")
                }

                try {
                    val f = path.toFile()
                    if (f.exists()) f.delete()
                } catch (_: Exception) {}

                initConnection(filePath)
            } else {
                println("DATABASE INIT ERROR: ${e.message}")
                throw e
            }
        }
    }

    private fun initConnection(filePath: String): Connection {
        val db = DriverManager.getConnection("jdbc:sqlite:$filePath")
        try {
            db.createStatement().use { statement ->
                statement.execute("PRAGMA foreign_keys=ON;")
                statement.execute(
                    "CREATE TABLE IF NOT EXISTS users(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, username TEXT UNIQUE, email TEXT UNIQUE, password_hash TEXT, is_active INTEGER DEFAULT 1);"
                )
                statement.execute(
                    "CREATE TABLE IF NOT EXISTS tasks(id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, title TEXT, description TEXT, priority TEXT DEFAULT 'Medium', completed INTEGER DEFAULT 0, due_date INTEGER, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE);"
                )
            }
            return db
        } catch (e: Exception) {
            try { db.close() } catch (_: Exception) {}
            throw e
        }
    }

    fun getTasks(uid: Long): List<TaskResponse> = 
        conn.prepareStatement("SELECT * FROM tasks WHERE user_id=?").use { s -> 
            s.setLong(1,uid)
            s.executeQuery().use { 
                val l = mutableListOf<TaskResponse>()
                while(it.next()) l.add(it.toT())
                l 
            } 
        }

    fun addTask(uid: Long, t: String, d: String, p: String, due: Long?) = 
        conn.prepareStatement("INSERT INTO tasks(user_id,title,description,priority,due_date) VALUES(?,?,?,?,?)").use { s -> 
            s.setLong(1,uid)
            s.setString(2,t)
            s.setString(3,d)
            s.setString(4,p)
            if (due != null) s.setLong(5, due) else s.setNull(5, Types.INTEGER)
            s.executeUpdate()==1 
        }

    fun updateTask(tid: Long, uid: Long, t: String, d: String, p: String, c: Boolean, due: Long?) = 
        conn.prepareStatement("UPDATE tasks SET title=?, description=?, priority=?, completed=?, due_date=? WHERE id=? AND user_id=?").use { s -> 
            s.setString(1,t)
            s.setString(2,d)
            s.setString(3,p)
            s.setInt(4,if(c) 1 else 0)
            if (due != null) s.setLong(5, due) else s.setNull(5, Types.INTEGER)
            s.setLong(6,tid)
            s.setLong(7,uid)
            s.executeUpdate()==1 
        }

    fun deleteTask(tid: Long, uid: Long) = 
        conn.prepareStatement("DELETE FROM tasks WHERE id=? AND user_id=?").use { s -> 
            s.setLong(1,tid)
            s.setLong(2,uid)
            s.executeUpdate()==1 
        }

    fun register(r: RegisterRequest) = 
        conn.prepareStatement("INSERT INTO users(name,username,email,password_hash,is_active) VALUES(?,?,?,?,1)").use { s -> 
            s.setString(1,r.name)
            s.setString(2,r.username)
            s.setString(3,r.email.lowercase())
            s.setString(4,hash(r.password))
            try { 
                s.executeUpdate()
                find(r.username) 
            } catch (e: Exception) { null } 
        }

    fun authenticate(u: String, p: String) = 
        conn.prepareStatement("SELECT * FROM users WHERE (username=? OR email=?) AND password_hash=? AND (is_active=1 OR is_active='true')").use { s -> 
            s.setString(1,u)
            s.setString(2,u.lowercase())
            s.setString(3,hash(p))
            s.executeQuery().use { if (it.next()) it.toU() else null } 
        }

    fun resetPassword(e: String, p: String) = 
        conn.prepareStatement("UPDATE users SET password_hash=? WHERE email=?").use { s -> 
            s.setString(1,hash(p))
            s.setString(2,e.lowercase())
            s.executeUpdate()==1 
        }

    fun deleteUser(uid: Long) = 
        conn.prepareStatement("UPDATE users SET is_active = 0 WHERE id = ?").use { s -> 
            s.setLong(1,uid)
            s.executeUpdate()==1 
        }

    fun getOrCreateByEmail(email: String, name: String): User? {
        val existing = findByEmail(email)
        if (existing != null) {
            if (!existing.isActive) {
                conn.prepareStatement("UPDATE users SET is_active = 1 WHERE id = ?").use { s ->
                    s.setLong(1, existing.id)
                    s.executeUpdate()
                }
                return findByEmail(email)
            }
            return existing
        }
        return register(RegisterRequest(name, email.replace("@", "_").replace(".", "_"), email, "bhrish_auto_pass"))
    }

    fun findByEmail(email: String): User? = 
        conn.prepareStatement("SELECT * FROM users WHERE email=? AND (is_active=1 OR is_active='true')").use { s -> 
            s.setString(1, email.lowercase())
            s.executeQuery().use { if (it.next()) it.toU() else null } 
        }

    fun find(u: String) = 
        conn.prepareStatement("SELECT * FROM users WHERE (username=? OR email=?) AND (is_active=1 OR is_active='true')").use { s -> 
            s.setString(1,u)
            s.setString(2,u.lowercase())
            s.executeQuery().use { if (it.next()) it.toU() else null } 
        }

    private fun ResultSet.toU() = User(
        getLong("id"),
        getString("name"),
        getString("username"),
        getString("email"),
        getObject("is_active")?.toString().let { it == "1" || it == "true" }
    )

    private fun ResultSet.toT() = TaskResponse(
        getLong("id"), 
        getString("title"), 
        getString("description"), 
        getString("priority") ?: "Medium",
        getInt("completed") == 1,
        getLong("due_date").let { if (wasNull()) null else it }
    )

    private fun hash(v: String) = MessageDigest.getInstance("SHA-256").digest(v.toByteArray()).joinToString("") { "%02x".format(it) }
}
