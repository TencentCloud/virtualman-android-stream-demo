package com.tencent.virtualman_demo_app.view.shadow

import android.content.Context
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import android.os.Build
import android.graphics.drawable.BitmapDrawable
import android.graphics.*
import com.tencent.virtualman_demo_app.R
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.tencent.virtualman_demo_app.view.shadow.GlideRoundUtils
import java.lang.IllegalArgumentException
import java.lang.UnsupportedOperationException

/**
 * 阴影控件
 */
class ShadowLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var clickAbleFalseDrawable: Drawable? = null
    private var clickAbleFalseColor = -101
    private var layoutBackground: Drawable? = null
    private var layoutBackground_true: Drawable? = null
    private var firstView: View? = null
    private var mBackGroundColor = 0
    private var mBackGroundColor_true = -101
    private var mShadowColor = 0
    private var mShadowLimit = 0f
    private var mCornerRadius = 0f
    private var mDx = 0f
    private var mDy = 0f
    private var leftShow = false
    private var rightShow = false
    private var topShow = false
    private var bottomShow = false
    private var shadowPaint: Paint? = null
    private var paint: Paint? = null
    private var leftPadding = 0
    private var topPadding = 0
    private var rightPadding = 0
    private var bottomPadding = 0

    //阴影布局子空间区域
    private val rectf = RectF()

    //ShadowLayout的样式，是只需要pressed还是selected。默认是pressed.
    private var selectorType = 1
    private var isShowShadow = true
    private var isSym = false

    //增加各个圆角的属性
    private var mCornerRadius_leftTop = 0f
    private var mCornerRadius_rightTop = 0f
    private var mCornerRadius_leftBottom = 0f
    private var mCornerRadius_rightBottom = 0f

    //边框画笔
    private var paint_stroke: Paint? = null
    private var stroke_with = 0f
    private var stroke_color = 0
    private var stroke_color_true = 0
    private var mIsClickable: Boolean = false
    override fun setClickable(clickable: Boolean) {
        super.setClickable(clickable)
        mIsClickable = clickable
        changeSwitchClickable()
        if (mIsClickable) {
            super.setOnClickListener(onClickListener)
        }
    }

    //解决xml设置clickable = false时。代码设置true时，点击事件无效的bug
    private var onClickListener: OnClickListener? = null
    override fun setOnClickListener(l: OnClickListener?) {
        onClickListener = l
        if (mIsClickable) {
            super.setOnClickListener(l)
        }
    }

    fun changeSwitchClickable() {
        //不可点击的状态只在press mode的模式下生效
        if (selectorType == 1 && firstView != null) {

            //press mode
            if (!mIsClickable) {
                //不可点击的状态。
                if (clickAbleFalseColor != -101) {
                    //说明设置了颜色
                    if (layoutBackground != null) {
                        //说明此时是设置了图片的模式
                        firstView!!.background.alpha = 0
                    }
                    paint!!.color = clickAbleFalseColor
                    postInvalidate()
                } else if (clickAbleFalseDrawable != null) {
                    //说明设置了背景图
                    setmBackGround(clickAbleFalseDrawable)
                    paint!!.color = Color.parseColor("#00000000")
                    postInvalidate()
                }
            } else {
                //可点击的状态
                if (layoutBackground != null) {
                    setmBackGround(layoutBackground)
                } else {
                    if (firstView!!.background != null) {
                        firstView!!.background.alpha = 0
                    }
                }
                paint!!.color = mBackGroundColor
                postInvalidate()
            }
        }
    }

    //增加selector样式
    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        if (selectorType == 2) {
            if (selected) {
                if (mBackGroundColor_true != -101) {
                    paint!!.color = mBackGroundColor_true
                }
                if (stroke_color_true != -101) {
                    paint_stroke!!.color = stroke_color_true
                }
                if (layoutBackground_true != null) {
                    setmBackGround(layoutBackground_true)
                }
            } else {
                paint!!.color = mBackGroundColor
                if (stroke_color != -101) {
                    paint_stroke!!.color = stroke_color
                }
                if (layoutBackground != null) {
                    setmBackGround(layoutBackground)
                }
            }
            postInvalidate()
        }
    }

    fun setShowShadow(isShowShadow: Boolean) {
        this.isShowShadow = isShowShadow
        if (width != 0 && height != 0) {
            setBackgroundCompat(width, height)
        }
    }

    //动态设置x轴偏移量
    fun setMDx(mDx: Float) {
        if (Math.abs(mDx) > mShadowLimit) {
            if (mDx > 0) {
                this.mDx = mShadowLimit
            } else {
                this.mDx = -mShadowLimit
            }
        } else {
            this.mDx = mDx
        }
        setPadding()
    }

    //动态设置y轴偏移量
    fun setMDy(mDy: Float) {
        if (Math.abs(mDy) > mShadowLimit) {
            if (mDy > 0) {
                this.mDy = mShadowLimit
            } else {
                this.mDy = -mShadowLimit
            }
        } else {
            this.mDy = mDy
        }
        setPadding()
    }

    fun getmCornerRadius(): Float {
        return mCornerRadius
    }

    //动态设置 圆角属性
    fun setmCornerRadius(mCornerRadius: Int) {
        this.mCornerRadius = mCornerRadius.toFloat()
        if (width != 0 && height != 0) {
            setBackgroundCompat(width, height)
        }
    }

    fun getmShadowLimit(): Float {
        return mShadowLimit
    }

    //动态设置阴影扩散区域
    fun setmShadowLimit(mShadowLimit: Int) {
        this.mShadowLimit = mShadowLimit.toFloat()
        setPadding()
    }

    //动态设置阴影颜色值
    fun setmShadowColor(mShadowColor: Int) {
        this.mShadowColor = mShadowColor
        if (width != 0 && height != 0) {
            setBackgroundCompat(width, height)
        }
    }

    fun setLeftShow(leftShow: Boolean) {
        this.leftShow = leftShow
        setPadding()
    }

    fun setRightShow(rightShow: Boolean) {
        this.rightShow = rightShow
        setPadding()
    }

    fun setTopShow(topShow: Boolean) {
        this.topShow = topShow
        setPadding()
    }

    fun setBottomShow(bottomShow: Boolean) {
        this.bottomShow = bottomShow
        setPadding()
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun onFinishInflate() {
        super.onFinishInflate()
        firstView = getChildAt(0)
        if (firstView == null) {
            firstView = this@ShadowLayout
            //当子View都没有的时候。默认不使用阴影
            isShowShadow = false
        }
        if (firstView != null) {

            //selector样式不受clickable的影响
            if (selectorType == 2) {
                //如果是selector的模式下
                if (this.isSelected) {
                    //这个方法内已经判断了是否为空
                    setmBackGround(layoutBackground_true)
                } else {
                    setmBackGround(layoutBackground)
                }
            } else {
                if (mIsClickable) {
                    setmBackGround(layoutBackground)
                } else {
                    setmBackGround(clickAbleFalseDrawable)
                    if (clickAbleFalseColor != -101) {
                        paint!!.color = clickAbleFalseColor
                    }
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            setBackgroundCompat(w, h)
        }
    }

    private fun initView(context: Context, attrs: AttributeSet?) {
        initAttributes(attrs)
        shadowPaint = Paint()
        shadowPaint!!.isAntiAlias = true
        shadowPaint!!.style = Paint.Style.FILL
        paint_stroke = Paint()
        paint_stroke!!.isAntiAlias = true
        paint_stroke!!.style = Paint.Style.STROKE
        paint_stroke!!.strokeWidth = stroke_with
        if (stroke_color != -101) {
            paint_stroke!!.color = stroke_color
        }


        //矩形画笔
        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint!!.style = Paint.Style.FILL
        paint!!.color = mBackGroundColor
        setPadding()
    }

    fun dip2px(dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    fun setPadding() {
        if (isShowShadow && mShadowLimit > 0) {
            //控件区域是否对称，默认是对称。不对称的话，那么控件区域随着阴影区域走
            if (isSym) {
                val xPadding = (mShadowLimit + Math.abs(mDx)).toInt()
                val yPadding = (mShadowLimit + Math.abs(mDy)).toInt()
                leftPadding = if (leftShow) {
                    xPadding
                } else {
                    0
                }
                topPadding = if (topShow) {
                    yPadding
                } else {
                    0
                }
                rightPadding = if (rightShow) {
                    xPadding
                } else {
                    0
                }
                bottomPadding = if (bottomShow) {
                    yPadding
                } else {
                    0
                }
            } else {
                if (Math.abs(mDy) > mShadowLimit) {
                    mDy = if (mDy > 0) {
                        mShadowLimit
                    } else {
                        0 - mShadowLimit
                    }
                }
                if (Math.abs(mDx) > mShadowLimit) {
                    mDx = if (mDx > 0) {
                        mShadowLimit
                    } else {
                        0 - mShadowLimit
                    }
                }
                topPadding = if (topShow) {
                    (mShadowLimit - mDy).toInt()
                } else {
                    0
                }
                bottomPadding = if (bottomShow) {
                    (mShadowLimit + mDy).toInt()
                } else {
                    0
                }
                rightPadding = if (rightShow) {
                    (mShadowLimit - mDx).toInt()
                } else {
                    0
                }
                leftPadding = if (leftShow) {
                    (mShadowLimit + mDx).toInt()
                } else {
                    0
                }
            }
            setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
        }
    }

    private fun setBackgroundCompat(w: Int, h: Int) {
        if (isShowShadow) {
            //判断传入的颜色值是否有透明度
            isAddAlpha(mShadowColor)
            val bitmap = createShadowBitmap(
                w,
                h,
                mCornerRadius,
                mShadowLimit,
                mDx,
                mDy,
                mShadowColor,
                Color.TRANSPARENT
            )
            val drawable = BitmapDrawable(bitmap)
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                setBackgroundDrawable(drawable)
            } else {
                background = drawable
            }
        } else {
            if (getChildAt(0) == null) {
                if (layoutBackground != null) {
                    firstView = this@ShadowLayout
                    if (mIsClickable) {
                        setmBackGround(layoutBackground)
                    } else {
                        changeSwitchClickable()
                    }
                } else {
                    //解决不执行onDraw方法的bug就是给其设置一个透明色
                    setBackgroundColor(Color.parseColor("#00000000"))
                }
            } else {
                setBackgroundColor(Color.parseColor("#00000000"))
            }
        }
    }

    private fun initAttributes(attrs: AttributeSet?) {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.ShadowLayout) ?: return
        try {
            //默认是显示
            isShowShadow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHidden, false)
            leftShow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHiddenLeft, false)
            rightShow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHiddenRight, false)
            bottomShow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHiddenBottom, false)
            topShow = !attr.getBoolean(R.styleable.ShadowLayout_hl_shadowHiddenTop, false)
            mCornerRadius = attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius, 0f)
            mCornerRadius_leftTop =
                attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius_leftTop, -1f)
            mCornerRadius_leftBottom =
                attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius_leftBottom, -1f)
            mCornerRadius_rightTop =
                attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius_rightTop, -1f)
            mCornerRadius_rightBottom =
                attr.getDimension(R.styleable.ShadowLayout_hl_cornerRadius_rightBottom, -1f)

            //默认扩散区域宽度
            mShadowLimit = attr.getDimension(R.styleable.ShadowLayout_hl_shadowLimit, 0f)
            if (mShadowLimit == 0f) {
                //如果阴影没有设置阴影扩散区域，那么默认隐藏阴影
                isShowShadow = false
            }

            //x轴偏移量
            mDx = attr.getDimension(R.styleable.ShadowLayout_hl_shadowOffsetX, 0f)
            //y轴偏移量
            mDy = attr.getDimension(R.styleable.ShadowLayout_hl_shadowOffsetY, 0f)
            mShadowColor = attr.getColor(
                R.styleable.ShadowLayout_hl_shadowColor,
                resources.getColor(R.color.black_a20)
            )
            selectorType = attr.getInt(R.styleable.ShadowLayout_hl_shapeMode, 1)
            isSym = attr.getBoolean(R.styleable.ShadowLayout_hl_shadowSymmetry, true)

            //背景颜色的点击(默认颜色为白色)
            mBackGroundColor = resources.getColor(R.color.white)
            val background = attr.getDrawable(R.styleable.ShadowLayout_hl_layoutBackground)
            if (background != null) {
                if (background is ColorDrawable) {
                    mBackGroundColor = background.color
                } else {
                    layoutBackground = background
                }
            }
            val trueBackground = attr.getDrawable(R.styleable.ShadowLayout_hl_layoutBackground_true)
            if (trueBackground != null) {
                if (trueBackground is ColorDrawable) {
                    mBackGroundColor_true = trueBackground.color
                } else {
                    layoutBackground_true = trueBackground
                }
            }
            if (mBackGroundColor_true != -101 && layoutBackground != null) {
                throw UnsupportedOperationException("使用了ShadowLayout_hl_layoutBackground_true属性，必须先设置ShadowLayout_hl_layoutBackground属性。且设置颜色时，必须保持都为颜色")
            }
            if (layoutBackground == null && layoutBackground_true != null) {
                throw UnsupportedOperationException("使用了ShadowLayout_hl_layoutBackground_true属性，必须先设置ShadowLayout_hl_layoutBackground属性。且设置图片时，必须保持都为图片")
            }

            //边框颜色的点击
            stroke_color = attr.getColor(R.styleable.ShadowLayout_hl_strokeColor, -101)
            stroke_color_true = attr.getColor(R.styleable.ShadowLayout_hl_strokeColor_true, -101)
            if (stroke_color == -101 && stroke_color_true != -101) {
                throw UnsupportedOperationException("使用了ShadowLayout_hl_strokeColor_true属性，必须先设置ShadowLayout_hl_strokeColor属性")
            }
            stroke_with =
                attr.getDimension(R.styleable.ShadowLayout_hl_strokeWith, dip2px(1f).toFloat())
            //规定边框长度最大不错过7dp
            if (stroke_with > dip2px(7f)) {
                stroke_with = dip2px(5f).toFloat()
            }
            val clickAbleFalseBackground =
                attr.getDrawable(R.styleable.ShadowLayout_hl_layoutBackground_clickFalse)
            if (clickAbleFalseBackground != null) {
                if (clickAbleFalseBackground is ColorDrawable) {
                    clickAbleFalseColor = clickAbleFalseBackground.color
                } else {
                    clickAbleFalseDrawable = clickAbleFalseBackground
                }
            }
            mIsClickable = attr.getBoolean(R.styleable.ShadowLayout_clickable, true)
            setClickable(mIsClickable)
        } finally {
            attr.recycle()
        }
    }

    private fun createShadowBitmap(
        shadowWidth: Int, shadowHeight: Int, cornerRadius: Float, shadowRadius: Float,
        dx: Float, dy: Float, shadowColor: Int, fillColor: Int
    ): Bitmap {
        //优化阴影bitmap大小,将尺寸缩小至原来的1/4。
        var shadowWidth = shadowWidth
        var shadowHeight = shadowHeight
        var cornerRadius = cornerRadius
        var shadowRadius = shadowRadius
        var dx = dx
        var dy = dy
        dx = dx / 4
        dy = dy / 4
        shadowWidth = shadowWidth / 4
        shadowHeight = shadowHeight / 4
        cornerRadius = cornerRadius / 4
        shadowRadius = shadowRadius / 4
        val output = Bitmap.createBitmap(shadowWidth, shadowHeight, Bitmap.Config.ARGB_4444)
        val canvas = Canvas(output)

        //这里缩小limit的是因为，setShadowLayer后会将bitmap扩散到shadowWidth，shadowHeight
        val shadowRect = RectF(
            shadowRadius,
            shadowRadius,
            shadowWidth - shadowRadius,
            shadowHeight - shadowRadius
        )
        if (isSym) {
            if (dy > 0) {
                shadowRect.top += dy
                shadowRect.bottom -= dy
            } else if (dy < 0) {
                shadowRect.top += Math.abs(dy)
                shadowRect.bottom -= Math.abs(dy)
            }
            if (dx > 0) {
                shadowRect.left += dx
                shadowRect.right -= dx
            } else if (dx < 0) {
                shadowRect.left += Math.abs(dx)
                shadowRect.right -= Math.abs(dx)
            }
        } else {
            shadowRect.top -= dy
            shadowRect.bottom -= dy
            shadowRect.right -= dx
            shadowRect.left -= dx
        }
        shadowPaint!!.color = fillColor
        if (!isInEditMode) { //dx  dy
            shadowPaint!!.setShadowLayer(shadowRadius, dx, dy, shadowColor)
        }
        if (mCornerRadius_leftBottom == -1f && mCornerRadius_leftTop == -1f && mCornerRadius_rightTop == -1f && mCornerRadius_rightBottom == -1f) {
            //如果没有设置整个属性，那么按原始去画
            canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint!!)
        } else {
            //目前最佳的解决方案
            rectf.left = leftPadding.toFloat()
            rectf.top = topPadding.toFloat()
            rectf.right = (width - rightPadding).toFloat()
            rectf.bottom = (height - bottomPadding).toFloat()
            shadowPaint!!.isAntiAlias = true
            val leftTop: Int
            leftTop = if (mCornerRadius_leftTop == -1f) {
                mCornerRadius.toInt() / 4
            } else {
                mCornerRadius_leftTop.toInt() / 4
            }
            val leftBottom: Int
            leftBottom = if (mCornerRadius_leftBottom == -1f) {
                mCornerRadius.toInt() / 4
            } else {
                mCornerRadius_leftBottom.toInt() / 4
            }
            val rightTop: Int
            rightTop = if (mCornerRadius_rightTop == -1f) {
                mCornerRadius.toInt() / 4
            } else {
                mCornerRadius_rightTop.toInt() / 4
            }
            val rightBottom: Int
            rightBottom = if (mCornerRadius_rightBottom == -1f) {
                mCornerRadius.toInt() / 4
            } else {
                mCornerRadius_rightBottom.toInt() / 4
            }
            val outerR = floatArrayOf(
                leftTop.toFloat(),
                leftTop.toFloat(),
                rightTop.toFloat(),
                rightTop.toFloat(),
                rightBottom.toFloat(),
                rightBottom.toFloat(),
                leftBottom.toFloat(),
                leftBottom.toFloat()
            ) //左上，右上，右下，左下
            val path = Path()
            path.addRoundRect(shadowRect, outerR, Path.Direction.CW)
            canvas.drawPath(path, shadowPaint!!)
        }
        return output
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        rectf.left = leftPadding.toFloat()
        rectf.top = topPadding.toFloat()
        rectf.right = (width - rightPadding).toFloat()
        rectf.bottom = (height - bottomPadding).toFloat()
        val trueHeight = (rectf.bottom - rectf.top).toInt()
        //如果都为0说明，没有设置特定角，那么按正常绘制
        if (getChildAt(0) != null) {
            if (mCornerRadius_leftTop == -1f && mCornerRadius_leftBottom == -1f && mCornerRadius_rightTop == -1f && mCornerRadius_rightBottom == -1f) {
                if (mCornerRadius > trueHeight / 2) {


                    //画圆角矩形
                    canvas.drawRoundRect(
                        rectf,
                        (trueHeight / 2).toFloat(),
                        (trueHeight / 2).toFloat(),
                        paint!!
                    )
                    if (stroke_color != -101) {
                        val rectFStroke = RectF(
                            rectf.left + stroke_with / 2,
                            rectf.top + stroke_with / 2,
                            rectf.right - stroke_with / 2,
                            rectf.bottom - stroke_with / 2
                        )
                        canvas.drawRoundRect(
                            rectFStroke,
                            (trueHeight / 2).toFloat(),
                            (trueHeight / 2).toFloat(),
                            paint_stroke!!
                        )
                    }
                } else {
                    canvas.drawRoundRect(rectf, mCornerRadius, mCornerRadius, paint!!)
                    if (stroke_color != -101) {
                        val rectFStroke = RectF(
                            rectf.left + stroke_with / 2,
                            rectf.top + stroke_with / 2,
                            rectf.right - stroke_with / 2,
                            rectf.bottom - stroke_with / 2
                        )
                        canvas.drawRoundRect(
                            rectFStroke,
                            mCornerRadius,
                            mCornerRadius,
                            paint_stroke!!
                        )
                    }
                }
            } else {
                setSpaceCorner(canvas, trueHeight)
            }
        }
    }

    //这是自定义四个角的方法。
    private fun setSpaceCorner(canvas: Canvas, trueHeight: Int) {
        var leftTop: Int
        var rightTop: Int
        var rightBottom: Int
        var leftBottom: Int
        leftTop = if (mCornerRadius_leftTop == -1f) {
            mCornerRadius.toInt()
        } else {
            mCornerRadius_leftTop.toInt()
        }
        if (leftTop > trueHeight / 2) {
            leftTop = trueHeight / 2
        }
        rightTop = if (mCornerRadius_rightTop == -1f) {
            mCornerRadius.toInt()
        } else {
            mCornerRadius_rightTop.toInt()
        }
        if (rightTop > trueHeight / 2) {
            rightTop = trueHeight / 2
        }
        rightBottom = if (mCornerRadius_rightBottom == -1f) {
            mCornerRadius.toInt()
        } else {
            mCornerRadius_rightBottom.toInt()
        }
        if (rightBottom > trueHeight / 2) {
            rightBottom = trueHeight / 2
        }
        leftBottom = if (mCornerRadius_leftBottom == -1f) {
            mCornerRadius.toInt()
        } else {
            mCornerRadius_leftBottom.toInt()
        }
        if (leftBottom > trueHeight / 2) {
            leftBottom = trueHeight / 2
        }
        val outerR = floatArrayOf(
            leftTop.toFloat(),
            leftTop.toFloat(),
            rightTop.toFloat(),
            rightTop.toFloat(),
            rightBottom.toFloat(),
            rightBottom.toFloat(),
            leftBottom.toFloat(),
            leftBottom.toFloat()
        ) //左上，右上，右下，左下
        if (stroke_color != -101) {
            val mDrawables = ShapeDrawable(RoundRectShape(outerR, null, null))
            mDrawables.paint.color = paint!!.color
            //            mDrawables.setBounds((int) (leftPadding + stroke_with), (int) (topPadding + stroke_with), (int) (getWidth() - rightPadding - stroke_with), (int) (getHeight() - bottomPadding - stroke_with));
            mDrawables.setBounds(
                leftPadding,
                topPadding,
                width - rightPadding,
                height - bottomPadding
            )
            mDrawables.draw(canvas)
            val mDrawablesStroke = ShapeDrawable(RoundRectShape(outerR, null, null))
            mDrawablesStroke.paint.color = paint_stroke!!.color
            mDrawablesStroke.paint.style = Paint.Style.STROKE
            mDrawablesStroke.paint.strokeWidth = stroke_with
            //            mDrawablesStroke.setBounds(leftPadding, topPadding, getWidth() - rightPadding, getHeight() - bottomPadding);
            mDrawablesStroke.setBounds(
                (leftPadding + stroke_with / 2).toInt(),
                (topPadding + stroke_with / 2).toInt(),
                (width - rightPadding - stroke_with / 2).toInt(),
                (height - bottomPadding - stroke_with / 2).toInt()
            )
            mDrawablesStroke.draw(canvas)
        } else {
            val mDrawables = ShapeDrawable(RoundRectShape(outerR, null, null))
            mDrawables.paint.color = paint!!.color
            mDrawables.setBounds(
                leftPadding,
                topPadding,
                width - rightPadding,
                height - bottomPadding
            )
            mDrawables.draw(canvas)
        }
    }

    fun isAddAlpha(color: Int) {
        //获取单签颜色值的透明度，如果没有设置透明度，默认加上#2a
        if (Color.alpha(color) == 255) {
            var red = Integer.toHexString(Color.red(color))
            var green = Integer.toHexString(Color.green(color))
            var blue = Integer.toHexString(Color.blue(color))
            if (red.length == 1) {
                red = "0$red"
            }
            if (green.length == 1) {
                green = "0$green"
            }
            if (blue.length == 1) {
                blue = "0$blue"
            }
            val endColor = "#2a$red$green$blue"
            mShadowColor = convertToColorInt(endColor)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mBackGroundColor_true != -101 || stroke_color_true != -101 || layoutBackground_true != null) {
            if (mIsClickable) {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> if (selectorType == 1) {
                        if (mBackGroundColor_true != -101) {
                            paint!!.color = mBackGroundColor_true
                        }
                        if (stroke_color_true != -101) {
                            paint_stroke!!.color = stroke_color_true
                        }
                        if (layoutBackground_true != null) {
                            setmBackGround(layoutBackground_true)
                        }
                        postInvalidate()
                    }
                    MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> if (selectorType == 1) {
                        paint!!.color = mBackGroundColor
                        if (stroke_color != -101) {
                            paint_stroke!!.color = stroke_color
                        }
                        if (layoutBackground != null) {
                            setmBackGround(layoutBackground)
                        }
                        postInvalidate()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    fun setmBackGround(drawable: Drawable?) {
        if (firstView != null && drawable != null) {
            if (mCornerRadius_leftTop == -1f && mCornerRadius_leftBottom == -1f && mCornerRadius_rightTop == -1f && mCornerRadius_rightBottom == -1f) {
                GlideRoundUtils.setRoundCorner(firstView!!, drawable, mCornerRadius)
            } else {
                val leftTop: Int = if (mCornerRadius_leftTop == -1f) {
                    mCornerRadius.toInt()
                } else {
                    mCornerRadius_leftTop.toInt()
                }
                val leftBottom: Int = if (mCornerRadius_leftBottom == -1f) {
                    mCornerRadius.toInt()
                } else {
                    mCornerRadius_leftBottom.toInt()
                }
                val rightTop: Int = if (mCornerRadius_rightTop == -1f) {
                    mCornerRadius.toInt()
                } else {
                    mCornerRadius_rightTop.toInt()
                }
                val rightBottom: Int = if (mCornerRadius_rightBottom == -1f) {
                    mCornerRadius.toInt()
                } else {
                    mCornerRadius_rightBottom.toInt()
                }
                GlideRoundUtils.setCorners(
                    firstView!!,
                    drawable,
                    leftTop.toFloat(),
                    leftBottom.toFloat(),
                    rightTop.toFloat(),
                    rightBottom.toFloat()
                )
            }
        }
    }

    companion object {
        @Throws(IllegalArgumentException::class)
        fun convertToColorInt(argb: String): Int {
            var argb = argb
            if (!argb.startsWith("#")) {
                argb = "#$argb"
            }
            return Color.parseColor(argb)
        }
    }

    init {
        initView(context, attrs)
    }
}