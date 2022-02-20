package com.my.penguin.presentation.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.my.penguin.data.ExchangeRateService
import com.my.penguin.databinding.FragmentMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
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

        setupObservers()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    private fun setupObservers() {
        viewModel.stateViewState.observe(this) {
            when(it) {
                is ViewState.Complete -> TODO()
                is ViewState.CurrentRate -> TODO()
                ViewState.Loading -> TODO()
                ViewState.NetworkError -> TODO()
                ViewState.UnknownError -> TODO()
            }
        }
    }
}