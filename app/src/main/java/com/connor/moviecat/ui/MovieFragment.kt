package com.connor.moviecat.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.connor.moviecat.R
import com.connor.moviecat.contract.onScroll
import com.connor.moviecat.databinding.FragmentMovieBinding
import com.connor.moviecat.model.net.ApiPath
import com.connor.moviecat.tag.Tag
import com.connor.moviecat.ui.adapter.FooterAdapter
import com.connor.moviecat.ui.adapter.MovieAdapter
import com.connor.moviecat.viewmodel.MainViewModel
import com.drake.channel.receiveTag
import com.drake.engine.base.EngineFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class MovieFragment : Fragment(R.layout.fragment_movie) {

    private lateinit var binding: FragmentMovieBinding
    private lateinit var movieAdapter: MovieAdapter

    private val viewModel: MainViewModel by sharedViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMovieBinding.inflate(
            inflater,
            container,
            false
        )
        val view = binding.root
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.rv.layoutManager = layoutManager
        movieAdapter = MovieAdapter(requireActivity(), "movie")
        binding.rv.adapter = movieAdapter.withLoadStateFooter(
            FooterAdapter { movieAdapter.retry() }
        )
        viewLifecycleOwner.lifecycleScope.launch {
            launch {
                viewModel.getPagingData(ApiPath.MOVIE).collect {
                    movieAdapter.submitData(it)
                }
            }
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.event.collect {
                    it.onScroll { binding.rv.scrollToPosition(0) }
                }
            }
        }
        return view
    }
}