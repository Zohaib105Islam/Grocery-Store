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
import com.example.blinkit.databinding.FragmentSplashBinding
import com.example.blinkit.utils.Utils
import com.example.blinkit.viewmodels.authViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private lateinit var binding: FragmentSplashBinding

    private var isFragmentAttached = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the flag to indicate that the fragment is attached
        isFragmentAttached = true
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSplashBinding.inflate(inflater, container, false)


        // Check if the fragment is attached before accessing the ViewModel
        if (isFragmentAttached) {

            Handler(Looper.getMainLooper()).postDelayed({
                checkUser()

            }, 2000)

        }

        return binding.root
    }

    fun checkUser(){
        val user = FirebaseAuth.getInstance().currentUser?.uid
        if(user != null){
            startActivity(Intent(requireContext(), MainActivity::class.java))
            requireActivity().finish()
        }
        else {
            findNavController().navigate(
                R.id.action_splashFragment_to_singInEmailFragment
            )

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Reset the flag to indicate that the fragment is detached
        isFragmentAttached = false
    }


}

