package me.blog.korn123.easydiary.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.github.chrisbanes.photoview.PhotoView
import io.github.aafactory.commons.utils.CommonUtils
import kotlinx.android.synthetic.main.activity_photo_view_pager.*
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.helper.DIARY_POSTCARD_DIRECTORY
import me.blog.korn123.easydiary.helper.POSTCARD_SEQUENCE
import java.io.File

/**
 * Created by hanjoong on 2017-06-08.
 */

class PostcardViewPagerActivity : EasyDiaryActivity() {
    private var mPostcardCount: Int = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view_pager)

        val intent = intent
        val sequence = intent.getIntExtra(POSTCARD_SEQUENCE, 0)

        val listPostcard = File(Environment.getExternalStorageDirectory().absolutePath + DIARY_POSTCARD_DIRECTORY)
                .listFiles()
                .filter { it.extension.equals("jpg", true)}
                .sortedDescending()
        mPostcardCount = listPostcard.size
        pageInfo.text = "1 / $mPostcardCount"

        view_pager.adapter = PostcardPagerAdapter(listPostcard)
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                pageInfo.text = "${position + 1} / $mPostcardCount"
            }

            override fun onPageScrollStateChanged(state: Int) {}
        })

//        val closeIcon = ContextCompat.getDrawable(this, R.drawable.x_mark_3)
//        closeIcon?.let {
//            it.setColorFilter(this.config.primaryColor, PorterDuff.Mode.SRC_IN)
//            close.setImageDrawable(closeIcon)   
//        }

        view_pager.setCurrentItem(sequence, false)
        close.setOnClickListener { finish() }
    }

    internal class PostcardPagerAdapter(var listPostcard: List<File>) : PagerAdapter() {
        override fun getCount(): Int {
            return listPostcard.size
        }

        override fun instantiateItem(container: ViewGroup, position: Int): View {
            val photoView = PhotoView(container.context)
            val bitmap = BitmapFactory.decodeFile(listPostcard[position].path)
            when (bitmap == null) {
                true -> {
                    val textView = TextView(container.context)
                    textView.gravity = Gravity.CENTER
                    val padding = CommonUtils.dpToPixel(container.context, 10, 1)
                    textView.setPadding(padding, padding, padding, padding)
                    FontUtils.setTypefaceDefault(textView)
                    textView.text = container.context.getString(R.string.photo_view_error_info)
                    container.addView(textView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    return textView    
                }
                false -> {
                    // Now just add PhotoView to ViewPager and return it
                    photoView.setImageBitmap(bitmap)
                    container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    return photoView    
                }
            }
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }
    }
}
