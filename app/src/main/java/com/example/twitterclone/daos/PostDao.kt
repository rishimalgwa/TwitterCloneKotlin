package com.example.twitterclone.daos

import com.example.twitterclone.models.AppUser
import com.example.twitterclone.models.Post
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.app
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao {
    val db = FirebaseFirestore.getInstance()
    val postCollection = db.collection("posts")
    val auth = Firebase.auth
    fun addPost(text:String){
        val currentUserId = auth.currentUser!!.uid
        val userDao = UserDao()
        GlobalScope.launch {
            val user = userDao.getUserById(currentUserId).await().toObject(AppUser::class.java)!!
            val currentTime = System.currentTimeMillis()
            val post = Post(text,user,currentTime)
            postCollection.document().set(post)
        }
    }
    fun getPostById(postId:String):Task<DocumentSnapshot>{
        return postCollection.document(postId).get()
    }
    fun updateLikes(postId: String){
        GlobalScope.launch {
            val currentUserId= auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)!!
            val isLiked = post.likedBy.contains(currentUserId)
            if (isLiked){
                post.likedBy.remove(currentUserId)
            }else{
                post.likedBy.add(currentUserId)
            }
            postCollection.document(postId).set(post)
        }
    }
}