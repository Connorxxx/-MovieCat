package com.connor.moviecat.ui

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.connor.moviecat.App.Companion.context
import com.connor.moviecat.BaseActivity
import com.connor.moviecat.R
import com.connor.moviecat.databinding.ActivitySearchBinding
import com.connor.moviecat.model.net.ApiPath
import com.connor.moviecat.ui.adapter.FooterAdapter
import com.connor.moviecat.ui.adapter.SearchAdapter
import com.connor.moviecat.utlis.showSnackBar
import com.connor.moviecat.utlis.textChanges
import com.connor.moviecat.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchActivity : BaseActivity(R.layout.activity_search) {

    private lateinit var searchAdapter: SearchAdapter

    private val viewModel: MainViewModel by viewModel()

    private val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }
    private val imm by lazy { context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setActionBarAndHome(binding.toolbar)
        initRV()
        initEditText()
    }

    private fun initRV() {
        with(binding.rv) {
            val manager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            layoutManager = manager
            searchAdapter = SearchAdapter(this@SearchActivity)
            adapter = searchAdapter.withLoadStateFooter(
                FooterAdapter { searchAdapter.retry() }
            )
            setOnTouchListener { view, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_MOVE -> imm.hideSoftInputFromWindow(windowToken, 0)
                    MotionEvent.ACTION_UP -> view.performClick()
                }
                super.onTouchEvent(motionEvent)
            }
        }
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun initEditText() {
        binding.imgClean.setOnClickListener {
            binding.etSearch.setText("")
        }
        with(binding.etSearch) {
            lifecycleScope.launch {
                textChanges().debounce(700)
                    .collect {
                        launch(Dispatchers.IO) {
                            viewModel.getSearchPagingData(ApiPath.SEARCH_MULTI, it.toString())
                                .collect { paging ->
                                    searchAdapter.submitData(paging)
                                }
                        }
                        binding.rv.scrollToPosition(0)
                    }
            }
            postDelayed({
                requestFocus()
                imm.showSoftInput(this, 0)
            }, 200)
            setOnEditorActionListener { textView, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH && textView.text.isNotBlank()) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        viewModel.getSearchPagingData(
                            ApiPath.SEARCH_MULTI,
                            textView.text.toString()
                        ).flowOn(Dispatchers.IO).collect {
                            searchAdapter.submitData(it)
                            binding.rv.scrollToPosition(0)
                        }
                    }
                    imm.hideSoftInputFromWindow(windowToken, 0)
                } else showSnackBar("Please Input")
                return@setOnEditorActionListener true
            }
        }
    }
}