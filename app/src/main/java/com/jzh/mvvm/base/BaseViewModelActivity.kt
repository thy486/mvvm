package com.jzh.mvvm.base

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.jzh.mvvm.mvvm.viewModel.CommonViewModel
import com.jzh.mvvm.utils.toast
import kotlinx.coroutines.TimeoutCancellationException
import kotlin.coroutines.cancellation.CancellationException

abstract class BaseViewModelActivity<VM : CommonViewModel> : BaseActivity() {

    protected lateinit var viewModel: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        initVM()
        super.onCreate(savedInstanceState)
        startObserve()
        startHttp()
    }

    private fun initVM() {
        providerVMClass().let {
            viewModel = ViewModelProvider(this).get(it)
            lifecycle.addObserver(viewModel)
        }
    }

    //viewModel实例
    abstract fun providerVMClass(): Class<VM>

    private fun startObserve() {
        //处理一些通用异常，比如网络超时等
        viewModel.run {
            getError().observe(this@BaseViewModelActivity, {
                hideLoading()
                hideSearchLoading()
                requestError(it)
            })
            getFinally().observe(this@BaseViewModelActivity, {
                requestFinally(it)
            })
        }
    }

    //接口请求完毕，子类可以重写此方法做一些操作
    open fun requestFinally(it: Int?) {}

    //接口请求出错，子类可以重写此方法做一些操作
    open fun requestError(it: Exception?) {
        //处理一些已知异常
        it?.run {
            when (it) {
                is CancellationException -> Log.d("${TAG}--->接口请求取消", it.message.toString())
                else -> toast(it.message.toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(viewModel)
    }
}