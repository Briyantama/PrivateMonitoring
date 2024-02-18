package com.elektro.monitoring.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.elektro.monitoring.databinding.FragmentChangePassBinding
import com.elektro.monitoring.helper.utils.showToastWithoutIcon
import com.elektro.monitoring.viewmodel.AuthViewModel
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

        val scope = viewLifecycleOwner.lifecycleScope
        val lifecycle = viewLifecycleOwner.lifecycle

        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                authViewModel.check.collect { result ->
                    if (result.error.isNotBlank()) {
                        requireContext().showToastWithoutIcon(result.error)
                    }
                }
            }
        }

        authViewModel.shouldNavigateUp.observe(viewLifecycleOwner) { navigateUp ->
            if (navigateUp == true) {
                requireContext().showToastWithoutIcon("Password Has Changed")
                findNavController().navigateUp()}
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}