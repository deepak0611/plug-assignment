package com.example.plug_assignment;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    Context mContext;
    Activity mActivity;
    List<UserRoom> userRoomList = new ArrayList<>();

    public MyAdapter(Context mContext, Activity mActivity, List<UserRoom> userRoomList){
        this.mContext = mContext;
        this.mActivity = mActivity;
        this.userRoomList = userRoomList ;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserRoom userRoom = userRoomList.get(position);
        Glide.with(mContext).load(userRoom.getPhotoUrl()).into(holder.IV_friend_photo);
        holder.TV_friend_name.setText(userRoom.getFullName());
        holder.TV_friend_email.setText(userRoom.getEmail());
        holder.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((HomeActivity)mActivity).connectToFriend(userRoom.getUser_id());
            }
        });
    }

    @Override
    public int getItemCount() {
        return userRoomList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView IV_friend_photo;
        TextView TV_friend_name,TV_friend_email;
        Button connect;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            IV_friend_photo = itemView.findViewById(R.id.IV_friend_photo);
            TV_friend_name = itemView.findViewById(R.id.TV_friend_name);
            TV_friend_email = itemView.findViewById(R.id.TV_friend_email);
            connect = itemView.findViewById(R.id.connect);
        }
    }
}
