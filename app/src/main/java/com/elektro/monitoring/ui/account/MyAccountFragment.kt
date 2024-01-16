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
import com.elektro.monitoring.databinding.FragmentMyAccountBinding
import com.elektro.monitoring.helper.utils.loadImage
import com.elektro.monitoring.helper.utils.showToastWithoutIcon
import com.elektro.monitoring.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MyAccountFragment : Fragment() {

    private var _binding: FragmentMyAccountBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMyAccountBinding.inflate(inflater,container,false)

        authViewModel.getUserData()

        val lifecycle = viewLifecycleOwner.lifecycle
        val scope = viewLifecycleOwner.lifecycleScope

        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                authViewModel.userData.collect {
                    if (it.error.isNotBlank()) {
                        requireContext().showToastWithoutIcon(it.error)
                    }
                    if (it.data != null) { it.data.let { user ->
                        binding.showNama.text = user.name
                        binding.showEmail.text = user.email
                        binding.showJob.text = user.job
                        binding.showNoHp.text = user.phoneNumber
                        loadImage(user.image, requireView(), binding.ivUser)
                    }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }
}