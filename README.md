# RangeSeekBar
RangeSeekBar is an Android Library to show a distinct slider let the user set ranges.

# Usage
Add a RangeSeekBar to your xml files and set color, which thumb to draw,...
```xml
<net.peterg.android.rangeseekbar.RangeSeekBar
    android:id="@+id/rangeSeekBar1"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginEnd="8dp"
    android:layout_marginStart="8dp"
    android:layout_marginTop="8dp"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent"
    app:showPin="true"
    app:thumbAndSeekBarColor="@color/colorAccent"
    app:thumbToDraw="both" />
```
## Possible attributes to change
* thumbAndSeekBarColor -> changes the color of the thumbs and the seek bar
  * color|reference
* thumbRadius -> changes the radius of the thumbs
  * dimension
* showPin -> shows/hides the pin with current value
  * boolean
* drawPinShadow -> shows/hides a shadow pin with the current value which is always visible
  * boolean
* thumbToDraw -> defines which thumb to draw
  *leftThumb
  *rightThumb
  *both
## Set data
Data to set in the RangeSeekBar has to implement Parcelable. The text shown in the pins is taken from the toString method of your Object.
```java
rangeSeekBar.setData(listOf(SimpleDataClass("Min", -1), SimpleDataClass("2", 2), SimpleDataClass("3", 3), SimpleDataClass("Max", -1)))

@Parcelize
data class SimpleDataClass(private val label: String, val value: Int) : Parcelable {
    override fun toString() = label
}
```
## Get value changes
Just implement a lambda function for the callbackAction field of the RangeSeekBar:
```java
rangeSeekBar.callbackAction = { bar, leftThumb, rightThumb, fromUser ->
    Timber.d("The bar $bar changed to $leftThumb, $rightThumb and was user intende? $fromUser")
}
```
# License
[Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)
