package com.elorrieta.alumnoclient.entity

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.sql.Timestamp

@Parcelize
@JsonIgnoreProperties(ignoreUnknown = true)
data class  User(
    var id: Long? = null,
    var role: Role? = null,
    var name: String? = null,
    var email: String? = null,
    var emailVerifiedAt: Timestamp? = null,
    var password: String? = null,
    var rememberToken: String? = null,
    var createdAt: Timestamp? = null,
    var updatedAt: Timestamp? = null,
    var deletedAt: Timestamp? = null,
    var lastname: String? = null,
    var pin: String? = null,
    var address: String? = null,
    var phone1: String? = null,
    var phone2: String? = null,
    var photo: ByteArray? = null,
    var intensive: Boolean = false,
    var registered: Boolean = false,
    var modules: Set<Module> = mutableSetOf(),
    var enrollments: Set<Enrollment> = mutableSetOf(),
    var meetings: Set<Meeting> = mutableSetOf()
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Long::class.java.classLoader) as? Long,
        TODO("role"),
        parcel.readString(),
        parcel.readString(),
        TODO("emailVerifiedAt"),
        parcel.readString(),
        parcel.readString(),
        TODO("createdAt"),
        TODO("updatedAt"),
        TODO("deletedAt"),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createByteArray(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte(),
        TODO("modules"),
        TODO("enrollments"),
        TODO("meetings")
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(rememberToken)
        parcel.writeString(lastname)
        parcel.writeString(pin)
        parcel.writeString(address)
        parcel.writeString(phone1)
        parcel.writeString(phone2)
        parcel.writeByteArray(photo)
        parcel.writeByte(if (intensive) 1 else 0)
        parcel.writeByte(if (registered) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}

annotation class Parcelize
