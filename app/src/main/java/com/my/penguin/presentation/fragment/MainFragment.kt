package com.my.penguin.presentation.fragment

import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import com.my.penguin.R
import com.my.penguin.databinding.FragmentMainBinding
import com.my.penguin.presentation.hideKeyboard
import com.my.penguin.presentation.model.Country
import com.my.penguin.presentation.model.RecipientCurrencyBinaryValue
import com.my.penguin.presentation.showDialog
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null

    // Valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModel()

    private val TextInputLayout.text: String
        get() = editText?.text.toString()

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
                requireActivity().hideKeyboard(it)
                viewModel.onSendAction(
                    textFirstName.text,
                    textLastName.text,
                    textPhoneNumber.text,
                    textAmount.text
                )
            }
            setupInputLayouts()
        }
    }

    private fun setupInputLayouts() {
        with(binding) {
            updateFieldRequirementFeedback(textFirstName, textLastName, textPhoneNumber, textAmount)
            textCountry.editText?.setOnFocusChangeListener { view, focus ->
                if (focus) {
                    requireActivity().hideKeyboard(view)
                }
            }

            textAmount.editText?.run {
                addTextChangedListener(
                    afterTextChanged = {
                        viewModel.onAmountChanged(it.toString())
                    }
                )
                setOnEditorActionListener { _, actionId, _ ->
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        buttonSend.performClick()
                        return@setOnEditorActionListener true
                    }
                    false
                }
            }
        }
    }

    private fun updateFieldRequirementFeedback(vararg inputLayouts: TextInputLayout) {
        inputLayouts.forEach { input ->
            input.editText?.addTextChangedListener {
                input.hideErrorIfNotEmpty()
            }
            input.editText?.setOnFocusChangeListener { _, focus ->
                if (!focus) input.showErrorIfEmpty()
            }
        }
    }

    private fun setupObservers() {
        with(viewModel) {
            viewState.observe(viewLifecycleOwner) {
                when (it) {
                    is ViewState.Initial -> showInitialState(it.countriesName)
                    is ViewState.Default -> showDefaultState(it.country)
                    is ViewState.GeneralError -> showErrorState(it.dialog)
                    is ViewState.InputFieldError -> showInputErrorState(it.inputFieldStatus)
                    is ViewState.Finish -> showFinishState(it.dialog)
                    else -> Unit
                }
                showLoadingState(it.loading)
            }
            recipientCurrencyBinaryValue.observe(
                viewLifecycleOwner,
                ::updateRecipientBinaryCurrency
            )
        }
    }

    private fun updateRecipientBinaryCurrency(recipientCurrency: RecipientCurrencyBinaryValue) {
        binding.textAmount.helperText =
            getString(
                R.string.input_text_amount_helper,
                recipientCurrency.prefix,
                recipientCurrency.value
            )
        binding.buttonSend.isEnabled = recipientCurrency.isValidAmount
    }
    // endregion

    // region - States
    private fun showInitialState(countriesName: List<String>) {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_menu_item,
            countriesName
        )
        (binding.dropdownMenu as? AutoCompleteTextView)?.run {
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
                getString(R.string.input_text_amount_helper, country.currencyPrefix, "")

            textPhoneNumber.prefixText = country.phonePrefix
            textPhoneNumber.editText?.text?.clear()
            textPhoneNumber.editText?.filters =
                arrayOf(InputFilter.LengthFilter(country.phoneNumberDigits))
            textPhoneNumber.requestFocus()
            defaultGroup.isVisible = true
        }
    }

    private fun showLoadingState(shouldShow: Boolean) {
        with(binding) {
            progressIndicatorContainer.isVisible = shouldShow
            if (shouldShow) buttonSend.isEnabled = false
        }
    }

    private fun showInputErrorState(inputFieldsStatus: InputFieldsStatus) {
        with(binding) {
            val invalidErrorMessage = getString(R.string.input_text_invalid_error)
            if (!inputFieldsStatus.isValidFirstName) textFirstName.error = invalidErrorMessage
            if (!inputFieldsStatus.isValidLastName) textLastName.error = invalidErrorMessage
            if (!inputFieldsStatus.isValidPhoneNumber) textPhoneNumber.error = invalidErrorMessage
        }
    }

    private fun showFinishState(dialog: TransactionDialog) {
        if (dialog is TransactionDialog.Complete) {
            resetState()
        }
        requireContext().showDialog(dialog.data)
    }

    private fun resetState() {
        with(binding) {
            clearFields(
                textFirstName,
                textLastName,
                textCountry,
                textPhoneNumber,
                textLastName
            )
            defaultGroup.isVisible = false
        }
    }

    private fun clearFields(vararg inputLayouts: TextInputLayout) {
        inputLayouts.forEach {
            it.editText?.text?.clear()
        }
    }

    private fun showErrorState(errorDialog: ErrorDialog) {
        requireContext().showDialog(errorDialog.data)
    }
    // endregion

    // region - Helpers
    private fun TextInputLayout.hideErrorIfNotEmpty() {
        if (text.isNotBlank()) {
            isErrorEnabled = false
        }
    }

    private fun TextInputLayout.showErrorIfEmpty() {
        if (text.isBlank()) {
            isErrorEnabled = true
            error = getString(R.string.input_text_required_error)
        } else {
            isErrorEnabled = false
        }
    }
    // endregion
}