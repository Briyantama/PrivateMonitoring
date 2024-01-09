package com.elektro.monitoring.ui.account

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.elektro.monitoring.app.MainActivity
import com.elektro.monitoring.databinding.FragmentChangePassBinding
import com.elektro.monitoring.helper.utils.showSnackbar
import com.elektro.monitoring.helper.utils.showToast
import com.elektro.monitoring.viewmodel.AuthViewModel
import com.elektro.monitoring.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePassFragment : Fragment() {

    private var _binding: FragmentChangePassBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChangePassBinding.inflate(inflater,container,false)

        binding.viewModel = authViewModel
        binding.lifecycleOwner = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel.getUserData()

        val lifecycle = viewLifecycleOwner.lifecycle
        val scope = viewLifecycleOwner.lifecycleScope

        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                authViewModel.shouldNavigateUp.observe(viewLifecycleOwner)
                { navigateUp ->
                    if (navigateUp == true) {
                        binding.snackbar.showSnackbar("Password Has Changed")
                        findNavController().navigateUp()}
                }
                authViewModel.check.collect { result ->
                    if (result.error.isNotBlank()) {
                        requireContext().showToast(result.error)
                    }
                }
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}