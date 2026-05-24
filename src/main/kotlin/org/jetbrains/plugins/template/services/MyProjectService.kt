package org.jetbrains.plugins.template.services

import com.intellij.openapi.components.*
import org.jetbrains.plugins.template.api.ApiRequest

@State(name = "ApiHistoryState", storages = [Storage("api_history.xml")])
@Service(Service.Level.PROJECT)
class MyProjectService : PersistentStateComponent<MyProjectService.State> {

    class State {
        var savedRequests: MutableList<ApiRequest> = mutableListOf()
    }

    private var myState = State()

    override fun getState() = myState

    override fun loadState(state: State) {
        myState = state
    }

    fun addRequest(req: ApiRequest) {
        myState.savedRequests.add(req)
    }
}