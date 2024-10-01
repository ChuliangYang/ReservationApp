import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reservationapp.data.model.UserType
import com.example.reservationapp.ui.feature.login.LoginEventState
import com.example.reservationapp.ui.feature.login.LoginViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToProviderFlow: (Int) -> Unit,
    onNavigateToClientFlow: (Int) -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    var userType by rememberSaveable { mutableStateOf(UserType.CLIENT) }
    var account by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            event?.let {
                when (it) {
                    is LoginEventState.NavigateToProvider -> {
                        onNavigateToProviderFlow(it.providerId)
                    }

                    is LoginEventState.NavigateToClient -> {
                        onNavigateToClientFlow(it.clientId)
                    }

                    is LoginEventState.DisplayErrorMessage -> {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
                viewModel.onEventHandled()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center
        ) {
            UserTypeToggle(
                userType = userType,
                onUserTypeChange = { userType = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = account,
                onValueChange = { account = it },
                label = { Text("Account") },
                leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = "Email Icon") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.login(account, password, userType) },
                modifier = Modifier.fillMaxWidth(),
                enabled = account.isNotBlank() && password.isNotBlank()
            ) {
                Text(text = "Login")
            }
        }
    }
}

@Composable
fun UserTypeToggle(
    userType: UserType,
    onUserTypeChange: (UserType) -> Unit
) {
    val options = listOf("Client", "Provider")
    val selectedOption = when (userType) {
        UserType.CLIENT -> 0
        UserType.PROVIDER -> 1
    }

    Surface(
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
        ) {
            options.forEachIndexed { index, text ->
                val isSelected = index == selectedOption
                val backgroundColor =
                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                val contentColor =
                    if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(backgroundColor)
                        .clickable { onUserTypeChange(if (index == 0) UserType.CLIENT else UserType.PROVIDER) }
                ) {
                    Text(
                        text = text,
                        color = contentColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
