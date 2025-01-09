package com.example.bridgelink.signals

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel

class SignalViewModel {
    class SignalViewModel : ViewModel() {
        private val _signals = mutableStateListOf<Signal>()
        val signals: List<Signal> = _signals

        fun addSignal(signal: Signal) {
            _signals.add(signal)
        }
    }
}