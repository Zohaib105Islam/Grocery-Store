package com.example.blinkit.auth_fragments

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.activities.MainActivity
import com.example.blinkit.databinding.FragmentOTPBinding
import com.example.blinkit.models.Users
import com.example.blinkit.utils.Utils
import com.example.blinkit.viewmodels.authViewModel
import kotlinx.coroutines.launch

class OTPFragment : Fragment() {

    private lateinit var binding: FragmentOTPBinding

    private val viewModel: authViewModel by viewModels()

    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentOTPBinding.inflate(inflater, container, false)

        // disable continue button
        binding.signBtn.isEnabled = false
        binding.otp1.requestFocus()

// get user number and set into text view
        getUserNumber()
// set up for OTP text fields and button
        setupEditTextFields()
// back button click goto sign In fragment
        onBackButtonClick()

// Click on login button
        onLoginButtonClick()

        //  send OTP to number
        sendOTP()



        return binding.root
    }

    private fun onLoginButtonClick() {
        binding.signBtn.setOnClickListener() {
            Utils.showDialog(requireContext(), "Signing you...")
            val editTextList = listOf(
                binding.otp1,
                binding.otp2,
                binding.otp3,
                binding.otp4,
                binding.otp5,
                binding.otp6
            )

            val otp = editTextList.joinToString("") { it.text.toString() }

            if (otp.length < editTextList.size) {
                Utils.showToast(requireContext(), "please enter right OTP")
            } else {
                editTextList.forEach { it.text?.clear(); it.clearFocus() }

                verifyOtp(otp)
            }

        }
    }

    private fun verifyOtp(otp: String) {

        val users =Users("currentUser",phoneNumber, userAddress = " ")

        viewModel.apply {
            signInWithPhoneAuthCredential(otp, phoneNumber,users)
            lifecycleScope.launch {
                isSignInSuccessfully.apply{
                    if (true){
                        Utils.hideDialog()
                        Utils.showToast(requireContext(),"Login Successfully...!!")
                        startActivity(Intent(requireContext(),MainActivity::class.java))
                        requireActivity().finish()
                    }
                    else{
                        Handler(Looper.getMainLooper()).postDelayed({
                            Utils.hideDialog()
                            Utils.showToast(requireContext(), "Login error...")
                            Utils.showToast(requireContext(), phoneNumber)
                        }, 3000)
                    }

                }
            }
        }

    }

    private fun sendOTP() {
        Utils.showDialog(requireContext(), "OTP sending...")
        viewModel.apply {
            sendOTP(phoneNumber, requireActivity())
            lifecycleScope.launch {
                otpSend.apply {
                    if (true) {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "OTP sent...")
                    } else {

                        Handler(Looper.getMainLooper()).postDelayed({
                            Utils.hideDialog()
                            Utils.showToast(requireContext(), "OTP not sent...")
                            Utils.showToast(requireContext(), phoneNumber)
                        }, 3000)
                    }
                }
            }
        }
    }

    private fun onBackButtonClick() {
        binding.toolbar.setNavigationOnClickListener() {
            findNavController().navigateUp()
        }
    }

    private fun getUserNumber() {
        val bundle = arguments
        phoneNumber = bundle?.getString("phoneNumber").toString()
        binding.userPhNo.setText(phoneNumber)
    }

    private fun setupEditTextFields() {
        val editTextList = listOf(
            binding.otp1,
            binding.otp2,
            binding.otp3,
            binding.otp4,
            binding.otp5,
            binding.otp6
        )

        for (i in editTextList.indices) {
            editTextList[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        if (i < editTextList.size - 1) {
                            editTextList[i + 1].requestFocus()
                        }
                        if (binding.otp6.length() == 1) {
                            binding.signBtn.setBackgroundColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.yellow
                                )
                            )
                            binding.signBtn.isEnabled = true
                        }

                    } else if (s?.length == 0) {
                        if (i > 0) {
                            editTextList[i - 1].requestFocus()
                            binding.signBtn.setBackgroundColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.grey
                                )
                            )
                            binding.signBtn.isEnabled = false
                        }
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
    }


}