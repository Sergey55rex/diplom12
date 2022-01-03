package ru.kot1.demo.auth

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.kot1.demo.api.ApiService
import ru.kot1.demo.dto.AuthState
import ru.kot1.demo.error.ApiError
import ru.kot1.demo.repository.AppNetState
import ru.kot1.demo.repository.AuthMethods
import javax.inject.Inject


class AppAuth @Inject constructor(
    val context: Context,
    val apiService: ApiService,
    var repository: AuthMethods
) {
    companion object {
        private lateinit var prefs: SharedPreferences
        const val idKey = "id"
        const val tokenKey = "token"

        fun initPrefs(context: Context) {
            prefs = context.getSharedPreferences("authX", Context.MODE_PRIVATE)
        }

        fun getAuthInfo(context: Context): Pair<Long, String?> {
            initPrefs(context)
            return prefs.getLong(idKey, 0) to
                    prefs.getString(tokenKey, null)
        }
    }

    private val _authStateFlow: MutableStateFlow<AuthState> = MutableStateFlow(AuthState())


    fun checkAmLogined() {
        val (id, token) = getAuthInfo(context)
        if (id == 0L || token == null) {  //ничего нет- чистим
            cleanToken()
            // токена нет - ничего не делать

        } else {
            // проверить что ключ валидный
            checkTheToken({
                //оказался валидный? - присваиваем
                _authStateFlow.value = AuthState(id, token)
            }, {
                cleanToken()
            })

        }
    }

    private fun cleanToken() {
        _authStateFlow.value = AuthState()
        with(prefs.edit()) {
            clear()
            apply()
        }
    }

    private fun checkTheToken(success: () -> Unit, failure: () -> Unit) =
        CoroutineScope(Dispatchers.Default).launch {
            if (repository.checkToken()) {
                success()
            } else {
                failure()
            }
        }


    val authStateFlow: StateFlow<AuthState> = _authStateFlow.asStateFlow()

    val myId: Long
    get() = authStateFlow.value.id

    //----------------------


    fun authUser(login: String, pass: String, callBack: (AppNetState) -> Unit) =
        CoroutineScope(Dispatchers.Default).launch {
            val result : (state: AppNetState)-> Unit = { state ->
                Handler(Looper.getMainLooper()).post {
                    callBack(state)
                }
            }
            when (repository.checkConnection()) {
                AppNetState.CONNECTION_ESTABLISHED -> {
                    initPrefs(context)
                    try {
                        repository.authUser(login, pass) { id, token ->
                            setAuth(id, token)
                        }
                        result(AppNetState.CONNECTION_ESTABLISHED)
                    } catch (e: ApiError) {
                        if (e.status == 404) {
                            result(AppNetState.THIS_USER_NOT_REGISTERED)
                        }
                        if (e.status == 400) {
                            result(AppNetState.INCORRECT_PASSWORD)
                        }
                        if (e.status == 500) {
                            result(AppNetState.SERVER_ERROR_500)
                        }

                    } catch (e: IllegalStateException) {
                        Log.e("exc", "  error ${e.javaClass.simpleName}  ${e.printStackTrace()} ${e.message}")
                    } catch (e: Exception) {
                        Log.e("exc", "  error ${e.javaClass.simpleName}")
                    }
                }
                AppNetState.NO_INTERNET -> {
                    result(AppNetState.NO_INTERNET)
                }

                AppNetState.NO_SERVER_CONNECTION -> {
                    result(AppNetState.NO_SERVER_CONNECTION)
                }
            }
        }


    fun newUserRegistration(
        login: String,
        pass: String,
        name: String,
        uri: String? = null,
        callBack: (AppNetState) -> Unit
    ) =
        CoroutineScope(Dispatchers.Default).launch {
            initPrefs(context)
            when (repository.checkConnection()) {
                AppNetState.CONNECTION_ESTABLISHED -> {
                    try {
                        repository.regNewUser(login, pass, name, uri) { id, token ->
                            cleanToken()
                            setAuth(id, token)
                            Handler(Looper.getMainLooper()).post {
                                callBack(AppNetState.CONNECTION_ESTABLISHED)
                            }
                        }
                    } catch (e: ApiError) {
                        if (e.status == 404) {
                            Handler(Looper.getMainLooper()).post {
                                callBack(AppNetState.THIS_USER_NOT_REGISTERED)
                            }
                        }
                        if (e.status == 400) {
                            Handler(Looper.getMainLooper()).post {
                                callBack(AppNetState.INCORRECT_PASSWORD)
                            }
                        }
                        if (e.status == 500) {
                            Handler(Looper.getMainLooper()).post {
                                callBack(AppNetState.SERVER_ERROR_500)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("exc", " error ${e.javaClass.simpleName}")
                    }
                }

                AppNetState.NO_INTERNET -> {
                    Handler(Looper.getMainLooper()).post {
                        callBack(AppNetState.NO_INTERNET)
                    }
                }

                AppNetState.NO_SERVER_CONNECTION -> {
                    Handler(Looper.getMainLooper()).post {
                        callBack(AppNetState.NO_SERVER_CONNECTION)
                    }
                }

            }
        }

    //-------


    @Synchronized
    private fun setAuth(id: Long, token: String) {
       _authStateFlow.value = AuthState(id, token)
         with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            apply()
        }
    }

    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = AuthState()
        with(prefs.edit()) {
            clear()
            apply()
        }
    }


}

