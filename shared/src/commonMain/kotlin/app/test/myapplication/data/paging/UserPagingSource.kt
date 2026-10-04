package app.test.myapplication.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import app.test.myapplication.data.model.User
import app.test.myapplication.data.network.ReqResApiClient

class UserPagingSource(
    private val apiClient: ReqResApiClient = ReqResApiClient(),
    private val perPage: Int = 6
) : PagingSource<Int, User>() {

    override fun getRefreshKey(state: PagingState<Int, User>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, User> {
        val page = params.key ?: 1
        return try {
            val response = apiClient.getUsers(page = page, perPage = perPage)
            val nextKey = if (page >= response.totalPages) null else page + 1
            val prevKey = if (page == 1) null else page - 1

            LoadResult.Page(
                data = response.data,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}
