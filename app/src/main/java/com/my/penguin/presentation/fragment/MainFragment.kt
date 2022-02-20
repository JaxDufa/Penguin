package com.my.penguin.presentation.fragment

import android.app.Activity
import android.os.Bundle
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.my.penguin.R
import com.my.penguin.databinding.FragmentMainBinding
import com.my.penguin.presentation.models.Country
import com.my.penguin.presentation.models.CurrencyBinaryValue
import com.my.penguin.presentation.models.Transaction
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainFragment : Fragment() {

    private val viewModel: MainViewModel by viewModel()

    private var _binding: FragmentMainBinding? = null

    // Valid between onCreateView and onDestroyView
    private val binding get() = _binding!!

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
                it.hideKeyboard()
                it.requestFocus()
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
            textCountry.editText?.setOnFocusChangeListener { view, focus ->
                if (focus) {
                    view.hideKeyboard()
                }
            }

            listenFieldChanges(textFirstName, textLastName, textPhoneNumber, textAmount)
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

    private fun listenFieldChanges(vararg inputLayouts: TextInputLayout) {
        inputLayouts.forEach { input ->
            input.editText?.addTextChangedListener {
                if (input.text.isNotBlank()) {
                    input.isErrorEnabled = false
                }
            }
            input.editText?.setOnFocusChangeListener { _, focus ->
                if (!focus) {
                    input.listenFieldChanges()
                }
            }
        }
    }

    private fun TextInputLayout.listenFieldChanges() {
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
                    is ViewState.Initial -> showInitialState(it.countriesName)
                    is ViewState.Default -> showDefaultState(it.country)
                    is ViewState.Error -> showErrorState(it.type)
                    is ViewState.InputFieldError -> showInputErrorState(
                        it.firstName,
                        it.lastName,
                        it.phoneNumber
                    )
                    is ViewState.Confirm -> showConfirmState(it.transaction)
                    is ViewState.Complete -> showCompleteState(it.transaction)
                    else -> Unit
                }
                showLoadingState(it.loading)
            }
            currencyBinaryFinalValue.observe(viewLifecycleOwner, ::updateWithCurrency)
        }
    }

    private fun updateWithCurrency(currency: CurrencyBinaryValue) {
        binding.textAmount.helperText =
            getString(R.string.input_text_amount_helper, currency.prefix, currency.value)
        binding.buttonSend.isEnabled = currency.value.isNotBlank()
    }
    // endregion

    // region - States
    private fun showInitialState(countriesName: List<String>) {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_menu_item,
            countriesName
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
        binding.progressIndicatorContainer.isVisible = shouldShow
    }

    private fun showInputErrorState(
        showFirstNameError: Boolean,
        showLastNameError: Boolean,
        showPhoneError: Boolean
    ) {
        with(binding) {
            if (showFirstNameError) textFirstName.error =
                getString(R.string.input_text_invalid_error)
            if (showLastNameError) textLastName.error = getString(R.string.input_text_invalid_error)
            if (showPhoneError) textPhoneNumber.error = getString(R.string.input_text_invalid_error)
        }
    }

    private fun showConfirmState(transaction: Transaction) {
        buildSimpleDialog(
            resources.getString(R.string.confirm_title),
            resources.getString(
                R.string.confirm_message,
                transaction.amount,
                transaction.phonePrefix,
                transaction.phoneNumber
            )
        ).setPositiveButton(resources.getString(R.string.confirm_positive_action)) { _, _ ->
            viewModel.onConfirmAction()
        }.setNegativeButton(resources.getString(R.string.confirm_negative_action)) { dialog, _ ->
            dialog.dismiss()
        }.show()
    }

    private fun showCompleteState(transaction: Transaction) {
        resetState()

        buildSimpleDialog(
            resources.getString(R.string.transaction_complete_title),
            resources.getString(
                R.string.transaction_complete_message,
                transaction.amount,
                transaction.phonePrefix,
                transaction.phoneNumber
            )
        ).setPositiveButton(resources.getString(R.string.transaction_positive_action)) { dialog, _ ->
            dialog.dismiss()
        }.show()
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

    private fun showErrorState(errorType: ErrorType) {
        buildSimpleDialog(
            resources.getString(errorType.title),
            resources.getString(errorType.message)
        ).setPositiveButton(resources.getString(R.string.error_positive_button)) { _, _ ->
            viewModel.onTryAgain()
        }.show()
    }
    // endregion

    private fun buildSimpleDialog(title: String, message: String): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(requireContext())
            .setCancelable(false)
            .setTitle(title)
            .setMessage(message)
    }

    private fun View.hideKeyboard() {
        val imm =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}