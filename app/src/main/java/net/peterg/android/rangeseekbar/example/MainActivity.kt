package net.peterg.android.rangeseekbar.example

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.plant(Timber.DebugTree())

        rangeSeekBar1.setData(listOf(SimpleDataClass("Min", -1), SimpleDataClass("2", 2), SimpleDataClass("3", 3), SimpleDataClass("Max", -1)))

        setDataList.setOnClickListener {
            rangeSeekBar2.setData(listOf(SimpleDataClass("0", 0), SimpleDataClass("1", 1), SimpleDataClass("2", 2), SimpleDataClass("3", 3),
                    SimpleDataClass("4", 4), SimpleDataClass("5", 5), SimpleDataClass("6", 6), SimpleDataClass("7", 7), SimpleDataClass("Max", -1)))
            rangeSeekBar3.setData(listOf(SimpleDataClass("A", 100), SimpleDataClass("B", 200), SimpleDataClass("C", 300)))
        }

        rangeSeekBar2.callbackAction = { bar, leftThumb, rightThumb, fromUser ->
            Timber.d("The bar $bar changed to $leftThumb, $rightThumb and was user intende? $fromUser")
        }
    }
}

@Parcelize
data class SimpleDataClass(private val label: String, val value: Int) : Parcelable {
    override fun toString() = label
}
