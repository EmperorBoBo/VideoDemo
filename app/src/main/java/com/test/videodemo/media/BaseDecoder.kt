package com.test.videodemo.media

import android.annotation.SuppressLint
import android.media.MediaCodec
import android.media.MediaFormat
import java.io.File
import java.nio.ByteBuffer

/**
 *  Author:   RandBII
 *  DATE:   2020/1/6
 *  Description: 解码器的基类
 */


abstract class BaseDecoder(private val mFilePath: String) : IDecoder {
    //正在运行
    private var mIsRunning = false
    // 线程锁
    private val mLock = Object()

    private var mReadyForDecoder = false

    /**
     * 音视频解码器
     */
    protected var mCodec: MediaCodec? = null

    /**
     * 音视频读取器
     */
    protected var mExtractor: IExtractor? = null

    /**
     * 解码输入缓存区
     */
    protected var mInputBuffers: Array<ByteBuffer>? = null

    protected var mOutputBuffers: Array<ByteBuffer>? = null

    private var mBufferInfo = MediaCodec.BufferInfo()

    private var mState = DecodeState.STOP

    private var mDecoderStateListener: IDecoderStateListener? = null

    private var mIsEOS = false

    private var mVideoWidth = 0
    private var mVideoHeight = 0
    private var mStartPos = 0
    private var mEndPos: Long = 0
    private var mDuration: Long = 0


    /**
     * 开始音视频界面时间， 用于音视频同步
     */
    private var mStartTimeForSync = -1L

    /**
     * 是否需要音视频同步
     */
    private var mSyncRender = true


    final override fun run() {

        if (mState == DecodeState.STOP) {
            mState = DecodeState.START
        }
        mDecoderStateListener?.decoderPrepare(this)

        //初始化并且启动器解码
        if (!init()) return

        try {


            while (mIsRunning) {

                if (mState != DecodeState.START && mState != DecodeState.DECODING && mState != DecodeState.SEEKING) {
                    waitDecode()
                    mStartTimeForSync = System.currentTimeMillis() - getCurrentTimeStamp()
                }

                if (!mIsRunning || mState == DecodeState.STOP) {
                    mIsRunning = false
                    break
                }

                if (mStartTimeForSync == -1L) {
                    mStartTimeForSync = System.currentTimeMillis()
                }

                // 如果数据没有解码完毕，继续解码
                if (!mIsEOS) {
                    mIsEOS = pushBufferToDecoder()
                }

                val index = pullBufferFromDecoder()

                if (index >= 0) {
                    if (mSyncRender && mState == DecodeState.DECODING) {
                        sleepRender()
                    }
                    if (mSyncRender) {
                        render(mOutputBuffers!![index], mBufferInfo)
                    }
                    val frame = Frame()
                    frame.buffer = mOutputBuffers!![index]
                    frame.setBufferInfo(mBufferInfo)
                    mDecoderStateListener?.decodeOnFrame(this, frame)
                    //释放输出缓冲
                    mCodec!!.releaseOutputBuffer(index, true)
                    if (mState == DecodeState.START) {
                        mState = DecodeState.PAUSE
                    }
                }

                if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_END_OF_STREAM) {
                    mState = DecodeState.FINISH
                    mDecoderStateListener?.decoderFinish(this)
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            doneDecode()
            release()
        }

    }

    private fun release() {

        try {
            mState = DecodeState.STOP
            mIsEOS = false
            mExtractor?.stop()
            mCodec?.release()
            mDecoderStateListener?.decoderDestroy(this)
        } catch (e: Exception) {

        }

    }

    abstract fun doneDecode()

    private fun render(byteBuffer: ByteBuffer, mBufferInfo: MediaCodec.BufferInfo) {

    }

    private fun sleepRender() {

        val passTime = System.currentTimeMillis() - mStartTimeForSync
        val curTime = getCurrentTimeStamp()
        if (curTime > passTime) {
            Thread.sleep(curTime - passTime)
        }

    }

    @SuppressLint("SwitchIntDef")
    private fun pullBufferFromDecoder(): Int {

        val index = mCodec!!.dequeueOutputBuffer(mBufferInfo, 1000)

        when (index) {
            MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {

            }
            MediaCodec.INFO_TRY_AGAIN_LATER -> {

            }
            MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED -> {
                mOutputBuffers = mCodec!!.outputBuffers
            }
            else -> {
                return index
            }
        }
        return -1
    }


    private fun pushBufferToDecoder(): Boolean {

        var inputBufferIndex = mCodec!!.dequeueInputBuffer(1000)
        var isEndOfStream = false
        if (inputBufferIndex >= 0) {

            val inputBuffer = mInputBuffers!![inputBufferIndex]
            val sampleSize = mExtractor!!.readBuffer(inputBuffer)
            if (sampleSize < 0) {  // 数据已经取完，压入数据结束标志
                mCodec!!.queueInputBuffer(
                    inputBufferIndex,
                    0,
                    0,
                    0,
                    MediaCodec.BUFFER_FLAG_END_OF_STREAM
                )
                isEndOfStream = true
            }
        }

        return isEndOfStream

    }


    private fun getCurrentTimeStamp(): Long {
        return mBufferInfo.presentationTimeUs

    }

    abstract fun waitDecode()

    private fun init(): Boolean {

        if (mFilePath.isEmpty() || !File(mFilePath).exists()) {
            return false
        }
        if (!check()) return false
        mExtractor = initExtractor(mFilePath)
        if (mExtractor == null || mExtractor!!.getFormat() == null) return false
        if (!initParams()) return false
        if (!initRender()) return false
        if (!initCodec()) return false
        return true

    }

    private fun initCodec(): Boolean {

        try {
            val type = mExtractor!!.getFormat()!!.getString(MediaFormat.KEY_MIME)
            mCodec = MediaCodec.createDecoderByType(type)
            if (!configCodec(mCodec!!, mExtractor!!.getFormat()!!)) {
                waitDecode()
            }
            mCodec!!.start()
            mInputBuffers = mCodec?.inputBuffers
            mOutputBuffers = mCodec?.outputBuffers
        } catch (e: Exception) {
            return false
        }
        return true
    }

    /**
     * 配置解码
     */
    abstract fun configCodec(mCodec: MediaCodec, format: MediaFormat): Boolean

    abstract fun initRender(): Boolean

    private fun initParams(): Boolean {

        try {
            val format = mExtractor!!.getFormat()!!
            mDuration = format.getLong(MediaFormat.KEY_DURATION) / 1000
            if (mEndPos == 0L) mEndPos = mDuration
            initSpecParams(mExtractor!!.getFormat()!!)
        } catch (e: Exception) {
            return false
        }
        return false

    }

    /**
     * 初始化自己特有参数
     */
    abstract fun initSpecParams(format: MediaFormat)

    /**
     * 初始化数据提取器
     */
    abstract fun initExtractor(mFilePath: String): IExtractor?


    /**
     * 检查子类参数
     */
    abstract fun check(): Boolean

}