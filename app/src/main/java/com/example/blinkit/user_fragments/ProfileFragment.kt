package com.example.blinkit.user_fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.blinkit.R
import com.example.blinkit.activities.AuthActivity
import com.example.blinkit.databinding.AddressBookLayoutBinding
import com.example.blinkit.databinding.FragmentProfileBinding
import com.example.blinkit.utils.Utils
import com.example.blinkit.viewmodels.UserViewModel


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel : UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentProfileBinding.inflate(inflater,container,false)


        onBackButtonClicked()

        onOrdersClicked()

        onAddressBookClicked()

        onLogOutClicked()


        return binding.root
    }

    private fun onLogOutClicked() {
        binding.profileLogout.setOnClickListener {
            val builder=AlertDialog.Builder(requireContext())
            val alertDialog=builder.create()
            builder.setTitle("Log out")
                .setMessage("Do you want to log out ?")
                .setPositiveButton("Yes"){_,_->
                    viewModel.logOutUser()
                    startActivity(Intent(requireContext(),AuthActivity::class.java))
                    requireActivity().finish()
                }
                .setNegativeButton("No"){_,_->
                    alertDialog.dismiss()
                }
                .show()
                .setCancelable(false)
        }
    }

    private fun onAddressBookClicked() {
        binding.profileAddressBook.setOnClickListener {
            val addressBookLayoutBinding=AddressBookLayoutBinding.inflate(LayoutInflater.from(requireContext()))
            viewModel.getUserAddress { address->
                addressBookLayoutBinding.etAddress.setText(address.toString())

            }

            val alertDialog=AlertDialog.Builder(requireContext())
                .setView(addressBookLayoutBinding.root)
                .create()
            alertDialog.show()

            addressBookLayoutBinding.btnEdit.setOnClickListener {
                addressBookLayoutBinding.etAddress.isEnabled=true
            }

            addressBookLayoutBinding.btnSave.setOnClickListener {
                viewModel.saveUserAddress(addressBookLayoutBinding.etAddress.text.toString())
                alertDialog.dismiss()
                Utils.showToast(requireContext(),"Address updated...")

            }

        }
    }

    private fun onOrdersClicked() {
        binding.profileYourOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_ordersFragment)
        }
    }

    private fun onBackButtonClicked() {
        binding.tbProfile.setNavigationOnClickListener{
            findNavController().navigate(R.id.action_profileFragment_to_homeFragment)
        }
    }


}