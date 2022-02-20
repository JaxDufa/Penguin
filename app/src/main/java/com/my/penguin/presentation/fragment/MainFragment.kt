package com.my.penguin.presentation.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.my.penguin.R
import com.my.penguin.databinding.FragmentMainBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModel()

    private var _binding: FragmentMainBinding? = null

    // Valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    // region - Lifecycle
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.selectedCountry = Country.KENYA
        setupObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    private fun setupObservers() {
        viewModel.stateViewState.observe(this) {
            when (it) {
                is ViewState.Complete -> TODO()
                is ViewState.CurrentRate -> showCurrentCountryRate(it.value)
                is ViewState.Error -> showErrorState(it.type)
                else -> Unit
            }
            Log.d("TAG", "ViewState $it")
            showLoadingState(it.loading)
        }
    }

    private fun showCurrentCountryRate(rate: Float) {
        binding.text.text = rate.toString()
    }

    private fun showLoadingState(shouldShow: Boolean) {
        Log.d("TAG", "Should show loading $shouldShow")
        binding.progressIndicator.isVisible = shouldShow
    }

    private fun showErrorState(errorType: ErrorType) {
        MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle(resources.getString(errorType.title))
            .setMessage(resources.getString(errorType.message))
            .setPositiveButton(resources.getString(R.string.error_positive_button)) { _, _ ->
                viewModel.loadExchangeRates()
            }
            .show()
    }
}