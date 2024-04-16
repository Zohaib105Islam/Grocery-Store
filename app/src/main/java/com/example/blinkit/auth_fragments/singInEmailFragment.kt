package com.example.blinkit.auth_fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.activities.MainActivity
import com.example.blinkit.databinding.FragmentSingInEmailBinding
import com.example.blinkit.databinding.ResetPasswordBinding
import com.example.blinkit.utils.Utils
import com.example.blinkit.viewmodels.authViewModel
import com.google.android.play.integrity.internal.r
import kotlinx.coroutines.launch


class singInEmailFragment : Fragment() {

    private lateinit var binding: FragmentSingInEmailBinding

    private val viewModel: authViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentSingInEmailBinding.inflate(inflater,container,false)

        binding.loginBtn1.setOnClickListener{
            signInUser()
        }

        binding.gotosignup.setOnClickListener{
            findNavController().navigate(R.id.action_singInEmailFragment_to_signUpEmailFragment)
        }

        binding.resetPass1.setOnClickListener {
            resetPassword()
        }


        return binding.root
    }

    private fun resetPassword() {
        val resetPasswordLayout=ResetPasswordBinding.inflate(LayoutInflater.from(requireContext()))

        val alertDialog= AlertDialog.Builder(requireContext())
            .setView(resetPasswordLayout.root).create()
        alertDialog.show()

        resetPasswordLayout.resetEmailBtn.setOnClickListener {

            Utils.showDialog(requireContext(),"Verification email sending...")

            val email=resetPasswordLayout.resetEmail.text.toString()

            viewModel.apply {
                resetPassword(email)
                lifecycleScope.launch {
                    isPasswordReset.apply {
                        if (true){
                            alertDialog.dismiss()
                            Utils.hideDialog()
                            Utils.showToast(requireContext(),"Email sent, please verify and reset password")
                        }
                        else{
                            Handler().postDelayed(Runnable {
                                alertDialog.dismiss()
                                Utils.hideDialog()
                                Utils.showToast(requireContext(),"Error...")
                            },3000)

                        }
                    }
                }
            }
        }
    }

    private fun signInUser() {
        val email= binding.loginEmail1.text.toString()
        val password= binding.loginPass1.text.toString()

        if (email.isEmpty()){
            binding.loginEmail1.error="Enter email"
            binding.loginPass1.error="Enter password"
        }
        else if (password.isEmpty()){
            binding.loginPass1.error="Enter password"
        }
        else{
            Utils.showDialog(requireContext(),"Signing in...")

            viewModel.apply {
                signInWithEmail(requireContext(),email, password)
                lifecycleScope.launch {
                    isSignInSuccessfully.apply {
                        if (true){
                            Handler(Looper.getMainLooper()).postDelayed(Runnable{
                                Utils.hideDialog()
                                Utils.showToast(requireContext(),"Signin Succesfully...")
                                startActivity(Intent(requireActivity(), MainActivity::class.java))
                                requireActivity().finish()
                            },2000)

                        }
                        else{
                            Utils.hideDialog()
                            Utils.showToast(requireContext(),"Signin Error...")
                        }
                    }

                }
            }
        }

    }

}