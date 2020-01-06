package com.test.videodemo.media

import android.media.MediaFormat

/**
 *  Author:   RandBII
 *  DATE:   2020/1/6
 *  Description: 界面器流程 （音视频公用一个基类解码器）
 */
interface IDecoder : Runnable {

    /**
     * 暂停解码
     */
    fun pause()

    /**
     * 继续解码
     */
    fun goOn()

    /**
     * 停止解码
     */
    fun stop()

    /**
     * 正在解码
     */
    fun isDecoding(): Boolean

    /**
     * 正在快进
     */
    fun isSeeking(): Boolean

    fun isStop(): Boolean

    /**
     * 设置状态监听
     */
    fun setStateListener(listener: IDecoderStateListener?)

    /**
     * 获取视频宽度
     */
    fun getWidth() : Int

    /**
     * 获取视频高度
     */
    fun getHeight():Int

    /**
     * 获取视频
     */
    fun getDuration():Long

    /**
     * 获取视频旋转角度
     */
    fun getRotationAngle():Int

    /**
     * 获取媒体格式
     */
    fun getMediaFormat() : MediaFormat?

    /**
     * 获取媒体的轨道
     */
    fun getMediaTrack():Int


    /**
     * 获取解码文件路径
     */
    fun getFilePath():String



}