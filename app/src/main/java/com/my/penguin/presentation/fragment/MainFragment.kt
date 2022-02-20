package com.my.penguin.presentation.fragment

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
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

    // region - Setup
    private fun setupView() {
        with(binding) {
            buttonSend.setOnClickListener {
                viewModel.onSendAction()
            }

            updateRequiredError(textFirstName, textLastName, textPhoneNumber, textAmount)
            textAmount.editText?.addTextChangedListener(
                afterTextChanged = {
                    viewModel.onAmountChanged(it.toString())
                }
            )
        }
    }

    private fun updateRequiredError(vararg inputLayouts: TextInputLayout) {
        inputLayouts.forEach {
            it.editText?.setOnFocusChangeListener { _, focus ->
                if (!focus) {
                    it.updateRequiredError()
                }
            }
        }
    }

    private fun TextInputLayout.updateRequiredError() {
        if (editText?.text.isNullOrBlank()) {
            isErrorEnabled = true
            error = getString(R.string.input_text_required_error)
        } else {
            isErrorEnabled = false
        }
    }

    private fun setupObservers() {
        with(viewModel) {
            stateViewState.observe(viewLifecycleOwner) {
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
            currencyBinaryFinalValue.observe(viewLifecycleOwner) {
                binding.textAmount.helperText =
                    getString(R.string.input_text_amount_helper, it.prefix, it.value)
                binding.buttonSend.isEnabled = it.value.isNotBlank()
            }
        }
    }
    // endregion

    // region - States
    private fun showInitialState(countries: List<Country>) {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_menu_item,
            countries.map { it.name }
        )
        (binding.dropdownMenu as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            setOnItemClickListener { _, _, i, _ ->
                viewModel.onCountrySelected(i)
            }
        }
        binding.initialGroup.isVisible = true
    }

    private fun showDefaultState(country: Country) {
        with(binding) {

            textAmount.editText?.text?.clear()
            textAmount.helperText =
                getString(R.string.input_text_amount_helper, country.currencyPrefix, 0)

            textPhoneNumber.prefixText = country.phonePrefix
            textPhoneNumber.editText?.text?.clear()
            textPhoneNumber.editText?.filters =
                arrayOf(InputFilter.LengthFilter(country.phoneNumberDigits))
            defaultGroup.isVisible = true
        }
    }

    private fun showLoadingState(shouldShow: Boolean) {
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
    // endregion
}