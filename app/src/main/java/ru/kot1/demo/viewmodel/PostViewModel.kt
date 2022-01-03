package ru.kot1.demo.viewmodel

import androidx.lifecycle.*
import androidx.paging.*
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.kot1.demo.auth.AppAuth
import ru.kot1.demo.model.*
import ru.kot1.demo.repository.AppEntities
import javax.inject.Inject


@HiltViewModel
@ExperimentalCoroutinesApi
class PostViewModel @Inject constructor(var repository: AppEntities ,
                                        var workManager: WorkManager,
                                        var auth: AppAuth
) : ViewModel() {
    private var postAuthorId : Long = -1

    val cachedposts = repository.pdata.cachedIn(viewModelScope)

    val feedModels = cachedposts.map { pagingData ->
        if (postAuthorId == -1L){
            pagingData.map { post ->
                    PostModel(post = post) as FeedModel
                }
        } else { pagingData.filter { postAuthorId == it.authorId.toLong()  }
            .map { post ->
                PostModel(post = post) as FeedModel
            }
        }
    }


    private val _dataState = SingleLiveEvent<FeedModelState>()
    val dataState: SingleLiveEvent<FeedModelState>
        get() = _dataState


    fun getWallById(id: Long) = viewModelScope.launch {
        postAuthorId = id
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getPostsById(id)
            _dataState.value = FeedModelState()

        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }


    fun refreshPosts() = viewModelScope.launch {
        getWallById(postAuthorId)
    }
}




