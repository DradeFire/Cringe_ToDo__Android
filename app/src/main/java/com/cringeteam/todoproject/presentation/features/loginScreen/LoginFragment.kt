package com.cringeteam.todoproject.presentation.features.loginScreen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import com.cringeteam.todoproject.R
import com.cringeteam.todoproject.common.logger.Logger
import com.cringeteam.todoproject.common.state.ScreenState
import com.cringeteam.todoproject.databinding.FragmentLoginBinding
import com.cringeteam.todoproject.presentation.base.BaseFragment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class LoginFragment : BaseFragment<FragmentLoginBinding, LoginViewModel>() {

    override val inflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLoginBinding =
        FragmentLoginBinding::inflate

    override val viewModelClass: Class<LoginViewModel> = LoginViewModel::class.java

    override val screenName: String = SCREEN_NAME

    override fun initUI() {
        super.initUI()

        val drawerLayout = requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        val toolbar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolbar.isVisible = false
    }

    override fun initObservers() {
        super.initObservers()

        viewModel?.let { viewModel ->
            compositeDisposable?.add(
                viewModel.screenState
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { state ->
                            when (state) {
                                ScreenState.Waiting -> {
                                    Logger.log("State is waiting")
                                    binding?.loginButton?.isEnabled = true
                                    binding?.progressBar?.isVisible = false
                                }
                                ScreenState.Loading -> {
                                    Logger.log("State is loading")
                                    binding?.loginButton?.isEnabled = false
                                    binding?.progressBar?.isVisible = true
                                }
                                ScreenState.Success -> {
                                    Logger.log("State is success")
                                    findNavController().navigate(R.id.navigate_loginScreen_to_TasksScreen)
                                }

                                ScreenState.Error -> TODO("Add Error state and show error toast")

                                null -> TODO("Add if we get null")
                            }
                        },
                        { error ->
                            Logger.log("LoginFragment::initObservers(), screenState error: ${error.localizedMessage}")
                        }
                    )
            )
        }
    }

    override fun initButtons() {
        super.initButtons()

        binding?.let { bindingNotNull ->
            with(bindingNotNull) {
                loginButton.setOnClickListener {
                    val login: String = loginEditText.text.toString()
                    val password: String = passwordEditText.text.toString()
                    viewModel?.onLoginClick(login, password)
                }

                registrationButton.setOnClickListener {
                    findNavController().navigate(R.id.navigate_loginScreen_to_registrationScreen)
                }
            }
        }
    }

    companion object {
        private const val SCREEN_NAME = "login screen"
    }
}