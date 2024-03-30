package com.example.blinkit.auth_fragments

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
import com.example.blinkit.databinding.FragmentSignUpEmailBinding
import com.example.blinkit.models.Users
import com.example.blinkit.utils.Utils
import com.example.blinkit.viewmodels.authViewModel
import kotlinx.coroutines.launch


class signUpEmailFragment : Fragment() {

    private lateinit var binding: FragmentSignUpEmailBinding

    private val viewModel: authViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentSignUpEmailBinding.inflate(inflater,container,false)

        binding.signupBtn.setOnClickListener{
            createUser()
        }

        binding.gotoLogin.setOnClickListener{
            findNavController().navigate(R.id.action_signUpEmailFragment_to_singInEmailFragment)
        }




        return binding.root
    }

    private fun createUser() {
        val email = binding.signupEmail.text.toString()
        val password = binding.signupPass.text.toString()
        val name = binding.signupName.text.toString()
        val address = binding.signupAddress.text.toString()
        val phone = binding.signupPhone.text.toString()

        val users = Users(Utils.currentUser(), name, phone, email, address, password)

        if (name.isEmpty()){
            binding.signupName.error="Enter name"
            binding.signupEmail.error="Enter email"
            binding.signupPass.error="Enter password"
            binding.signupAddress.error="Enter address"
            binding.signupPhone.error="Enter phone"
        }
        else if (email.isEmpty()){
            binding.signupEmail.error="Enter email"
            binding.signupPass.error="Enter password"
            binding.signupAddress.error="Enter address"
            binding.signupPhone.error="Enter phone"
        }
        else if (password.isEmpty()){
            binding.signupPass.error="Enter password"
            binding.signupAddress.error="Enter address"
            binding.signupPhone.error="Enter phone"
        }
        else if (address.isEmpty()){
            binding.signupAddress.error="Enter address"
            binding.signupPhone.error="Enter phone"
        }
        else if (phone.isEmpty()){
            binding.signupPhone.error="Enter phone"
        }
        else{
            lifecycleScope.launch {
                viewModel.apply {
                    createUserWithEmail(email, password, users)
                    lifecycleScope.launch {
                        isSignInSuccessfully.apply {
                            Utils.showDialog(requireContext(),"Sign Up...")

                            if (true) {
                                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                                    Utils.hideDialog()
                                    Utils.showToast(requireContext(), "Signup Successfully...!!")
                                    startActivity(Intent(requireContext(), MainActivity::class.java))
                                    requireActivity().finish()
                                },2000)
                            }
                            else {
                                Utils.hideDialog()
                                Utils.showToast(requireContext(), "Signup Error...")
                            }



                        }
                    }
                }


            }
        }



    }

}