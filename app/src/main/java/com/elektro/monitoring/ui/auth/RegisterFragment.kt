package com.elektro.monitoring.ui.auth

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.elektro.monitoring.R
import com.elektro.monitoring.databinding.FragmentRegisterBinding
import com.elektro.monitoring.helper.utils.loadImage
import com.elektro.monitoring.helper.utils.showToastWithoutIcon
import com.elektro.monitoring.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var getContentLauncher: ActivityResultLauncher<String>
    private val authViewModel: AuthViewModel by viewModels()

    private val getContentImage = this.registerForActivityResult(
        ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            loadImage(uri, requireView(), binding.ivUser)
            authViewModel.file.value = uri
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater,container,false)

        binding.viewModel = authViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val lifecycle = viewLifecycleOwner.lifecycle
        val scope = viewLifecycleOwner.lifecycleScope

        getContentLauncher = getContentImage

        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                authViewModel.check.collect {
                    if (it.error.isNotBlank()) {
                        requireContext().showToastWithoutIcon(it.error)
                    }
                }
            }
        }

        authViewModel.emailExist.observe(viewLifecycleOwner) { exist ->
            if (exist){
                requireContext().showToastWithoutIcon("Email sudah terdaftar")
            }
        }

        binding.ivUser.setOnClickListener {
            getContentLauncher.launch("image/*")
        }

        binding.btnRegister.setOnClickListener {
            if (authViewModel.file.value == null){
                requireContext().showToastWithoutIcon("Please input image")
            }
        }

        authViewModel.shouldNavigateUp.observe(viewLifecycleOwner) { navigateUp ->
            if (navigateUp == true) {
                requireContext().showToastWithoutIcon("Register Successful")
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_registerFragment_to_accountFragment)
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}