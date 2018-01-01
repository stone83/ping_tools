package com.jj.game.boost.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.ImageView;

import com.ccmt.library.lru.LruMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/****
 * 此工具类源于stack over flow
 * 原文链接:http://stackoverflow.com/questions/8692328/causing-outofmemoryerror-in-frame-by-frame-animation-in-android
 * 主要使用了BitmapFactory.decodeByteArray方法通过底层C来绘制图片,有效防止OOM
 * 使用了第三方类库:org.apache.commons.io.IOUtils,将Inputstream转为byte字节数组
 * *******/
public class MyAnimationDrawable {

    private Handler mHandler = new Handler();
    private ArrayList<MyFrame> mMyFrames = new ArrayList<>();
    private boolean mIsFinished;

    private static class MyFrame {
        byte[] mBytes;
        int mDuration;
        Drawable mDrawable;
        boolean mIsReady;
        long mPlayTime;
    }

    interface OnDrawableLoadedListener {
        void onDrawableLoaded(List<MyFrame> myFrames);
    }

    public static MyAnimationDrawable getInstance() {
        LruMap lruMap = LruMap.getInstance();
        String name = MyAnimationDrawable.class.getName();
        MyAnimationDrawable myAnimationDrawable = (MyAnimationDrawable) lruMap.get(name);
        if (myAnimationDrawable == null) {
            myAnimationDrawable = new MyAnimationDrawable();
            lruMap.put(name, myAnimationDrawable);
        }
        return myAnimationDrawable;
    }

    /***
     * 性能更优
     * 在animation-list中设置时间
     * **/
    @SuppressWarnings("WeakerAccess")
    public void animateRawManuallyFromXML(int resourceId,
                                          final ImageView imageView, final Runnable onStart,
                                          final Runnable onComplete, boolean isRe) {
        loadRaw(resourceId, imageView.getContext(),
                myFrames -> {
                    if (onStart != null) {
                        onStart.run();
                    }
                    animateRawManually(resourceId, myFrames, imageView, onStart, onComplete, isRe);
                });
    }

    public void animateRawManuallyFromXML(int resourceId,
                                          final ImageView imageView, final Runnable onStart,
                                          final Runnable onComplete) {
        animateRawManuallyFromXML(resourceId, imageView, onStart, onComplete, false);
    }

    private void loadRaw(final int resourceId, final Context context,
                         final OnDrawableLoadedListener onDrawableLoadedListener) {
        loadFromXml(resourceId, context, onDrawableLoadedListener);
    }

    @SuppressWarnings({"StatementWithEmptyBody", "TryWithIdenticalCatches"})
    private void loadFromXml(final int resourceId,
                             final Context context,
                             final OnDrawableLoadedListener onDrawableLoadedListener) {
        if (mMyFrames.size() > 0) {
            if (onDrawableLoadedListener != null) {
                onDrawableLoadedListener.onDrawableLoaded(mMyFrames);
            }
            return;
        }
        mIsFinished = false;
        LogUtil.i("播放帧动画");
        ThreadManager.executeAsyncTask(() -> {
            XmlResourceParser parser = context.getResources().getXml(
                    resourceId);

            try {
                int eventType = parser.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_DOCUMENT) {

                    } else if (eventType == XmlPullParser.START_TAG) {

                        if (parser.getName().equals("item")) {
                            byte[] bytes = null;
                            int duration = 1000;

                            for (int i = 0; i < parser.getAttributeCount(); i++) {
                                if (parser.getAttributeName(i).equals(
                                        "drawable")) {
                                    int resId = Integer.parseInt(parser
                                            .getAttributeValue(i)
                                            .substring(1));
                                    bytes = IOUtil.toByteArray(context
                                            .getResources()
                                            .openRawResource(resId));
                                } else if (parser.getAttributeName(i)
                                        .equals("duration")) {
                                    duration = parser.getAttributeIntValue(
                                            i, 1000);
                                }
                            }

                            MyFrame myFrame = new MyFrame();
                            myFrame.mBytes = bytes;
                            myFrame.mDuration = duration;
                            mMyFrames.add(myFrame);
                        }
                    } else if (eventType == XmlPullParser.END_TAG) {

                    } else if (eventType == XmlPullParser.TEXT) {

                    }

                    eventType = parser.next();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

            // Run on UI Thread
            mHandler.post(() -> {
                if (onDrawableLoadedListener != null) {
                    onDrawableLoadedListener.onDrawableLoaded(mMyFrames);
                }
            });
        });
    }

    private void animateRawManually(int resourceId, List<MyFrame> myFrames,
                                    ImageView imageView, final Runnable onStart,
                                    Runnable onComplete, boolean isRe) {
        animateRawManually(resourceId, myFrames, imageView, onStart, onComplete, 0, isRe);
    }

    private void animateRawManually(int resourceId, final List<MyFrame> myFrames,
                                    final ImageView imageView, final Runnable onStart,
                                    final Runnable onComplete,
                                    final int frameNumber, boolean isRe) {
        final MyFrame thisFrame = myFrames.get(frameNumber);
        if (frameNumber == 0) {
            thisFrame.mDrawable = new BitmapDrawable(imageView.getContext()
                    .getResources(), BitmapFactory.decodeByteArray(
                    thisFrame.mBytes, 0, thisFrame.mBytes.length));
        } else {
            MyFrame previousFrame = myFrames.get(frameNumber - 1);
            ((BitmapDrawable) previousFrame.mDrawable).getBitmap().recycle();
            previousFrame.mDrawable = null;
            previousFrame.mIsReady = false;
        }

        imageView.setImageDrawable(thisFrame.mDrawable);

        thisFrame.mPlayTime = System.currentTimeMillis();

        // Load next frame
        CountDownLatch countDownLatch;
        int newFrameNumber = frameNumber + 1;
        if (newFrameNumber < myFrames.size()) {
            countDownLatch = new CountDownLatch(1);
            CountDownLatch countDownLatchTemp = countDownLatch;
            MyFrame nextFrame = myFrames.get(newFrameNumber);
            ThreadManager.executeAsyncTask(() -> {
                nextFrame.mDrawable = new BitmapDrawable(imageView
                        .getContext().getResources(),
                        BitmapFactory.decodeByteArray(nextFrame.mBytes, 0,
                                nextFrame.mBytes.length));
                if (nextFrame.mIsReady) {
                    // Animate next frame
                    mHandler.post(() -> animateRawManually(resourceId, myFrames, imageView, onStart, onComplete,
                            newFrameNumber, isRe));
                } else {
                    nextFrame.mIsReady = true;
                }
                countDownLatchTemp.countDown();
            });
            try {
                countDownLatchTemp.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        long dtime = System.currentTimeMillis() - thisFrame.mPlayTime;
        if (dtime < thisFrame.mDuration) {
            SystemClock.sleep(thisFrame.mDuration - dtime);
        }
        mHandler.post(new AnimationRunnable(resourceId, myFrames, imageView, onStart,
                onComplete, isRe, frameNumber, thisFrame));
    }

    private static class AnimationRunnable implements Runnable {
        private int mResourceId;
        private List<MyFrame> mMyFrames;
        private ImageView mImageView;
        private Runnable mOnStart;
        private Runnable mOnComplete;
        private boolean mIsRe;
        private int mFrameNumber;
        private MyFrame mMyFrame;

        AnimationRunnable(int resourceId, List<MyFrame> myFrames, ImageView imageView, Runnable onStart,
                          Runnable onComplete, boolean isRe, int frameNumber, MyFrame myFrame) {
            this.mResourceId = resourceId;
            this.mMyFrames = myFrames;
            this.mImageView = imageView;
            this.mOnStart = onStart;
            this.mOnComplete = onComplete;
            this.mIsRe = isRe;
            this.mFrameNumber = frameNumber;
            this.mMyFrame = myFrame;
        }

        @Override
        public void run() {
            // Make sure ImageView hasn't been changed to a different Image
            // in this time
            MyAnimationDrawable myAnimationDrawable = getInstance();
            if (myAnimationDrawable.mIsFinished) {
                myAnimationDrawable.mIsFinished = false;
                LogUtil.i("帧动画结束");
                MyFrame currentFrame = mMyFrames.get(mFrameNumber);
                ((BitmapDrawable) currentFrame.mDrawable).getBitmap().recycle();
                currentFrame.mDrawable = null;
                currentFrame.mIsReady = false;
                if (mOnComplete != null) {
                    mOnComplete.run();
                }
                mMyFrames.clear();
                return;
            }
            if (mMyFrames.size() == 0) {
                return;
            }
            if (mImageView.getDrawable() == mMyFrame.mDrawable) {
                if (mFrameNumber + 1 < mMyFrames.size()) {
                    MyFrame nextFrame = mMyFrames.get(mFrameNumber + 1);
                    if (nextFrame.mIsReady) {
                        // Animate next frame
                        myAnimationDrawable.animateRawManually(mResourceId, mMyFrames,
                                mImageView, mOnStart, mOnComplete,
                                mFrameNumber + 1, mIsRe);
                    } else {
                        nextFrame.mIsReady = true;
                    }
                } else {
                    // 播放最后1帧动画,播放完要回收资源.
                    MyFrame currentFrame = mMyFrames.get(mFrameNumber);
                    ((BitmapDrawable) currentFrame.mDrawable).getBitmap().recycle();
                    currentFrame.mDrawable = null;
                    currentFrame.mIsReady = false;

                    if (mIsRe) {
                        myAnimationDrawable.animateRawManuallyFromXML(mResourceId, mImageView,
                                mOnStart, mOnComplete, true);
                    } else {
                        LogUtil.i("帧动画结束");
                        if (mOnComplete != null) {
                            mOnComplete.run();
                        }
                        myAnimationDrawable.mMyFrames.clear();
                    }
                }
            }
            mImageView = null;
        }
    }

    /**
     * 第二种方法,代码中控制时间,但不精确.
     * duration = 1000;
     *
     * @param animationDrawableResourceId
     * @param imageView
     * @param onStart
     * @param onComplete
     * @param duration
     * @throws IOException
     * @throws XmlPullParserException
     */
    @SuppressWarnings({"JavaDoc", "unused", "StatementWithEmptyBody", "ConstantConditions"})
    public void animateManuallyFromRawResource(
            int animationDrawableResourceId, ImageView imageView,
            Runnable onStart, Runnable onComplete, int duration) throws IOException,
            XmlPullParserException {
        AnimationDrawable animationDrawable = new AnimationDrawable();

        XmlResourceParser parser = imageView.getContext().getResources()
                .getXml(animationDrawableResourceId);

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {

            } else if (eventType == XmlPullParser.START_TAG) {

                if (parser.getName().equals("item")) {
                    Drawable drawable = null;

                    for (int i = 0; i < parser.getAttributeCount(); i++) {
                        if (parser.getAttributeName(i).equals("drawable")) {
                            int resId = Integer.parseInt(parser
                                    .getAttributeValue(i).substring(1));
                            byte[] bytes = IOUtil.toByteArray(imageView
                                    .getContext().getResources()
                                    .openRawResource(resId));//IOUtils.readBytes
                            drawable = new BitmapDrawable(imageView
                                    .getContext().getResources(),
                                    BitmapFactory.decodeByteArray(bytes, 0,
                                            bytes.length));
                        } else if (parser.getAttributeName(i)
                                .equals("duration")) {
                            duration = parser.getAttributeIntValue(i, 66);
                        }
                    }

                    animationDrawable.addFrame(drawable, duration);
                }

            } else if (eventType == XmlPullParser.END_TAG) {

            } else if (eventType == XmlPullParser.TEXT) {

            }

            eventType = parser.next();
        }

        if (onStart != null) {
            onStart.run();
        }
        animateDrawableManually(animationDrawable, imageView, onComplete, 0);
    }

    private void animateDrawableManually(
            final AnimationDrawable animationDrawable,
            final ImageView imageView, final Runnable onComplete,
            final int frameNumber) {
        final Drawable frame = animationDrawable.getFrame(frameNumber);
        imageView.setImageDrawable(frame);
        new Handler().postDelayed(() -> {
            // Make sure ImageView hasn't been changed to a different Image
            // in this time
            if (imageView.getDrawable() == frame) {
                if (frameNumber + 1 < animationDrawable.getNumberOfFrames()) {
                    // Animate next frame
                    animateDrawableManually(animationDrawable, imageView,
                            onComplete, frameNumber + 1);
                } else {
                    // Animation complete
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        }, animationDrawable.getDuration(frameNumber));
    }

    @SuppressWarnings("WeakerAccess")
    public void release() {
//        getInstance().mHandler.post(() -> mMyFrames.clear());
//        Iterator<Runnable> ite = mAllRunnables.iterator();
//        Runnable next;
//        while (ite.hasNext()) {
//            next = ite.next();
//            mHandler.removeCallbacks(next);
//            ite.remove();
//        }
        mIsFinished = true;
    }

}