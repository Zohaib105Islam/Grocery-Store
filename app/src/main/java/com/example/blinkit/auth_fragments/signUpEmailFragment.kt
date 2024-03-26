package com.example.blinkit.auth_fragments

import android.content.Intent
import android.os.Bundle
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
//            startActivity(Intent(requireActivity(), MainActivity::class.java))
//            requireActivity().finish()
            createUser()
        }

        binding.gotoLogin.setOnClickListener{
            findNavController().navigate(R.id.action_signUpEmailFragment_to_singInEmailFragment)
        }




        return binding.root
    }

    private fun createUser() {
        val email=binding.signupEmail.text.toString()
        val password=binding.signupPass.text.toString()
        val name=binding.signupName.text.toString()
        val address=binding.signupAddress.text.toString()
        val phone=binding.signupPhone.text.toString()

        val users= Users(Utils.currentUser(),name,phone,email,address,password)

        viewModel.apply {
            createUserWithEmail(email,password,users)
            lifecycleScope.launch {
                isSignInSuccessfully.apply {
                    if (true){
                        Utils.showToast(requireContext(),"Signup Successfully...!!")
                        startActivity(Intent(requireContext(),MainActivity::class.java))
                        requireActivity().finish()
                    }
                    else{
                        Utils.showToast(requireContext(),"Signup Error...")
                    }
                }
            }
        }



    }


}