package com.example.taskmanagerkmpapp
import androidx.compose.foundation.background; import androidx.compose.foundation.layout.*; import androidx.compose.foundation.lazy.*; import androidx.compose.foundation.shape.RoundedCornerShape; import androidx.compose.material.icons.Icons; import androidx.compose.material.icons.filled.*; import androidx.compose.material3.*; import androidx.compose.runtime.*; import androidx.compose.ui.*; import androidx.compose.ui.graphics.Color; import androidx.compose.ui.text.font.FontWeight; import androidx.compose.ui.text.input.*; import androidx.compose.ui.text.style.TextAlign; import androidx.compose.ui.unit.*; import kotlinx.coroutines.*
import org.jetbrains.compose.resources.painterResource
import taskmanagerkmpapp.shared.generated.resources.Res
import taskmanagerkmpapp.shared.generated.resources.ic_google

@Composable internal fun WelcomeScreen(user: User?, blue: Color, scope: CoroutineScope, onLogout: () -> Unit, onDeleteAccount: () -> Unit) {
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Text("Welcome, ${user?.name ?: "User"}!", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = blue); Spacer(Modifier.height(24.dp))
        Button(onLogout, Modifier.width(200.dp), colors = ButtonDefaults.buttonColors(blue)) { Text("Logout") }; Spacer(Modifier.height(12.dp)); TextButton(onDeleteAccount) { Text("Delete Account", color = Color.Red) }
    }
}
@Composable internal fun AuthScreen(screen: String, blue: Color, scope: CoroutineScope, storage: SessionStorage, onNavigate: (String) -> Unit, onLoginSuccess: (User) -> Unit) {
    var u by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var n by remember { mutableStateOf("") }; var cp by remember { mutableStateOf("") }; var vis by remember { mutableStateOf(false) }; var err by remember { mutableStateOf("") }; var load by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), Alignment.Center) {
        Card(Modifier.fillMaxWidth().padding(24.dp), RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color.White)) {
            Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if(screen == "login") "Welcome Back" else "Create Account", fontSize = 22.sp, fontWeight = FontWeight.Bold); Text(if(screen == "login") "Sign in below" else "Sign up now", color = Color.Gray, fontSize = 14.sp)
                if (screen != "login") { Text("Full Name", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp); OutlinedTextField(n, { n = it }, Modifier.fillMaxWidth(), placeholder = { Text("Name") }) }
                Text("Email", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp); OutlinedTextField(u, { u = it }, Modifier.fillMaxWidth(), placeholder = { Text("Email") })
                Text("Password", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp); OutlinedTextField(p, { p = it }, Modifier.fillMaxWidth(), placeholder = { Text("Password") }, visualTransformation = if (vis) VisualTransformation.None else PasswordVisualTransformation(), trailingIcon = { IconButton({ vis = !vis }) { Icon(if (vis) Icons.Default.Visibility else Icons.Default.VisibilityOff, null) } })
                if (screen != "login") { Text("Confirm Password", Modifier.align(Alignment.Start), fontWeight = FontWeight.Bold, fontSize = 14.sp); OutlinedTextField(cp, { cp = it }, Modifier.fillMaxWidth(), placeholder = { Text("Confirm") }, visualTransformation = PasswordVisualTransformation()) }
                if (err.isNotEmpty()) Text(err, color = Color.Red, fontSize = 12.sp, textAlign = TextAlign.Center)
                if (screen == "login") Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) { Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(false, {}) ; Text("Remember me", fontSize = 12.sp, color = Color.Gray) }; TextButton({ onNavigate("forgotPassword") }) { Text("Forgot password?", color = blue, fontSize = 12.sp, fontWeight = FontWeight.Bold) } }
                Button({ if (screen != "login" && p != cp) { err = "Mismatch"; return@Button }; load = true; scope.launch { val res = if (screen == "login") Repo.login(u, p) else Repo.register(n, u.substringBefore("@"), u, p); if (res is AuthResponse.Success) { storage.save(res.auth.user.username, res.auth.user.name, res.auth.user.email, res.auth.token); Repo.token = res.auth.token; onLoginSuccess(User(res.auth.user.name, res.auth.user.username, res.auth.user.email)) } else err = (res as AuthResponse.Error).message; load = false } }, Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(blue)) { Text(if (load) "Loading..." else if (screen == "login") "Login" else "Register") }
                if (screen == "login") {
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { HorizontalDivider(Modifier.weight(1f)) ; Text(" or ", color = Color.LightGray) ; HorizontalDivider(Modifier.weight(1f)) }
                    OutlinedButton({
                        if (load) return@OutlinedButton; err = ""; load = true
                        googleLogin(scope) { res ->
                            if (res != null) scope.launch {
                                val a = Repo.login(res, "google_login", true)
                                if (a is AuthResponse.Success) {
                                    storage.save(a.auth.user.username, a.auth.user.name, a.auth.user.email, a.auth.token)
                                    Repo.token = a.auth.token; onLoginSuccess(User(a.auth.user.name, a.auth.user.username, a.auth.user.email))
                                } else err = (a as AuthResponse.Error).message; load = false
                            } else { err = "Google Login Cancelled"; load = false }
                        }
                    }, Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                        Icon(painterResource(Res.drawable.ic_google), null, Modifier.size(20.dp), tint = Color.Unspecified)
                        Spacer(Modifier.width(12.dp)); Text("Continue with Google", color = Color.Black)
                    }
                }
                TextButton({ onNavigate(if (screen == "login") "register" else "login"); err = "" }) { Text(if (screen == "login") "New user? Register" else "Have an account? Login", color = Color.Gray, fontSize = 12.sp) }
            }
        }
    }
}
@Composable internal fun ForgotPasswordScreen(blue: Color, scope: CoroutineScope, onNavigate: (String) -> Unit) {
    var e by remember { mutableStateOf("") }; var np by remember { mutableStateOf("") }; var cp by remember { mutableStateOf("") }; var msg by remember { mutableStateOf("") }; var err by remember { mutableStateOf("") }; var load by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().background(Color(0xFFF5F5F5)), Alignment.Center) {
        Card(Modifier.fillMaxWidth().padding(24.dp), RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(Color.White)) {
            Column(Modifier.padding(24.dp), Arrangement.spacedBy(12.dp)) {
                Text("Reset Password", color = blue, fontSize = 28.sp, fontWeight = FontWeight.Bold); OutlinedTextField(e, { e = it }, Modifier.fillMaxWidth(), label = { Text("Email") }); OutlinedTextField(np, { np = it }, Modifier.fillMaxWidth(), label = { Text("New Pass") }, visualTransformation = PasswordVisualTransformation()); OutlinedTextField(cp, { cp = it }, Modifier.fillMaxWidth(), label = { Text("Confirm") }, visualTransformation = PasswordVisualTransformation())
                if (err.isNotEmpty()) Text(err, color = Color.Red, fontSize = 12.sp); if (msg.isNotEmpty()) Text(msg, color = Color.Green, fontSize = 12.sp)
                Button({ if (np != cp) { err = "Mismatch"; return@Button }; load = true; scope.launch { if (Repo.reset(e, np)) { msg = "Success!"; delay(1500); onNavigate("login") } else err = "Failed"; load = false } }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(blue)) { Text(if (load) "Wait..." else "Reset Password") }; TextButton({ onNavigate("login") }, Modifier.align(Alignment.CenterHorizontally)) { Text("Back to Login") }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun TaskListScreen(user: User?, blue: Color, scope: CoroutineScope, onLogout: () -> Unit, onAddTask: () -> Unit, onDeleteAccount: () -> Unit) {
    var tasks by remember { mutableStateOf(emptyList<Task>()) }; var load by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) { tasks = Repo.getTasks(); load = false }
    Scaffold(topBar={TopAppBar(title={Column{Text("My Tasks",fontSize=20.sp,fontWeight=FontWeight.Bold);Text(user?.name?:"",fontSize=12.sp,color=Color.Gray)}},actions={IconButton(onLogout){Icon(Icons.Default.ExitToApp,null,tint=blue)};IconButton(onDeleteAccount){Icon(Icons.Default.Delete,null,tint=Color.Red)}})},floatingActionButton={FloatingActionButton(onAddTask,containerColor=blue,contentColor=Color.White){Icon(Icons.Default.Add,null)}}){ p ->
        Box(Modifier.padding(p).fillMaxSize()){
            if(load) CircularProgressIndicator(Modifier.align(Alignment.Center),color=blue)
            else if(tasks.isEmpty()) Column(Modifier.align(Alignment.Center),horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.List,null,Modifier.size(64.dp),Color.LightGray);Text("No tasks yet",color=Color.Gray)}
            else Column{
                if(tasks.isNotEmpty()) Card(Modifier.fillMaxWidth().padding(16.dp),colors=CardDefaults.cardColors(blue.copy(0.1f))){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Text("Progress",fontWeight=FontWeight.Bold,color=blue);Spacer(Modifier.weight(1f));Text("${tasks.count{it.completed}} / ${tasks.size} completed",color=blue)}}
                LazyColumn(Modifier.fillMaxSize().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){ items(tasks.size){ i-> val t=tasks[i]; Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp),colors=CardDefaults.cardColors(Color.White),elevation=CardDefaults.cardElevation(2.dp)){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Checkbox(t.completed,{c->scope.launch{if(Repo.updateTask(t.copy(completed=c)))tasks=Repo.getTasks()}});Column(Modifier.weight(1f).padding(horizontal=12.dp)){Text(t.title,fontWeight=FontWeight.Bold,fontSize=16.sp,textDecoration=if(t.completed)androidx.compose.ui.text.style.TextDecoration.LineThrough else null);if(t.description.isNotEmpty())Text(t.description,fontSize=14.sp,color=Color.Gray)};IconButton({scope.launch{if(Repo.deleteTask(t.id))tasks=Repo.getTasks()}}){Icon(Icons.Default.Delete,null,tint=Color.LightGray)}}}}}
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun AddTaskScreen(blue: Color, scope: CoroutineScope, onBack: () -> Unit) {
    var t by remember { mutableStateOf("") }; var d by remember { mutableStateOf("") }; var load by remember { mutableStateOf(false) }
    Scaffold(topBar={TopAppBar(title={Text("New Task")},navigationIcon={IconButton(onBack){Icon(Icons.Default.ArrowBack,null)}})}){ p ->
        Column(Modifier.padding(p).padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
            OutlinedTextField(t,{t=it},Modifier.fillMaxWidth(),label={Text("Title")});OutlinedTextField(d,{d=it},Modifier.fillMaxWidth(),label={Text("Desc")},minLines=3)
            Button({if(t.isBlank())return@Button;load=true;scope.launch{if(Repo.addTask(t,d))onBack();load=false}},Modifier.fillMaxWidth().height(50.dp),shape=RoundedCornerShape(25.dp),colors=ButtonDefaults.buttonColors(blue)){Text(if(load)"Saving..." else "Create Task")}
        }
    }
}
