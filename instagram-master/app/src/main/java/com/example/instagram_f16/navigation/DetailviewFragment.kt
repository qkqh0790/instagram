package com.example.instagram_f16.navigation
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.instagram_f16.R
import com.example.instagram_f16.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_detail.view.*
import kotlinx.android.synthetic.main.item_detail.view.*
import java.util.ArrayList

class DetailViewFragment : Fragment(){
    var firestore : FirebaseFirestore?=null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = LayoutInflater.from(activity).inflate(R.layout.fragment_detail,container,false)
        firestore= FirebaseFirestore.getInstance()


        view.detailviewfragment_recyclerview.adapter = DetailRecyclerViewAdapter()
        view.detailviewfragment_recyclerview.layoutManager = LinearLayoutManager(activity)
        return view
    }
    inner class DetailRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        val contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        val contentUidList: ArrayList<String> = arrayListOf()

        init {

            firestore?.collection("images")?.orderBy("timestamp")?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                notifyDataSetChanged()
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val viewholder = (holder as CustomViewHolder).itemView

            // 유저 아이디
            viewholder.detailviewitem_explain_textview.text = contentDTOs!![position].userId

            // 가운데 이미지
            Glide.with(holder.itemView.context)
                .load(contentDTOs[position].imageUrl)
                .into(viewholder.detailviewitem_imageview_content)

            // 설명 텍스트
            viewholder.detailviewitem_explain_textview.text = contentDTOs[position].explain

            //좋아요
            viewholder.detailviewitem_favoritecounter_textview.text = "좋아요 " + contentDTOs[position].favoriteCount


            //유저 프로필
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl).into(viewholder.detailviewitem_profile_image)
        }



    }

}
