package com.example.architecturtestproject.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class TestData(

    @PrimaryKey override var id: String = UUID.randomUUID().toString(),
    var elementNumber: Int = 0,
    var url: String = "https://picsum.photos/200",
    var unread: Boolean = true,
    var text: String = ""

) : RealmObject(), RealmModelObject {

}