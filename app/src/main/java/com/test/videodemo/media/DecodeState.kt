package com.test.videodemo.media

/**
 *  Author:   RandBII
 *  DATE:   2020/1/6
 *  Description: 解码状态
 */
enum class DecodeState {
    START,
    DECODING,
    PAUSE,
    SEEKING,
    FINISH,
    STOP
}