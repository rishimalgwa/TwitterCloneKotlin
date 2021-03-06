package com.example.twitterclone.daos

import com.example.twitterclone.models.AppUser
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao {
    private val DB = FirebaseFirestore.getInstance()
    private val userCollection = DB.collection("users")

    fun addUser(user: AppUser){
        user.let {
            GlobalScope.launch(Dispatchers.IO) {
                userCollection.document(user.uid).set(it)
            }
        }
    }
    fun getUserById(uId: String): Task<DocumentSnapshot>{
        return userCollection.document(uId).get()
    }
}