package com.test.videodemo.media

/**
 *  Author:   RandBII
 *  DATE:   2020/1/6
 *  Description: 解码器状态监听
 */

interface IDecoderStateListener {

    fun decoderPrepare(baseDecoder: BaseDecoder)

    fun decoderReady()

    fun decoderRunning()

    fun decoderPause()

    fun decodeOnFrame(
        baseDecoder: BaseDecoder,
        frame: Frame
    )

    fun decoderFinish(baseDecoder: BaseDecoder)

    fun decoderDestroy(baseDecoder: BaseDecoder)

    fun decoderError()

}
