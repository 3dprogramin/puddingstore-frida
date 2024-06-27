package ro.steinbach.pudding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Login button clicked
     */
    fun buttonClicked(view: View) {
        val email = findViewById<EditText>(R.id.email_input).text.toString()
        val password = findViewById<EditText>(R.id.password_input).text.toString()
        val apiUrl = getString(R.string.api_url)

        // make sure the input is not empty
        if (email.isEmpty()) {
            view.context.toast("Email is missing")
            return
        }
        if (password.isEmpty()) {
            view.context.toast("Password is missing")
            return
        }

        // disable button
        controlButton(false)

        // make HTTP request
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val loginSuccess = sendLoginRequest(apiUrl, email, password)
                withContext(Dispatchers.Main) {
                    if (loginSuccess) {
                        loggedIn(view)
                    } else {
                        view.context.toast("Account does not exist")
                    }
                }
            } catch (e: Exception) {
                // Handle the exception and show an error message on the main thread
                withContext(Dispatchers.Main) {
                    view.context.toast("Error: ${e.message}")
                }
            } finally {
                withContext(Dispatchers.Main) {
                    controlButton(true)
                }
            }
        }
    }

    /**
     * Enable / disable button
     */
    private fun controlButton(enabled: Boolean) {
        val button = findViewById<Button>(R.id.login_btn)
        button.isEnabled = enabled
        button.isClickable = enabled
    }

    /**
     * Successfully logged in, change activity
     */
    private fun loggedIn(view: View) {
        view.context.toast("You are logged in!")
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
    }

    /**
     * Implement toast function to context object
     */
    private fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    /**
     * Send login request to API server
     */
    private fun sendLoginRequest(apiUrl: String, email: String, password: String): Boolean {
        var reqParam =
            URLEncoder.encode("email", "UTF-8") + "=" + URLEncoder.encode(email, "UTF-8")
        reqParam += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(
            password,
            "UTF-8"
        )
        val apiURL = URL(apiUrl)

        with(apiURL.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "POST"

            val wr = OutputStreamWriter(getOutputStream());
            wr.write(reqParam);
            wr.flush();
            println("Response code ${responseCode}")

            // if status code is 200, we're logged in
            return responseCode == 200

            // no need to read the body for this example
//            BufferedReader(InputStreamReader(inputStream)).use {
//                val response = StringBuffer()
//
//                var inputLine = it.readLine()
//                while (inputLine != null) {
//                    response.append(inputLine)
//                    inputLine = it.readLine()
//                }
//                println("Response : $response")
//            }
        }
    }
}