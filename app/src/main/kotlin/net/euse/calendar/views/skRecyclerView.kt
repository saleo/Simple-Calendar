package net.euse.calendar.views

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import net.euse.calendar.R
import com.simplemobiletools.commons.views.MyRecyclerView

open class skRecyclerView : MyRecyclerView{
    private var emptyView: View?=null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        emptyView=View.inflate(context, R.layout.empty_recycler_view,null)

    }
    private fun checkIfEmpty() {
        if (emptyView != null && adapter != null) {
            (emptyView as View).visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
            visibility = if (adapter.itemCount == 0) View.GONE else View.VISIBLE
        }
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        val oldAdapter = getAdapter()
        if (oldAdapter!=null) {
            if (!oldAdapter.hasObservers())
                oldAdapter.registerAdapterDataObserver(observer)
        }
        else{
            super.setAdapter(adapter)
            adapter!!.registerAdapterDataObserver(observer)
        }

        checkIfEmpty()
    }

    private val observer = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {
            checkIfEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }
    }
}
