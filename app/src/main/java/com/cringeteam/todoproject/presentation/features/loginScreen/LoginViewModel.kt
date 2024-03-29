package com.cringeteam.todoproject.presentation.features.loginScreen

import com.cringeteam.todoproject.common.logger.Logger
import com.cringeteam.todoproject.common.state.ScreenState
import com.cringeteam.todoproject.domain.usecases.SendLoginUserUseCase
import com.cringeteam.todoproject.presentation.base.BaseViewModel
import com.cringeteam.todoproject.presentation.model.login.LoginUserVo
import com.cringeteam.todoproject.presentation.model.login.LoginRequestFormatter
import com.cringeteam.todoproject.presentation.model.login.LoginResponseFormatter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject

class LoginViewModel : BaseViewModel() {

    private var loginRequestDisposable: Disposable? = null

    private val sendLoginUserUseCase = SendLoginUserUseCase()
    private val loginRequestFormatter = LoginRequestFormatter()
    private val loginResponseFormatter = LoginResponseFormatter()

    private val _screenState: BehaviorSubject<ScreenState> =
        BehaviorSubject.createDefault(ScreenState.Waiting)
    val screenState: BehaviorSubject<ScreenState> get() = _screenState

    fun onLoginClick(login: String, password: String) {

        val loginRequest = loginRequestFormatter.format(
            LoginUserVo(
                login = login,
                password = password,
            )
        )

        loginRequestDisposable = sendLoginUserUseCase.execute(loginRequest)
            .map { loginResponse ->
                loginResponseFormatter.format(loginResponse)
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                _screenState.onNext(ScreenState.Loading)
            }
            .subscribe(
                {
                    _screenState.onNext(ScreenState.Success)
                },
                { error ->
                    Logger.log("LoginViewModel::onLoginClick() error: $error")
                    _screenState.onNext(ScreenState.Waiting)
                }
            )
    }

    override fun onCleared() {
        loginRequestDisposable?.dispose()
        loginRequestDisposable = null
        super.onCleared()
    }
}