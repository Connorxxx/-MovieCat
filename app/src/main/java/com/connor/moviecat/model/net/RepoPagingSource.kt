package com.connor.moviecat.model.net

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.connor.moviecat.utlis.fire
import com.drake.logcat.LogCat
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

class RepoPagingSource(
    private val client: HttpClient,
    private val path: String
) : PagingSource<Int, MovieResult>() {

    override suspend fun load(params: LoadParams<Int>) = fire {
        val page = params.key ?: 1
        val repoResponse = movie(path, page)
        LoadResult.Page(
            data = repoResponse.results,
            prevKey = if (page > 1) page - 1 else null,
            nextKey = if (page != repoResponse.totalPages) page + 1 else null
        )
    }

    override fun getRefreshKey(state: PagingState<Int, MovieResult>) =
        state.anchorPosition?.let {
            val anchorPage = state.closestPageToPosition(it)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    private suspend fun movie(path: String, page: Int) = client.get(path) {
        parameter("page", page)
    }.body<Movie>()
}