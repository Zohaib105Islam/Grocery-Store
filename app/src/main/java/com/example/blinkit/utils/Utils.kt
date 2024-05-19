package com.example.blinkit.utils

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.example.blinkit.databinding.ProgressDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {

    private var dialog : AlertDialog? = null

    fun showDialog(context: Context,message: String){
        val progress = ProgressDialogBinding.inflate(LayoutInflater.from(context))
        progress.tvMessage.text = message
        dialog = AlertDialog.Builder(context).setView(progress.root).setCancelable(false).create()
        dialog!!.show()
    }

    fun hideDialog(){
        dialog?.dismiss()
    }

    fun showToast(context: Context, message: String){
        Toast.makeText(context,message,Toast.LENGTH_SHORT).show()
    }
private var firebaseAuthInstance: FirebaseAuth? = null
    fun getAuthInstance(): FirebaseAuth {
        if (firebaseAuthInstance  == null){
            firebaseAuthInstance = FirebaseAuth.getInstance()
        }
return firebaseAuthInstance!!
    }

    fun currentUser(): String? {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        return user
    }

    fun itemPushKey():String{
        // product push key
        val productRef = FirebaseDatabase.getInstance().getReference("Admins")
        val newItemKey: String? = productRef.push().key
        return newItemKey!!
    }

    fun getRandomId(): String{
        return (1.. 25).map { (('A'.. 'Z')+('a'..'z')+('0'..'9')).random()}.joinToString("")
    }
    fun getRandomId1(): String{
        return (1 .. 25).map{(('A' .. 'Z') + ('a' .. 'z') + ('0' .. '9')).random()}.joinToString("")
    }


//    fun getCurrentDate(): String?{
//        val currentDate = LocalDate.now()
//        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
//        return currentDate.format(formatter)
//    }
//
//    fun getCurrentDate1(): LocalDate {
//        return LocalDate.now()
//    }

    fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd-M-yyyy", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    fun timeStamp(): Long {
        val timestamp = System.currentTimeMillis()
        return timestamp
    }

}