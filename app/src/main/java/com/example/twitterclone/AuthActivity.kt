package com.example.twitterclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Visibility
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import com.example.twitterclone.daos.UserDao
import com.example.twitterclone.models.AppUser
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.TAG
import com.firebase.ui.auth.data.model.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class AuthActivity : AppCompatActivity() {
    private val RC_SIGN_IN :Int= 123
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInButton :SignInButton
    private lateinit var googleSignInProgressBar :ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)
        googleSignInButton = findViewById<SignInButton>(R.id.googleSignInButton)
        googleSignInProgressBar= findViewById<ProgressBar>(R.id.signInProgressBar)
        auth = Firebase.auth
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.firebase.ui.auth.R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)
        googleSignInButton.setOnClickListener {
            signIn()
        }


    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent,RC_SIGN_IN)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

       if (requestCode == RC_SIGN_IN){
           val task = GoogleSignIn.getSignedInAccountFromIntent(data)
           handleSignInResult(task)
       }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)!!
            Log.d("TAG", "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e:ApiException){
        Log.w("TAG", "signinResult: failed codee" + e.statusCode)
    }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken,null)
        googleSignInButton.visibility = View.GONE
        googleSignInProgressBar.visibility = View.VISIBLE
        GlobalScope.launch (Dispatchers.IO ){
            val auth = auth.signInWithCredential(credential).await()
            val firebaseUser = auth.user
            withContext(Dispatchers.Main){
                updateUI(firebaseUser)
            }
        }
    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        if(firebaseUser != null){
            val user = AppUser(firebaseUser.uid,firebaseUser.displayName,firebaseUser.photoUrl.toString())
            val  userDao= UserDao()
            userDao.addUser(user)
            val mainActivityIntent = Intent(this,MainActivity::class.java)
            startActivity(mainActivityIntent)
            finish()
        }else{
            googleSignInButton.visibility = View.VISIBLE
            googleSignInProgressBar.visibility = View.GONE
        }
    }

}