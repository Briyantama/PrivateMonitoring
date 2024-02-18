package com.elektro.monitoring.ui.account

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
import com.elektro.monitoring.databinding.FragmentEditProfileBinding
import com.elektro.monitoring.helper.utils.loadImage
import com.elektro.monitoring.helper.utils.showToastWithoutIcon
import com.elektro.monitoring.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : Fragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var getContentLauncher: ActivityResultLauncher<String>
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater,container,false)

        authViewModel.getUserData()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getContentLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                loadImage(uri, requireView(), binding.ivUser)
                authViewModel.file.postValue(uri)
            }
        }

        val scope = viewLifecycleOwner.lifecycleScope
        val lifecycle = viewLifecycleOwner.lifecycle

        scope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                authViewModel.userData.collect {
                    if (it.error.isNotBlank()) {
                        requireContext().showToastWithoutIcon(it.error)
                    }
                    if (it.data != null) { it.data.let { user ->
                        binding.showNama.setText(user.name)
                        binding.showEmail.text = user.email
                        binding.showJob.text = user.job
                        binding.showNoHp.setText(user.phoneNumber)
                        loadImage(user.image, requireView(), binding.ivUser)
                        authViewModel.name.postValue(user.name)
                        authViewModel.email.postValue(user.email)
                        authViewModel.nomor.postValue(user.phoneNumber)
                        authViewModel.password.postValue(user.image)
                    }
                    }
                }
            }
        }

        binding.ivUser.setOnClickListener {
            getContentLauncher.launch("image/*")
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.editProfile.setOnClickListener {
            if (authViewModel.name.value == binding.showNama.text.toString() &&
                authViewModel.nomor.value == binding.showNoHp.text.toString() &&
                authViewModel.file.value==null) {
                findNavController().navigateUp()
            } else {
                if (authViewModel.file.value==null){
                    authViewModel.changeDataNoUri(binding.showEmail.text.toString(),
                        authViewModel.password.value.toString(),
                        binding.showNama.text.toString(), binding.showNoHp.text.toString())
                } else {
                    authViewModel.changeData(binding.showEmail.text.toString(),
                        binding.showNama.text.toString(), binding.showNoHp.text.toString()
                    )
                }
            }
        }

        authViewModel.shouldNavigateUp.observe(viewLifecycleOwner) { navigateUp ->
            if (navigateUp == true) {
                requireContext().showToastWithoutIcon("Profile Updated Successfully")
                Navigation.findNavController(requireView())
                    .navigate(R.id.action_editProfileFragment_to_myAccountFragment)
            }
        }
    }
}