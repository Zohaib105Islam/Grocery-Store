package com.example.blinkit.viewmodels

import android.app.Activity
import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.blinkit.models.Users
import com.example.blinkit.utils.Utils
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.TimeUnit

class authViewModel : ViewModel() {

    val auth =FirebaseAuth.getInstance()

    private var _verficationId = MutableStateFlow<String?>(null)
    private var _otpSent = MutableLiveData(false)
    var otpSend = _otpSent

    private var _isSignInSuccessfully = MutableStateFlow(false)
    val isSignInSuccessfully = _isSignInSuccessfully

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

    // For Phone Authentication==========================================
    fun sendOTP(userNumber: String, activity: Activity) {

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
                // signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                _verficationId.value = verificationId
                _otpSent.value = true
            }
        }


        val options = PhoneAuthOptions.newBuilder(Utils.getAuthInstance())
            .setPhoneNumber(userNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithPhoneAuthCredential(otp: String, userNumber: String, users: Users) {
        val credential = PhoneAuthProvider.getCredential(_verficationId.value.toString(), otp)

        FirebaseMessaging.getInstance().token.addOnCompleteListener{
            users.userToken=it.result

        Utils.getAuthInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    FirebaseDatabase.getInstance().getReference("AllUsers")
                        .child(Utils.currentUser()!!).child("UserInfo").setValue(users)
                    _isSignInSuccessfully.value = true

                }
            }
    }
    }
//=====================================================================


// For Email Authentication===========================

    fun createUserWithEmail(email: String, password: String, users: Users) {

        FirebaseMessaging.getInstance().token.addOnCompleteListener{
            users.userToken=it.result

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    if (true) {
                        users.uid=Utils.currentUser()
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseDatabase.getInstance().getReference("AllUsers")
                            .child(Utils.currentUser()!!).child("UserInfo").setValue(users)
                        _isSignInSuccessfully.value = true
                        Log.d("GGG", "createUserWithEmail:${users.uid}")

                    } else {
                        _isSignInSuccessfully.value = false
                        // If sign in fails, display a message to the user.
                      //  Log.w(TAG, "createUserWithEmail:failure", it.exception)

                    }
                }
        }
    }

    fun signInWithEmail(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { task ->
                if (true) {
                    // Sign in success, update UI with the signed-in user's information
                    _isSignInSuccessfully.value = true
                    Log.d(TAG, "signInWithEmail:success")


                } else {
                    _isSignInSuccessfully.value = false
                    // If sign in fails, display a message to the user.
                  //  Log.w(TAG, "signInWithEmail:failure", )

                }
            }

    }


    fun resetPassword(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Password reset email sent successfully
                    // You can handle success or perform additional actions here
                    _isPasswordReset.value = true
                } else {
                    // Password reset email sending failed
                    // You can handle failure or display an error message to the user
                }
            }
    }
}
