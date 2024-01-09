package com.elektro.monitoring.ui.auth

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.navigation.fragment.findNavController
import com.elektro.monitoring.app.MainActivity
import com.elektro.monitoring.databinding.FragmentRegisterBinding
import com.elektro.monitoring.helper.utils.loadImage
import com.elektro.monitoring.helper.utils.showSnackbar
import com.elektro.monitoring.helper.utils.showToast
import com.elektro.monitoring.viewmodel.AuthViewModel
import com.elektro.monitoring.viewmodel.DataViewModel
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
                authViewModel.shouldNavigateUp.observe(viewLifecycleOwner)
                { navigateUp ->
                    if (navigateUp == true) {
                        binding.root.showSnackbar("Register Successful")
                        findNavController().navigateUp()}
                }
                authViewModel.check.collect {
                    if (it.error.isNotBlank()) {
                        requireContext().showToast(it.error)
                    }
                }
            }
        }

        binding.ivUser.setOnClickListener {
            getContentLauncher.launch("image/*")
        }

        binding.btnRegister.setOnClickListener {
            if (authViewModel.file.value == null){
                requireContext().showToast("Please input image")
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}