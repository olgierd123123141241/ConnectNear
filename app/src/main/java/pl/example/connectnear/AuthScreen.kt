package pl.example.connectnear

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

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
                            errorMessage = "Błąd logowania Google: $error"
                        }
                    )
                } catch (e: ApiException) {
                    errorMessage = "Błąd Google API: ${e.statusCode}"
                    Log.e("AuthScreen", "Google sign in failed", e)
                }
            } else {
                 errorMessage = "Logowanie Google anulowane."
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
    // --- END GOOGLE SIGN IN ---

    // --- FACEBOOK SIGN IN ---
    val callbackManager = remember { CallbackManager.Factory.create() }
    val loginManager = LoginManager.getInstance()

    val facebookLoginLauncher = rememberLauncherForActivityResult(
        contract = loginManager.createLogInActivityResultContract(callbackManager, null),
        onResult = { } 
    )

    DisposableEffect(Unit) {
        loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                isLoading = true
                FirebaseService.signInWithFacebook(result.accessToken.token,
                    onSuccess = {
                        // Po udanym logowaniu pobierz znajomych
                        FirebaseService.fetchAndStoreFacebookFriends(
                            onSuccess = {
                                isLoading = false
                                onLoginSuccess()
                            },
                            onError = { error ->
                                isLoading = false
                                // Nawet jeśli nie uda się pobrać znajomych, logowanie jest udane
                                onLoginSuccess()
                                Log.e("AuthScreen", "Friends fetch error: $error")
                            }
                        )
                    },
                    onError = { error ->
                        isLoading = false
                        errorMessage = "Błąd logowania Facebook: $error"
                    }
                )
            }

            override fun onCancel() {
                errorMessage = "Logowanie przez Facebooka anulowane."
            }

            override fun onError(error: FacebookException) {
                errorMessage = "Błąd Facebooka: ${error.message}"
            }
        })

        onDispose {
            loginManager.unregisterCallback(callbackManager)
        }
    }
    // --- END FACEBOOK SIGN IN ---


    LaunchedEffect(savedEmail) {
        if (savedEmail.isNotEmpty() && email.isEmpty()) {
            email = savedEmail
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0AA4F4), Color(0xFF1CD9C3))
                )
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isRegisterMode) "Rejestracja" else "Logowanie",
            fontSize = 32.sp,
            color = Color.White,
            modifier = Modifier.padding(bottom = 30.dp)
        )

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(bottom = 10.dp))
        }
        if (successMessage.isNotEmpty()) {
            Text(text = successMessage, color = Color.Green, modifier = Modifier.padding(bottom = 10.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.8f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Hasło (min. 6 znaków)") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Ukryj hasło" else "Pokaż hasło"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.8f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.8f)
            )
        )

        if (!isRegisterMode) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = {
                    val cleanEmail = email.trim()
                    if (cleanEmail.isEmpty()) {
                        errorMessage = "Wpisz e-mail, aby zresetować hasło!"
                    } else {
                        isLoading = true; errorMessage = ""; successMessage = ""
                        FirebaseService.sendPasswordResetEmail(cleanEmail,
                            { isLoading = false; successMessage = "Wysłano e-mail resetujący!" },
                            { error -> isLoading = false; errorMessage = "Błąd: $error" }
                        )
                    }
                }) { Text("Zapomniałeś hasła?", color = Color.White, fontSize = 12.sp) }
            }
        } else { Spacer(modifier = Modifier.height(20.dp)) }

        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else {
            Button(
                onClick = {
                    isLoading = true; errorMessage = ""; successMessage = ""
                    val cleanEmail = email.trim()
                    val cleanPassword = password.trim()

                    if (cleanEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                        errorMessage = "Niepoprawny e-mail"; isLoading = false; return@Button
                    }
                    if (cleanPassword.length < 6) {
                        errorMessage = "Za krótkie hasło"; isLoading = false; return@Button
                    }

                    if (isRegisterMode) {
                        FirebaseService.signUp(cleanEmail, cleanPassword,
                            { isLoading = false; successMessage = "Konto utworzone! Sprawdź e-mail."; isRegisterMode = false },
                            { isLoading = false; errorMessage = "Błąd: $it" }
                        )
                    } else {
                        FirebaseService.signIn(cleanEmail, cleanPassword,
                            onSuccess = {
                                scope.launch {
                                    preferenceManager.saveEmail(cleanEmail)
                                }
                                isLoading = false
                                onLoginSuccess()
                            },
                            onError = { isLoading = false; errorMessage = "Błąd: $it" }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006400))
            ) {
                Text(if (isRegisterMode) "Zarejestruj się" else "Zaloguj się")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Przycisk logowania Google
        Button(
            onClick = { 
                errorMessage = ""
                successMessage = ""
                googleSignInLauncher.launch(googleSignInClient.signInIntent) 
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_google_logo), // Upewnij się, że masz tę ikonę w drawable
                    contentDescription = "Google Logo",
                    tint = Color.Unspecified, // Ważne, aby nie zmieniać koloru ikony
                    modifier = Modifier.size(24.dp)
                )
                Text("Zaloguj się z Google", color = Color.Black, modifier = Modifier.padding(start = 16.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Przycisk logowania Facebook
        Button(
            onClick = { 
                errorMessage = ""
                successMessage = ""
                facebookLoginLauncher.launch(listOf("email", "public_profile", "user_friends")) 
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 16.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_facebook_logo),
                    contentDescription = "Facebook Logo",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
                Text("Zaloguj się z Facebookiem", color = Color.White, modifier = Modifier.padding(start = 16.dp))
            }
        }


        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = { isRegisterMode = !isRegisterMode; errorMessage = ""; successMessage = "" }) {
            Text(if (isRegisterMode) "Masz konto? Zaloguj się" else "Nie masz konta? Zarejestruj się", color = Color.White)
        }
    }
}
