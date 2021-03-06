package me.blog.korn123.easydiary.adapters

import android.app.Activity
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.ColorUtils
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import io.github.aafactory.commons.extensions.updateAppViews
import io.github.aafactory.commons.extensions.updateTextColors
import io.github.aafactory.commons.utils.CommonUtils
import io.github.aafactory.commons.utils.DateUtils
import me.blog.korn123.commons.utils.EasyDiaryUtils
import me.blog.korn123.commons.utils.FontUtils
import me.blog.korn123.easydiary.R
import me.blog.korn123.easydiary.extensions.config
import me.blog.korn123.easydiary.extensions.initTextSize
import me.blog.korn123.easydiary.helper.EasyDiaryDbHelper
import me.blog.korn123.easydiary.helper.THUMBNAIL_BACKGROUND_ALPHA
import me.blog.korn123.easydiary.models.DiaryDto
import org.apache.commons.lang3.StringUtils
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions



/**
 * Created by CHO HANJOONG on 2017-03-16.
 */

class DiaryMainItemAdapter(
        private val activity: Activity,
        private val layoutResourceId: Int,
        private val list: List<DiaryDto>
) : ArrayAdapter<DiaryDto>(activity, layoutResourceId, list) {
    var currentQuery: String? = null

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var row = convertView
        val holder: ViewHolder
        if (row == null) {
            row = activity.layoutInflater.inflate(this.layoutResourceId, parent, false)
            holder = ViewHolder()
            holder.textView1 = row.findViewById(R.id.text1)
            holder.textView2 = row.findViewById(R.id.text2)
            holder.textView3 = row.findViewById(R.id.text3)
            holder.imageView = row.findViewById(R.id.weather)
            holder.item_holder = row.findViewById(R.id.item_holder)
            holder.photoContainer = row.findViewById(R.id.photoContainer)
            holder.photoViews = row.findViewById(R.id.photoViews)
            row.tag = holder
        } else {
            holder = row.tag as ViewHolder
        }

        val diaryDto = list[position]
        if (StringUtils.isEmpty(diaryDto.title)) {
            holder.textView1?.visibility = View.GONE
        } else {
            holder.textView1?.visibility = View.VISIBLE
        }
        holder.textView1?.text = diaryDto.title
        holder.textView2?.text = diaryDto.contents

        // highlight current query
        if (StringUtils.isNotEmpty(currentQuery)) {
            if (context.config.diarySearchQueryCaseSensitive) {
                EasyDiaryUtils.highlightString(holder.textView1, currentQuery)
                EasyDiaryUtils.highlightString(holder.textView2, currentQuery)
            } else {
                EasyDiaryUtils.highlightStringIgnoreCase(holder.textView1, currentQuery)
                EasyDiaryUtils.highlightStringIgnoreCase(holder.textView2, currentQuery)
            }

        }
        holder.textView3?.text = DateUtils.getFullPatternDateWithTime(diaryDto.currentTimeMillis)
        EasyDiaryUtils.initWeatherView(holder.imageView, diaryDto.weather)

        holder.item_holder?.let {
            context.updateTextColors(it, 0, 0)
            context.updateAppViews(it)
            context.initTextSize(it, context)
        }

        when (diaryDto.photoUris?.size ?: 0 > 0) {
            true -> {
//                holder.photoViews.visibility = View.GONE
                holder.photoContainer.visibility = View.VISIBLE
            }
            false -> holder.photoContainer.visibility = View.GONE
        }
        FontUtils.setFontsTypeface(context, context.assets, null, holder.item_holder)
        holder.photoViews.removeAllViews()
        if (diaryDto.photoUris?.size ?: 0 > 0) {
            val maxPhotos = CommonUtils.getDefaultDisplay(activity).x / CommonUtils.dpToPixel(activity, 40, 1)
            diaryDto.photoUris?.map {
                val path = it.getFilePath()
                val imageView = ImageView(activity)
                val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(activity, 28, 1), CommonUtils.dpToPixel(activity, 28, 1))
                layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(activity, 3, 1), 0)
                imageView.layoutParams = layoutParams
                val drawable = ContextCompat.getDrawable(activity, R.drawable.bg_card_thumbnail)
                val gradient = drawable as GradientDrawable
                gradient.setColor(ColorUtils.setAlphaComponent(activity.config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA))
                imageView.background = gradient
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                val padding = (CommonUtils.dpToPixel(activity, 1, 1) * 1.5).toInt()
                imageView.setPadding(padding, padding, padding, padding)
                val options = RequestOptions()
                        .centerCrop()
                        .error(R.drawable.question_shield)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .priority(Priority.HIGH)
                Glide.with(context).load(path).apply(options).into(imageView)
                if (holder.photoViews.childCount >= maxPhotos) return@map
                holder.photoViews.addView(imageView)
            }
        }
        
        return row
    }

    private class AttachPhotoLoader(val activity: Activity, val sequence: Int, val holder: ViewHolder) : Thread() {
        override fun run() {
            super.run()
            val photoViews = mutableListOf<ImageView>()
            val realmInstance = EasyDiaryDbHelper.getInstance()
            val diaryDto: DiaryDto = EasyDiaryDbHelper.readDiaryBy(realmInstance, sequence)
            if (diaryDto.photoUris?.size ?: 0 > 0) {
                val maxPhotos = CommonUtils.getDefaultDisplay(activity).x / CommonUtils.dpToPixel(activity, 40, 1)
                diaryDto.photoUris?.map {
                    val bitmap = EasyDiaryUtils.photoUriToDownSamplingBitmap(activity, it, 30, 25, 25)
                    val imageView = ImageView(activity)
                    val layoutParams = LinearLayout.LayoutParams(CommonUtils.dpToPixel(activity, 28, 1), CommonUtils.dpToPixel(activity, 28, 1))
                    layoutParams.setMargins(0, 0, CommonUtils.dpToPixel(activity, 3, 1), 0)
                    imageView.layoutParams = layoutParams
//                        imageView.setBackgroundResource(R.drawable.bg_card_thumbnail)
                    val drawable = ContextCompat.getDrawable(activity, R.drawable.bg_card_thumbnail)
                    val gradient = drawable as GradientDrawable
                    gradient.setColor(ColorUtils.setAlphaComponent(activity.config.primaryColor, THUMBNAIL_BACKGROUND_ALPHA))
                    imageView.background = gradient
                    imageView.setImageBitmap(bitmap)
                    imageView.scaleType = ImageView.ScaleType.CENTER
                    if (photoViews.size >= maxPhotos) return@map
                    photoViews.add(imageView)
                }

                if (!isInterrupted) {
                    activity.runOnUiThread {
                        holder.photoViews.removeAllViews()
                        photoViews.map {
                            holder.photoViews.addView(it)
                        }
                        holder.photoViews.visibility = View.VISIBLE
                    }
                }
            }
            realmInstance.close()
        }
    }

    private class ViewHolder {
        lateinit var photoContainer: FrameLayout
        lateinit var photoViews: LinearLayout
        var textView1: TextView? = null
        var textView2: TextView? = null
        var textView3: TextView? = null
        var imageView: ImageView? = null
        var item_holder: ViewGroup? = null
        var attachPhotoLoader: AttachPhotoLoader? = null
    }
}
