package com.example.blinkit.auth_fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.databinding.FragmentChooseAuthBinding

class chooseAuthFragment : Fragment() {

    private lateinit var binding: FragmentChooseAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentChooseAuthBinding.inflate(inflater,container,false)

        binding.goEmailBtn.setOnClickListener{
            findNavController().navigate(R.id.action_chooseAuthFragment_to_singInEmailFragment)
        }

        binding.goPhoneBtn.setOnClickListener{
            findNavController().navigate(R.id.action_chooseAuthFragment_to_signInPhoneFragment)
        }


        return binding.root
    }


}