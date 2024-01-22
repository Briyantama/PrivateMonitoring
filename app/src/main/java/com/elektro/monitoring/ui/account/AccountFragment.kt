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
import androidx.navigation.Navigation
import com.elektro.monitoring.R
import com.elektro.monitoring.databinding.FragmentAccountBinding
import com.elektro.monitoring.helper.utils.dialogPN
import com.elektro.monitoring.helper.utils.loadImage
import com.elektro.monitoring.helper.utils.showToastWithoutIcon
import com.elektro.monitoring.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater,container,false)

        authViewModel.getUserData()

        val lifecycle = viewLifecycleOwner.lifecycle
        val scope = viewLifecycleOwner.lifecycleScope

        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                authViewModel.userData.collect {
                    if (it.error.isNotBlank()) {
                        requireContext().showToastWithoutIcon(it.error)
                    }
                    if (it.data != null) {
                        it.data.let { user ->
                            with(binding){
                                nameTv.text = user.name
                                loadImage(user.image, requireView(), ivProfile)
                                register.visibility = if (user.job == "admin") View.VISIBLE
                                else View.GONE
                            }
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.myProfile.setOnClickListener{
            Navigation.findNavController(view)
                .navigate(R.id.action_akunFragment_to_myAccountFragment)
        }

        binding.changePass.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_akunFragment_to_changePassFragment)
        }

        binding.register.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_akunFragment_to_registerFragment)
        }

        binding.logOut.setOnClickListener {
            requireContext().dialogPN("Log Out", "Are you sure?", "Yes", "No"){
                authViewModel.logOut()
                authViewModel.service.postValue(false)
                requireContext().showToastWithoutIcon("User has successfully logged out")
                Navigation.findNavController(view)
                    .navigate(R.id.action_akunFragment_to_splashFragment)
            }
        }
    }
}