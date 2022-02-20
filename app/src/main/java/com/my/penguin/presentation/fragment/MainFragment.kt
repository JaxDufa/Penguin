package com.my.penguin.presentation.fragment

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    private fun setupView() {
        binding.textAmount.editText?.addTextChangedListener(
            afterTextChanged = {
                viewModel.onAmountChanged(it.toString())
            }
        )
    }

    private fun setupObservers() {
        viewModel.stateViewState.observe(viewLifecycleOwner) {
            when (it) {
                is ViewState.Complete -> TODO()
                is ViewState.Initial -> showInitialState(it.countries)
                is ViewState.Default -> showDefaultState(it.country)
                is ViewState.Error -> showErrorState(it.type)
                else -> Unit
            }
            Log.d("TAG", "ViewState $it")
            showLoadingState(it.loading)
        }
    }

    private fun showCurrentCountryRate(rate: Float) {
//        binding.text.text = rate.toString()
    }

    private fun showInitialState(countries: List<Country>) {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_menu_item,
            countries.map { it.name }
        )
        (binding.dropdownMenu as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            //setText(countries[0].name, false)
            setOnItemClickListener { _, _, i, _ ->
                viewModel.onCountrySelected(i)
            }
        }
        binding.initialGroup.isVisible = true
    }

    private fun showDefaultState(country: Country) {
        with(binding) {

            textAmount.helperText =
                getString(R.string.input_text_amount_helper, country.currencyPrefix, 0)

            textPhoneNumber.editText?.filters =
                arrayOf(InputFilter.LengthFilter(country.phoneNumberDigits))
            defaultGroup.isVisible = true
        }
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
                viewModel.onTryAgain()
            }
            .show()
    }
}