package com.example.architecturtestproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.architecturtestproject.model.TestData
import io.realm.Realm
import io.realm.Sort

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: TestDataAdapter
    private lateinit var viewManager: LinearLayoutManager

    internal var realm: Realm? = null

    var theList: List<TestData>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Realm.init(this)

        realm = Realm.getDefaultInstance()

        /* Testcode to create a lot of data.*/
        realm?.apply {
            if (this.where(TestData::class.java).count() < 1) {

                this.executeTransaction {
                    it.copyToRealm((0 until 1000000).map { index ->
                        val to = TestData()
                        to.elementNumber = index
                        to
                    })

                }

            }
        }

        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        viewAdapter = TestDataAdapter()

        recyclerView = findViewById<RecyclerView>(R.id.list).apply {

            setHasFixedSize(true)

            layoutManager = viewManager

            adapter = viewAdapter
        }

        realm?.let { db ->

            println("RealmTest: Fetch starts.")

            /*
            Scenario 1: Fetch without any sort. Everything happens fast.
             */
            /*theList = db.where(TestData::class.java)
                .findAllAsync()
                .also {
                    it.addChangeListener { newList ->
                        println("RealmTest: Data emitted.")
                        viewAdapter.setData(newList)
                    }
                }*/


            /*
            Scenario 2: Fetches a bit slower because before fetch a sort is happen, which is understandable. An update on that
            list is also slow. What takes time is to calculate the changeset for the sorted list.
             */
            /*theList = db.where(TestData::class.java)
                .sort(
                    arrayOf("unread", "elementNumber", "url"),
                    arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
                )
                .findAllAsync()
                .also {
                    it.addChangeListener { newList ->
                        println("RealmTest: Data emitted.")
                        viewAdapter.setData(newList)
                    }
                }*/


            /*
            Scenario 3: Fetches the list without sorting and sorts the list before setting it to the adapter.
            It makes sense that sorting still slow, but also the change listener added to the unsorted list gets slow.
             */
            theList = db.where(TestData::class.java)
                .findAllAsync()
                .also {
                    it.addChangeListener { newList ->
                        println("RealmTest: Data emitted.")
                        viewAdapter.setData(newList
                            .sort(
                                arrayOf("unread", "elementNumber", "url"),
                                arrayOf(Sort.DESCENDING, Sort.DESCENDING, Sort.DESCENDING)
                            ))
                    }
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        realm?.close()
    }


    private inner class TestDataAdapter :
        RecyclerView.Adapter<TestDataAdapter.TestDataViewHolder>() {

        private var data: List<TestData> = emptyList()

        fun setData(data: List<TestData>) {
            this.data = data
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestDataViewHolder {
            return TestDataViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_view, null, false)
            )
        }

        override fun getItemCount(): Int {
            return data.size
        }

        override fun onBindViewHolder(holder: TestDataViewHolder, position: Int) {
            holder.setData(data[position])
        }

        inner class TestDataViewHolder(
            itemView: View
        ) : RecyclerView.ViewHolder(itemView) {

            fun setData(data: TestData) {
                itemView.findViewById<TextView>(R.id.index).text = data.elementNumber.toString()
                itemView.findViewById<TextView>(R.id.text).text = if (data.unread) "unread" else "read"

                itemView.setOnClickListener {
                    realm?.executeTransaction {
                        println("RealmTest: Data clicked.")
                        data.unread = !data.unread
                    }
                }
            }

        }
    }
}

