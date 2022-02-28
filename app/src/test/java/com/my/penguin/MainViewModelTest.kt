package com.my.penguin

import android.telephony.PhoneNumberUtils
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.my.penguin.data.Result
import com.my.penguin.data.model.ExchangeRate
import com.my.penguin.data.model.ExchangeRates
import com.my.penguin.data.repository.ExchangeRateRepository
import com.my.penguin.presentation.fragment.*
import com.my.penguin.presentation.model.Country
import com.my.penguin.presentation.model.RecipientCurrencyBinaryValue
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertIs

@ExperimentalCoroutinesApi
class MainViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = TestCoroutineDispatcher()

    private val mockRepository: ExchangeRateRepository = mockk(relaxed = true)
    private val mockNetworkProvider: NetworkProvider = mockk()

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockkStatic(PhoneNumberUtils::class)
        every { PhoneNumberUtils.isGlobalPhoneNumber(any()) } returns true
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
        unmockkAll()
    }

    @Test
    fun `when view model is initialized, then request exchange rates and post Loading state`() {
        initViewModel()
        runTest {
            assertEquals(ViewState.Loading, viewModel.viewState.value)
            coVerify { mockRepository.loadExchangeRates() }
        }
    }

    @Test
    fun `when view model is initialized and repository returns success, then request exchange rates and post Initial state`() {
        coEvery { mockRepository.loadExchangeRates() } coAnswers {
            Result.Success(
                ExchangeRates(
                    ExchangeRate(1f),
                    ExchangeRate(1f),
                    ExchangeRate(1f),
                    ExchangeRate(1f)
                )
            )
        }

        initViewModel()
        runTest {
            assertEquals(ViewState.Initial(
                listOf(
                    Country.Kenya,
                    Country.Nigeria,
                    Country.Tanzania,
                    Country.Uganda
                ).map { it.name }
            ), viewModel.viewState.value)
        }
    }

    @Test
    fun `when view model is initialized and repository returns error, then request exchange rates and post Error state`() {
        every { mockNetworkProvider.isConnected } returns true
        coEvery { mockRepository.loadExchangeRates() } coAnswers {
            Result.Error(Exception())
        }

        initViewModel()
        runTest {
            assertIs<ViewState.GeneralError>(viewModel.viewState.value)
            val errorState = viewModel.viewState.value as ViewState.GeneralError
            assertIs<ErrorDialog.UnknownError>(errorState.dialog)
        }
    }

    @Test
    fun `when view model is initialized and repository returns error without network, then request exchange rates and post Error state`() {
        every { mockNetworkProvider.isConnected } returns false
        coEvery { mockRepository.loadExchangeRates() } coAnswers {
            Result.Error(Exception())
        }

        initViewModel()
        runTest {
            assertIs<ViewState.GeneralError>(viewModel.viewState.value)
            val errorState = viewModel.viewState.value as ViewState.GeneralError
            assertIs<ErrorDialog.NetworkError>(errorState.dialog)
        }
    }

    @Test
    fun `when country 0 is selected, then post Kenya Default state`() {
        initViewModel()
        runTest {
            viewModel.onCountrySelected(0)

            assertEquals(ViewState.Default(Country.Kenya), viewModel.viewState.value)
        }
    }

    @Test
    fun `when country 1 is selected, then post Nigeria Default state`() {
        initViewModel()
        runTest {
            viewModel.onCountrySelected(1)

            assertEquals(ViewState.Default(Country.Nigeria), viewModel.viewState.value)
        }
    }

    @Test
    fun `when country 2 is selected, then post Tanzania Default state`() {
        initViewModel()
        runTest {
            viewModel.onCountrySelected(2)

            assertEquals(ViewState.Default(Country.Tanzania), viewModel.viewState.value)
        }
    }

    @Test
    fun `when country 3 is selected, then post Uganda Default state`() {
        initViewModel()
        runTest {
            viewModel.onCountrySelected(3)

            assertEquals(ViewState.Default(Country.Uganda), viewModel.viewState.value)
        }
    }

    @Test
    fun `when country -1 is selected, then post nothing new`() {
        initViewModel()
        runTest {
            viewModel.onCountrySelected(-1)

            assertEquals(ViewState.Loading, viewModel.viewState.value)
        }
    }

    @Test
    fun `when amount is submitted without selecting country, then post nothing new`() {
        initViewModel()
        runTest {
            viewModel.onAmountChanged("101010101")

            assertEquals(ViewState.Loading, viewModel.viewState.value)
        }
    }

    @Test
    fun `when empty amount is submitted for Nigeria, then post nothing new`() {
        initViewModel()
        runTest {
            viewModel.onCountrySelected(1)
            viewModel.onAmountChanged("")

            assertEquals(
                RecipientCurrencyBinaryValue(Country.Nigeria.currencyPrefix, ""),
                viewModel.recipientCurrencyBinaryValue.value
            )
        }
    }

    @Test
    fun `when amount is submitted for Nigeria, then post expected recipient currency value`() {
        coEvery { mockRepository.loadExchangeRates() } coAnswers {
            Result.Success(
                ExchangeRates(
                    ExchangeRate(1f),
                    ExchangeRate(361.50f),
                    ExchangeRate(1f),
                    ExchangeRate(1f)
                )
            )
        }

        initViewModel()
        runTest {
            viewModel.onCountrySelected(1)
            viewModel.onAmountChanged("010110")

            assertEquals(
                RecipientCurrencyBinaryValue(Country.Nigeria.currencyPrefix, "1111100010001"),
                viewModel.recipientCurrencyBinaryValue.value
            )
        }
    }

    @Test
    fun `when a send without a selected country, then nothing new is posted`() {
        initViewModel()
        runTest {
            viewModel.onSendAction("", "", "", "")

            assertEquals(ViewState.Loading, viewModel.viewState.value)
        }
    }

    @Test
    fun `when a send with invalid fields, then InputFieldError is posted`() {
        initViewModel()
        runTest {
            viewModel.onCountrySelected(1)
            viewModel.onSendAction("", "", "", "")

            assertEquals(
                ViewState.InputFieldError(
                    InputFieldsStatus(
                        isValidFirstName = false,
                        isValidLastName = false,
                        isValidPhoneNumber = false
                    )
                ),
                viewModel.viewState.value
            )
        }
    }

    @Test
    fun `when a send with one invalid field, then InputFieldError is posted`() {
        initViewModel()
        runTest {
            viewModel.onCountrySelected(1)
            viewModel.onSendAction("lala", "lalal", "123", "")

            assertEquals(
                ViewState.InputFieldError(
                    InputFieldsStatus(
                        isValidFirstName = true,
                        isValidLastName = true,
                        isValidPhoneNumber = false
                    )
                ),
                viewModel.viewState.value
            )
        }
    }

    @Test
    fun `when a send with valid fields for Kenya, then Confirm is posted`() {
        initViewModel()
        runTest {
            viewModel.onCountrySelected(0)
            viewModel.onSendAction("Dr", "Penguin", "123456789", "1")

            assertIs<ViewState.Finish>(viewModel.viewState.value)
            val viewState = viewModel.viewState.value as ViewState.Finish
            assertIs<TransactionDialog.Confirm>(viewState.dialog)
        }
    }

    @Test
    fun `when a send with valid fields for Nigeria, then Confirm is posted`() {
        initViewModel()
        runTest {
            viewModel.onCountrySelected(1)
            viewModel.onSendAction("Dr", "Penguin", "1234567", "1")

            assertIs<ViewState.Finish>(viewModel.viewState.value)
            val viewState = viewModel.viewState.value as ViewState.Finish
            assertIs<TransactionDialog.Confirm>(viewState.dialog)
        }
    }

    @Test
    fun `when confirm is posted and clicked, then Loading is posted`() {
        initViewModel()
        runTest {
            viewModel.onCountrySelected(1)
            viewModel.onSendAction("Dr", "Penguin", "1234567", "1")

            assertIs<ViewState.Finish>(viewModel.viewState.value)
            val viewState = viewModel.viewState.value as ViewState.Finish

            viewState.dialog.data.positiveButton.onClick()

            assertIs<ViewState.Loading>(viewModel.viewState.value)
        }
    }

    @Test
    fun `when confirm is posted and clicked, then Completed is posted after advanced time`() {
        initViewModel()
        runTest {
            viewModel.onCountrySelected(1)
            viewModel.onSendAction("Dr", "Penguin", "1234567", "1")

            assertIs<ViewState.Finish>(viewModel.viewState.value)
            val viewState = viewModel.viewState.value as ViewState.Finish

            viewState.dialog.data.positiveButton.onClick()

            this.testScheduler.advanceUntilIdle()

            assertIs<ViewState.Finish>(viewModel.viewState.value)
            val newViewState = viewModel.viewState.value as ViewState.Finish
            assertIs<TransactionDialog.Complete>(newViewState.dialog)
        }
    }

    private fun initViewModel() {
        viewModel = MainViewModel(mockRepository, mockNetworkProvider)
    }
}