package com.securitynav.security.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.securitynav.security.R

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var viewModel: ProfileViewModel
    private lateinit var tvName: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        tvName = view.findViewById(R.id.tvName)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        viewModel.userName.observe(viewLifecycleOwner) { name ->
            name?.let {
                tvName.text = it
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.loadProfile()
    }
}
