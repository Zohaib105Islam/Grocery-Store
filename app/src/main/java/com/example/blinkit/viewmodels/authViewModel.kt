package com.example.blinkit.viewmodels

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blinkit.models.Users
import com.example.blinkit.utils.Utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow

class authViewModel: ViewModel() {

    val auth =FirebaseAuth.getInstance()



    private var _isSignUpSuccessfully = MutableLiveData(false)
    var isSignUpSuccessfully = _isSignUpSuccessfully

    private var _isPasswordReset = MutableLiveData(false)
    var isPasswordReset = _isPasswordReset

    private var _isCurrentUser = MutableStateFlow(false)
    var isCurrentUser = _isCurrentUser

    init {
        if (Utils.getAuthInstance().currentUser != null){
            isCurrentUser.value = true
        }
//        Utils.getAuthInstance().currentUser?.let {
//            isCurrentUser.value = true
//        }
    }


// For Email Authentication===========================

    fun createUserWithEmail(email: String, password: String, users: Users) {

        FirebaseMessaging.getInstance().token.addOnCompleteListener{
            users.userToken=it.result

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                        users.uid=Utils.currentUser()

                        FirebaseDatabase.getInstance().getReference("AllUsers")
                            .child(Utils.currentUser()!!).child("UserInfo").setValue(users)

                        _isSignUpSuccessfully.value = true
                        Log.d("GGG", "createUserWithEmail:${users.uid}")

                }
                .addOnFailureListener(){
                    _isSignUpSuccessfully.value = false
                    Log.d("Error sign up", "Error sign up  : "+it.toString())
                }
        }
    }



    private var _isSignInSuccessfully = MutableLiveData(false)
    var isSignInSuccessfully = _isSignInSuccessfully
    fun signInWithEmail(context: Context,email: String, password: String) {


        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(context){ task ->
                if (task.isSuccessful) {
                    _isSignInSuccessfully.value = true
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser

                } else {
                    _isSignInSuccessfully.value = false
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)

                }
            }


//        auth.signInWithEmailAndPassword(email, password)
//            .addOnSuccessListener { task ->
//
//                if (task.isSuccessful){
//                    _isSignInSuccessfully.value = true
//                    Log.d(TAG, "signInWithEmail:success")
//                }
//
//
//            }
//            .addOnFailureListener(){
//                _isSignInSuccessfully.value = false
//                Log.d("Error sign in", "Error sign in  : "+it.toString())
//            }

    }


    fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Password reset email sent successfully
                    // You can handle success or perform additional actions here
                    _isPasswordReset.value = true
                } else {
                    _isPasswordReset.value = false
                    // Password reset email sending failed
                    // You can handle failure or display an error message to the user
                }
            }
    }
}
