@file:Suppress("unused")

package tool.xfy9326.schedule.tools.livedata

import androidx.lifecycle.*

typealias MutableEventLiveData<T> = MutableLiveData<Event<T>>
typealias MutableNotifyLiveData = MutableLiveData<Notify>
typealias EventLiveData<T> = LiveData<Event<T>>
typealias NotifyLiveData = LiveData<Notify>

fun <T, L : LiveData<T>> L.asEventLiveData(): EventLiveData<T> =
    map {
        Event(it)
    }

fun <T, L : LiveData<T>> L.asNotifyLiveData(): NotifyLiveData =
    map {
        Notify()
    }

fun <T> MutableEventLiveData<T>.postEvent(value: T) {
    postValue(Event(value))
}

fun <T, L : LiveData<Event<T>>> L.observeEvent(lifecycleOwner: LifecycleOwner, tag: String? = null, observer: (T) -> Unit) =
    EventObserver(tag, observer).also {
        observe(lifecycleOwner, it)
    }

fun <T, L : LiveData<Event<T>>> L.observeEventForever(tag: String? = null, observer: (T) -> Unit) =
    EventObserver(tag, observer).also {
        observeForever(it)
    }

fun MutableNotifyLiveData.notify() {
    postValue(Notify())
}

fun <L : LiveData<Notify>> L.observeNotify(lifecycleOwner: LifecycleOwner, tag: String? = null, observer: () -> Unit) =
    NotifyObserver(tag, observer).also {
        observe(lifecycleOwner, it)
    }

fun <L : LiveData<Notify>> L.observeNotifyForever(tag: String? = null, observer: () -> Unit) =
    NotifyObserver(tag, observer).also {
        observeForever(it)
    }

fun <T> LiveData<T>.addAsSource(target: MediatorLiveData<T>, mainThread: Boolean = true): LiveData<T> {
    target.addSource(this) {
        if (mainThread) {
            target.value = it
        } else {
            target.postValue(it)
        }
    }
    return this
}

fun <T : Any> LiveData<T?>.filterNotNull(): LiveData<T> {
    val result = MediatorLiveData<T>()
    result.addSource(this) { x ->
        if (x != null) result.value = x
    }
    return result
}