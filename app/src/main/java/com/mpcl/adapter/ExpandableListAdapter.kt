package com.mpcl.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mpcl.R
import com.mpcl.custom.BoldTextView
import com.mpcl.model.MenuModel

class ExpandableListAdapter(
    var context: Context,
    var listDataHeader: List<MenuModel>,
    var listDataChild: MutableMap<MenuModel, MutableList<MenuModel>>
) :
    BaseExpandableListAdapter() {

    //private val listDataHeader: List<MenuModel>? = null
    //private val listDataChild: HashMap<MenuModel, List<MenuModel>>? = null

    override fun getChild(groupPosition: Int, childPosititon: Int): MenuModel {
        return listDataChild[listDataHeader[groupPosition]]
            ?.get(childPosititon)!!
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        return childPosition.toLong()
    }

    override fun getChildView(
        groupPosition: Int, childPosition: Int,
        isLastChild: Boolean, convertView: View?, parent: ViewGroup?
    ): View? {
        var convertView = convertView
        val childText: String = getChild(groupPosition, childPosition).name
        if (convertView == null) {
            val infalInflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = infalInflater.inflate(R.layout.list_item, null)
        }
        val txtListChild = convertView!!.findViewById<TextView>(R.id.lblListItem)
        txtListChild.text = childText
        return convertView
    }

    override fun getChildrenCount(groupPosition: Int): Int {
        return if (listDataChild[listDataHeader[groupPosition]] == null) 0 else listDataChild[listDataHeader[groupPosition]]!!
            .size
    }

    override fun getGroup(groupPosition: Int): MenuModel {
        return listDataHeader[groupPosition]
    }

    override fun getGroupCount(): Int {
        return listDataHeader.size
    }

    override fun getGroupId(groupPosition: Int): Long {
        return groupPosition.toLong()
    }

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean,
        convertView: View?, parent: ViewGroup?
    ): View? {
        var convertView = convertView
        val headerTitle: String = getGroup(groupPosition).name
        if (convertView == null) {
            val infalInflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = infalInflater.inflate(R.layout.list_group, null)
        }
        val lblListHeader = convertView!!.findViewById<BoldTextView>(R.id.lblListHeader)
        val arrow = convertView!!.findViewById<ImageView>(R.id.arrow)
        if(listDataHeader.get(groupPosition).isGroup==false){
            arrow.visibility = View.GONE
        }else{
            arrow.visibility = View.VISIBLE
        }
        lblListHeader.text = headerTitle
        /*lblListHeader.setOnClickListener{
            Log.d("Click","Click : "+listDataHeader.get(groupPosition).isGroup)
        }*/
        return convertView
    }

    override fun hasStableIds(): Boolean {
        return false
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
        return true
    }
}