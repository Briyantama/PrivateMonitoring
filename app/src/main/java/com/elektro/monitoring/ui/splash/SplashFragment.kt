package com.elektro.monitoring.ui.splash

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
import androidx.navigation.Navigation
import com.elektro.monitoring.R
import com.elektro.monitoring.app.MainActivity
import com.elektro.monitoring.databinding.FragmentSplashBinding
import com.elektro.monitoring.helper.Constants.SPLASH_DELAY
import com.elektro.monitoring.viewmodel.AuthViewModel
import com.elektro.monitoring.viewmodel.DataViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater,container,false)

        authViewModel.loggedUser()

        val lifecycle = viewLifecycleOwner.lifecycle
        val scope = viewLifecycleOwner.lifecycleScope

        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                authViewModel.user.collect {
                    if (it.error.isNotBlank()) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            Navigation.findNavController(requireView())
                                .navigate(R.id.action_splashFragment_to_loginFragment)
                        }, SPLASH_DELAY.toLong())
                    }
                    if (it.data != null) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            Navigation.findNavController(requireView())
                                .navigate(R.id.action_splashFragment_to_homeFragment)
                        }, SPLASH_DELAY.toLong())
                    }
                }
            }
        }

        return binding.root
    }
}