package com.test.videodemo.media

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 *  Author:   RandBII
 *  DATE:   2020/1/6
 *  Description:
 */

class Frame {

    var buffer: ByteBuffer? = null

    var bufferInfo = MediaCodec.BufferInfo()
        private set

    fun setBufferInfo(info: MediaCodec.BufferInfo) {
        bufferInfo.set(info.offset, info.size, info.presentationTimeUs, info.flags)
    }

}