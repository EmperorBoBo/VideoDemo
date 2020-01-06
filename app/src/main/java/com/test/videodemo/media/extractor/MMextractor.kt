package com.test.videodemo.media.extractor

import android.media.MediaExtractor
import android.media.MediaFormat
import java.nio.ByteBuffer

/**
 *  Author:   RandBII
 *  DATE:   2020/1/6
 *  Description: 音视频分离器
 */

class MMextractor(path: String?) {

    /**
     * 音视频分离器
     */
    private var mExtractor: MediaExtractor? = null

    /***
     * 音频通道索引
     */
    private var mAudioTrack = -1

    /**
     *视频通道索引
     */
    private var mVideoTrack = -1

    private var mCurSampleTime: Long = 0

    private var mStartPos: Long = 0

    init {
        mExtractor = MediaExtractor()
        mExtractor?.setDataSource(path!!)
    }


    /**
     * 获取视频格式参数
     */
    fun getVideoFormat(): MediaFormat? {

        for (i in 0 until mExtractor!!.trackCount) {
            val mediaFormat = mExtractor!!.getTrackFormat(i)
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("video/")) {
                mVideoTrack = i
                break
            }
        }
        return if (mVideoTrack >= 0) mExtractor!!.getTrackFormat(mVideoTrack) else null
    }


    fun getAudioFormat(): MediaFormat? {

        for (i in 0 until mExtractor!!.trackCount) {
            val mediaFormat = mExtractor!!.getTrackFormat(i)
            val mime = mediaFormat.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("audio/")) {
                mAudioTrack = -1
                break
            }
        }

        return if (mAudioTrack >= 0) mExtractor!!.getTrackFormat(mAudioTrack) else null

    }


    fun readBuffer(byteBuffer: ByteBuffer) {
        byteBuffer.clear()
        selectSourceTrack()

    }

    /**
     * 选择通道
     */
    private fun selectSourceTrack() {
        if (mVideoTrack >= 0) {
            mExtractor!!.selectTrack(mVideoTrack)
        } else if (mAudioTrack > 0) {
            mExtractor!!.selectTrack(mAudioTrack)
        }
    }


}