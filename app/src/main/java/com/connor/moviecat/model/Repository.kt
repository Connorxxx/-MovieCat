package com.connor.moviecat.model

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.filter
import com.connor.moviecat.model.net.*
import io.ktor.client.*
import kotlinx.coroutines.flow.map

class Repository(private val client: HttpClient) {

    fun getPagingData(path: String) = Pager(
        config = PagingConfig(20),
        pagingSourceFactory = { RepoPagingSource(client, path) }
    ).flow

    fun getSearchPagingData(path: String, query: String) = Pager(
        config = PagingConfig(20),
        pagingSourceFactory = { SearchPagingSource(client, path, query) }
    ).flow.map { pagingData ->
        pagingData.filter { it.posterPath != null }
            .filter { it.releaseDate?.length != 0 && it.firstAirDate?.length != 0 }
    }
}