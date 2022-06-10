package com.example.twitterclone.models

class Post(
    val text:String="",
    val createdBy:AppUser = AppUser(),
    val createdAt:Long = 0L,
    val likedBy: ArrayList<String> = ArrayList()
) {
}