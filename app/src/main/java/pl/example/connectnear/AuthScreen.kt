package pl.example.connectnear

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val scope = rememberCoroutineScope()
    val savedEmail by preferenceManager.savedEmail.collectAsState(initial = "")

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // --- GOOGLE SIGN IN ---
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    val idToken = account.idToken!!
                    isLoading = true
                    FirebaseService.signInWithGoogle(idToken,
                        onSuccess = { 
                            isLoading = false
                            onLoginSuccess()
                        },
                        onError = { error -> 
                            isLoading = false
                            errorMessage = "Błąd: $error"
                        }
                    )
                } catch (e: ApiException) {
                    errorMessage = "Błąd Google API: ${e.statusCode}"
                }
            }
        }
    )

    val webClientId = stringResource(R.string.default_web_client_id)
    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember(context, gso) { GoogleSignIn.getClient(context, gso) }

    val primaryPurple = Color(0xFFC67CFF)
    val buttonBlue = Color(0xFF7B96FF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryPurple)
    ) {
        // Górna sekcja z Logo
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(110.dp),
                shape = RoundedCornerShape(28.dp),
                color = Color.White.copy(alpha = 0.95f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("Vibe", color = buttonBlue, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "VibeApp",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Dolna biała karta z zaokrąglonymi rogami
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.62f)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(topStart = 45.dp, topEnd = 45.dp),
            color = Color.White.copy(alpha = 0.98f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isRegisterMode) "Cześć!" else "Witaj ponownie!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (errorMessage.isNotEmpty()) {
                    Text(text = errorMessage, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }
                
                if (successMessage.isNotEmpty()) {
                    Text(text = successMessage, color = Color(0xFF4CAF50), fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 12.dp))
                }

                if (isRegisterMode) {
                    AuthField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Imię",
                        icon = Icons.Default.Person
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                AuthField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email",
                    icon = Icons.Default.Email,
                    keyboardType = KeyboardType.Email
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Hasło",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        isLoading = true; errorMessage = ""; successMessage = ""
                        if (isRegisterMode) {
                            FirebaseService.signUp(email, password,
                                onSuccess = { 
                                    FirebaseService.updateProfileName(name, { 
                                        isLoading = false
                                        successMessage = "Konto utworzone!\nSprawdź swoją pocztę e-mail, aby potwierdzić rejestrację."
                                        // Nie przełączamy trybu od razu, by użytkownik mógł przeczytać komunikat
                                    }, { err -> isLoading = false; errorMessage = err })
                                },
                                onError = { isLoading = false; errorMessage = it }
                            )
                        } else {
                            FirebaseService.signIn(email, password, { isLoading = false; onLoginSuccess() }, { isLoading = false; errorMessage = it })
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = buttonBlue)
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text(if (isRegisterMode) "Zarejestruj się" else "Zaloguj się", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("lub", color = Color.Gray, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(painter = painterResource(R.drawable.ic_google_logo), contentDescription = null, modifier = Modifier.size(20.dp), tint = Color.Unspecified)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Kontynuuj z Google", fontWeight = FontWeight.Medium, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                TextButton(onClick = { isRegisterMode = !isRegisterMode; errorMessage = ""; successMessage = "" }) {
                    Text(
                        text = if (isRegisterMode) "Masz już konto? Zaloguj się" else "Nie masz konta? Zarejestruj się",
                        color = buttonBlue,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        leadingIcon = { Icon(icon, null, tint = Color(0xFF666666)) },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = onTogglePassword) {
                    Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Color.Gray)
                }
            }
        } else null,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF7B96FF),
            unfocusedBorderColor = Color.LightGray
        )
    )
}
