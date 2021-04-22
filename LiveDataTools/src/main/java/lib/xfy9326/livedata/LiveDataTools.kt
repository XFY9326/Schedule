@file:Suppress("unused")
@file:JvmName("LiveDataTools")

package lib.xfy9326.livedata

import androidx.lifecycle.*

typealias MutableEventLiveData<T> = MutableLiveData<Event<T>>
typealias MutableNotifyLiveData = MutableLiveData<Notify>
typealias EventLiveData<T> = LiveData<Event<T>>
typealias NotifyLiveData = LiveData<Notify>

fun <T> LiveData<T>.asEventLiveData(): EventLiveData<T> =
    map {
        Event(it)
    }

fun <T> LiveData<T>.asNotifyLiveData(): NotifyLiveData =
    map {
        Notify()
    }

fun <T> MutableEventLiveData<T>.postEvent(value: T) {
    postValue(Event(value))
}

fun <T> MutableEventLiveData<T>.setEvent(value: T) {
    this.value = Event(value)
}

@JvmSynthetic
fun <T> LiveData<Event<T>>.observeEvent(lifecycleOwner: LifecycleOwner, tag: String? = null, observer: (T) -> Unit) =
    EventObserver(tag, observer).also {
        observe(lifecycleOwner, it)
    }

@JvmOverloads
fun <T> LiveData<Event<T>>.observeEvent(lifecycleOwner: LifecycleOwner, tag: String? = null, observer: Observer<T>) =
    EventObserver(tag, observer).also {
        observe(lifecycleOwner, it)
    }

@JvmSynthetic
fun <T> LiveData<Event<T>>.observeEventForever(tag: String? = null, observer: (T) -> Unit) =
    EventObserver(tag, observer).also {
        observeForever(it)
    }

@JvmOverloads
fun <T> LiveData<Event<T>>.observeEventForever(tag: String? = null, observer: Observer<T>) =
    EventObserver(tag, observer).also {
        observeForever(it)
    }

fun MutableNotifyLiveData.postNotify() {
    postValue(Notify())
}

fun MutableNotifyLiveData.setNotify() {
    value = Notify()
}

@JvmSynthetic
fun <L : LiveData<Notify>> L.observeNotify(lifecycleOwner: LifecycleOwner, tag: String? = null, observer: () -> Unit) =
    NotifyObserver(tag, observer).also {
        observe(lifecycleOwner, it)
    }

@JvmOverloads
fun LiveData<Notify>.observeNotify(lifecycleOwner: LifecycleOwner, tag: String? = null, observer: Observer<Unit>) =
    NotifyObserver(tag, observer).also {
        observe(lifecycleOwner, it)
    }

@JvmSynthetic
fun LiveData<Notify>.observeNotifyForever(tag: String? = null, observer: () -> Unit) =
    NotifyObserver(tag, observer).also {
        observeForever(it)
    }

@JvmOverloads
fun LiveData<Notify>.observeNotifyForever(tag: String? = null, observer: Observer<Unit>) =
    NotifyObserver(tag, observer).also {
        observeForever(it)
    }

fun <T> LiveData<T>.addAsSource(target: MediatorLiveData<T>, immediate: Boolean = true): LiveData<T> {
    target.addSource(this) {
        if (immediate) {
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