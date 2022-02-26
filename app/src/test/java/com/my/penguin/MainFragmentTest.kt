package com.my.penguin

import android.content.Context
import android.content.DialogInterface.BUTTON_POSITIVE
import android.os.Build
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider
import com.my.penguin.databinding.FragmentMainBinding
import com.my.penguin.presentation.fragment.*
import com.my.penguin.presentation.model.Country
import com.my.penguin.presentation.model.RecipientCurrencyBinaryValue
import com.my.penguin.presentation.model.Transaction
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.loadKoinModules
import org.koin.core.context.stopKoin
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog
import kotlin.test.*

@RunWith(RobolectricTestRunner::class)
@Config(
    manifest = Config.NONE,
    application = KoinTestApp::class,
    sdk = [Build.VERSION_CODES.S],
)
class MainFragmentTest : KoinTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val testViewState = MutableLiveData<ViewState>()
    private val testRecipientCurrencyBinaryValue = MutableLiveData<RecipientCurrencyBinaryValue>()
    private val mockViewModel = mockk<MainViewModel>(relaxed = true) {
        every { viewState } returns testViewState
        every { recipientCurrencyBinaryValue } returns testRecipientCurrencyBinaryValue
    }

    private val koinModule = module {
        single { mockViewModel }
    }

    private lateinit var binding: FragmentMainBinding
    private lateinit var scenario: FragmentScenario<MainFragment>

    @Before
    fun setUp() {
        loadKoinModules(koinModule)
        scenario = launchFragmentInContainer(themeResId = R.style.Theme_Penguin)
    }

    @After
    fun tearDown() {
        unloadKoinModules(koinModule)
        stopKoin()
        scenario.close()
    }

    @Test
    fun `when fragment is created, then view empty state is shown`() {
        launchFragment {
            assertTrue(binding.buttonSend.hasOnClickListeners())
            assertFalse(binding.textFirstName.isVisible)
            assertFalse(binding.textLastName.isVisible)
            assertFalse(binding.textCountry.isVisible)
            assertFalse(binding.textPhoneNumber.isVisible)
            assertFalse(binding.textAmount.isVisible)
            assertFalse(binding.buttonSend.isEnabled)
            assertFalse(binding.progressIndicatorContainer.isVisible)
        }
    }

    @Test
    fun `when fragment received Loading state, then loading state is shown`() {
        launchFragment {
            testViewState.value = ViewState.Loading

            assertTrue(binding.progressIndicatorContainer.isVisible)
        }
    }

    @Test
    fun `when fragment received Initial state, then initial state is shown`() {
        launchFragment {
            assertNull(binding.dropdownMenu.adapter)

            testViewState.value = ViewState.Initial(listOf("Brazil", "Kenya"))

            assertTrue(binding.textFirstName.isVisible)
            assertTrue(binding.textLastName.isVisible)
            assertTrue(binding.textCountry.isVisible)
            assertFalse(binding.textPhoneNumber.isVisible)
            assertFalse(binding.textAmount.isVisible)
            assertFalse(binding.buttonSend.isEnabled)
            assertFalse(binding.progressIndicatorContainer.isVisible)

            assertEquals(2, binding.dropdownMenu.adapter.count)
            assertEquals("Brazil", binding.dropdownMenu.adapter.getItem(0))
            assertEquals("Kenya", binding.dropdownMenu.adapter.getItem(1))
        }
    }

    @Test
    fun `when fragment received GeneralError state, then error state is shown`() {
        launchFragment {
            assertNull(ShadowAlertDialog.getLatestDialog())

            testViewState.value = ViewState.GeneralError(ErrorType.UnknownError)

            assertTrue(binding.buttonSend.hasOnClickListeners())
            assertFalse(binding.textFirstName.isVisible)
            assertFalse(binding.textLastName.isVisible)
            assertFalse(binding.textCountry.isVisible)
            assertFalse(binding.textPhoneNumber.isVisible)
            assertFalse(binding.textAmount.isVisible)
            assertFalse(binding.buttonSend.isEnabled)
            assertFalse(binding.progressIndicatorContainer.isVisible)

            assertNotNull(ShadowAlertDialog.getLatestDialog())

            (ShadowAlertDialog.getLatestDialog() as AlertDialog).getButton(BUTTON_POSITIVE)
                .performClick()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            verify { mockViewModel.onTryAgain() }
        }
    }

    @Test
    fun `when fragment received Default state, then default state is shown`() {
        launchFragment {

            testViewState.value = ViewState.Default(Country.Kenya)

            assertTrue(binding.buttonSend.hasOnClickListeners())
            assertTrue(binding.textPhoneNumber.isVisible)
            assertTrue(binding.textAmount.isVisible)
            assertFalse(binding.buttonSend.isEnabled)
            assertFalse(binding.progressIndicatorContainer.isVisible)
        }
    }

    @Test
    fun `when fragment received InputFieldError state, then input error state is shown`() {
        launchFragment {

            assertFalse(binding.textFirstName.isErrorEnabled)
            assertFalse(binding.textLastName.isErrorEnabled)
            assertFalse(binding.textPhoneNumber.isErrorEnabled)

            testViewState.value = ViewState.InputFieldError(
                InputFieldsStatus(
                    isValidFirstName = true,
                    isValidLastName = true,
                    isValidPhoneNumber = true
                )
            )

            assertFalse(binding.textFirstName.isErrorEnabled)
            assertFalse(binding.textLastName.isErrorEnabled)
            assertFalse(binding.textPhoneNumber.isErrorEnabled)
        }
    }

    @Test
    fun `when fragment received Confirm state, then confirm state is shown`() {
        launchFragment {
            assertNull(ShadowAlertDialog.getLatestDialog())

            testViewState.value = ViewState.Confirm(Transaction("name", "9999", "11"))

            assertNotNull(ShadowAlertDialog.getLatestDialog())

            (ShadowAlertDialog.getLatestDialog() as AlertDialog).getButton(BUTTON_POSITIVE)
                .performClick()
            Shadows.shadowOf(Looper.getMainLooper()).idle()
            verify { mockViewModel.onConfirmAction() }
        }
    }

    @Test
    fun `when fragment received Complete state, then complete state is shown`() {
        launchFragment {
            assertNull(ShadowAlertDialog.getLatestDialog())

            testViewState.value = ViewState.Complete(Transaction("name", "9999", "11"))

            assertNotNull(ShadowAlertDialog.getLatestDialog())
        }
    }

    @Test
    fun `when fragment received Recipient currency value, then update amount helper text`() {
        launchFragment {
            val prefix = "MONEY"
            val amount = "123456"

            assertFalse(binding.buttonSend.isEnabled)
            assertNull(binding.textAmount.helperText)

            testViewState.value = ViewState.Default(Country.Nigeria)
            testRecipientCurrencyBinaryValue.value = RecipientCurrencyBinaryValue(prefix, amount)

            assertTrue(binding.buttonSend.isEnabled)
            assertEquals(
                context.getString(R.string.input_text_amount_helper, prefix, amount),
                binding.textAmount.helperText
            )
        }
    }

    private fun launchFragment(block: (MainFragment) -> Unit) {
        scenario.onFragment {
            binding = FragmentMainBinding.bind(it.requireView())
            block(it)
        }
    }
}