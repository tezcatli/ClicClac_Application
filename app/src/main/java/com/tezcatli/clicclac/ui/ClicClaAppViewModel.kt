package com.tezcatli.clicclac.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ClicClaAppViewModel() :  ViewModel() {
    var fullScreen by mutableStateOf(false)

/*
    public fun isFullScreen() : Boolean {
        return fullScreen
    }

    public fun setFullScreen(fullscreen : Boolean) {
        fullScreen = fullscreen
    }
    */
}

