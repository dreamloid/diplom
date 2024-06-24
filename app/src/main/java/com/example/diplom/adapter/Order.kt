import android.os.Parcel
import android.os.Parcelable

data class Order(
    val Image: String,
    val ProductId: Int,
    val Title: String,
    val Volume: String,
    val TitleEng: String,
    val Price: String,
    val Quantity: Int,
    val isLiked: Boolean
)
    : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(Image)
        parcel.writeInt(ProductId)
        parcel.writeString(Title)
        parcel.writeString(Volume)
        parcel.writeString(TitleEng)
        parcel.writeString(Price)
        parcel.writeInt(Quantity)
        parcel.writeByte(if (isLiked) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Order> {
        override fun createFromParcel(parcel: Parcel): Order {
            return Order(parcel)
        }

        override fun newArray(size: Int): Array<Order?> {
            return arrayOfNulls(size)
        }
    }
}
