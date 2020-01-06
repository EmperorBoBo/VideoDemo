package com.test.videodemo.media

import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 *  Author:   RandBII
 *  DATE:   2020/1/6
 *  Description: 音视频分离器
 */

interface IExtractor {

    fun getFormat(): MediaFormat?

    /**
     * 音视频数据
     */
    fun readBuffer(byteBuffer: ByteBuffer): Int

    fun getCurrentTimeStamp(): Long

    fun getSampleFlag(): Int

    /**
     * 跳到指定位置，并且返回改帧的时间戳
     */
    fun seek(pos: Long): Long

    fun setStartPos(pos: Long)
    
    fun stop()


}